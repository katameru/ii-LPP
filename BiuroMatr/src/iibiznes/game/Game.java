
package iibiznes.game;

import iibiznes.fields.BoardInfo;
import iibiznes.fields.FieldInfo;
import iibiznes.game.cards.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

/**
 *
 * @author grzes
 */
public class Game
{
    public Game(NewGameSettings setts)
    {
        makePlayers(setts);
        makeFields();
        makeCardStacks();
        premium = setts.premium;
        pcs = new PropertyChangeSupport(this);
    }
    
    public Player curr()
    {
        return players[nextPlayer];
    }
        
    public Player[] getPlayers()
    {
        return players;
    }

    public Field[] getFields()
    {
        return fields;
    }

    public GameIO getGameIO()
    {
        return gameIO;
    }

    public void setGameIO(GameIO gameIO)
    {
        this.gameIO = gameIO;
    }

    public int getPremium()
    {
        return premium;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.addPropertyChangeListener(listener);
    }

    public int getGameState()
    {
        return gameState;
    }
    
    public void start()
    {
        gameState = STARTED;
        gameIO.begin();
        gameIO.turn(curr());
    }
    
    public void rollTheDices()
    {
        Player pl = next();
        if (!inPrison(pl))
        {
            int pos = pl.getPosition();
            Pair pair = ru.rollTheDices();
            gameIO.rolled(pl, pair.x, pair.y);
            int sum = pair.x + pair.y;
            boolean doubleDublet = false;
            if (pair.x == pair.y)
            {
                pair = ru.rollTheDices();
                gameIO.rolled(pl, pair.x, pair.y);
                sum += pair.x + pair.y;
                if (pair.x == pair.y)
                {
                    gameIO.doubleDouble(pl);
                    pl.setInPrison(2);
                }
            }
            if (!doubleDublet)
            {
                pl.setPosition( (pl.getPosition() + sum) % fields.length );
                gameIO.arrives(pl);
                if (pl.getPosition() < pos) // przeszliśmy przez start
                {
                    pl.addCS(premium);
                    gameIO.premium(pl);
                }
                fields[pl.getPosition()].arrive(pl);

                if (pl.getCS() < 0)
                {
                    pl.inGame = false;
                    if (winner() != null)
                    {
                        gameIO.winner(winner());
                    }
                }
            }
        }
        gameIO.turn(curr());
    }
    
    void wantBuy(Player pl, Buyable b)
    {
        if (b.getOwner() != null)
        {
            System.err.println("Gracz " + pl.getName() + " próbuje kupić "
                    + b.getFieldInfo().name + " ale ono jest już sprzedane!");
            return ;
        }
        b.setOwner(pl);
        pl.addCS( -b.getPrice() );
        pl.addProperty(b);
        pcs.firePropertyChange("OwnerChanged", null, null);
    }
    
    private boolean inPrison(Player pl)
    {
        int a = pl.getInPrison();
        if (a > 0)
        {
            gameIO.inPrison(pl);
            pl.setInPrison(a-1);
            return true;
        }
        return false;
    }
    
    private Player winner()
    {
        Player winner = null;
        for (Player p: players)
        {
            if (p.inGame)
            {
                if (winner == null) winner = p;
                else return null;
            }
        }
        return winner;
    }
    
    
    
    private void makePlayers(NewGameSettings setts)
    {
        players = new Player[setts.players];
        for (int i = 0; i < players.length; ++i)
        {
            players[i] = new Player(setts.names[i], setts.colors[i], setts.startCS);
        }
    }
    
    private void makeFields()
    {
        FieldInfo[] fi = BoardInfo.getFields();
        fields = new Field[fi.length];
        for (int i = 0; i < fi.length; ++i)
        {
            if (fi[i].type.equalsIgnoreCase("topic"))
                fields[i] = new Topic(fi[i], this);
            else if (fi[i].type.equalsIgnoreCase("empty"))
                fields[i] = new Empty(fi[i], this);
            else if (fi[i].type.equalsIgnoreCase("other"))
            {
                if (fi[i].subject.equalsIgnoreCase("Półobowiązek"))
                    fields[i] = new HalfDuty(fi[i], this);
                else fields[i] = new Course(fi[i], this);
            }
            else if (  fi[i].type.equalsIgnoreCase("rchance") )
                fields[i] = new Chance(fi[i], this, Chance.RED);
            else if (  fi[i].type.equalsIgnoreCase("bchance") )
                fields[i] = new Chance(fi[i], this, Chance.BLUE);
            else if (fi[i].type.equalsIgnoreCase("Loss"))
                fields[i] = new Learning(fi[i], this, fi[i].price);
            else if (fi[i].type.equalsIgnoreCase("Prison"))
            {
                fields[i] = new Empty(fi[i], this);
                ToPrison.prisonField = i;
            }
            else if (fi[i].type.equalsIgnoreCase("ToPrison"))
                fields[i] = new ToPrison(fi[i], this);
            else {
                fields[i] = new Empty(fi[i], this);
                System.err.println("Unknown type of field: \""
                        + fi[i].type + "\"");
            }
        }
    }
    
    private void makeCardStacks()
    {
        ArrayList<Card> rc = new ArrayList<Card>();
        rc.add(new Loss(100, "Oblany egzamin obniża Twoje motywacje."));
        rc.add(new Loss(200, "Awans w pracy! Może nie opłaca się studiować?"));
        rc.add(new Loss(400, "Kończysz wieloletni związek, nic Ci się nie chce."));
        rc.add(new Premium(100,"Za dobrą ocenę z ćwiczeń jest zwolnienie z egzaminu!"));
        rc.add(new Premium(200,"Wysoki wynik kolokwium zwiększa Twoje morale!"));
        rc.add(new Premium(300,"Uczęszczasz na przedmiot wykładowcy z powołania."));
        rc.add(new Move(5));
        rc.add(new Move(-3));
        rc.add(new Tasks(0,50,false));
        rc.add(new Tasks(300,0,true));
        CardStack red = new CardStack(rc);
        
        ArrayList<Card> bc = new ArrayList<Card>();
        bc.add(new Loss(200, "Lista zadań jest bardzo trudna."));
        bc.add(new Loss(200, "Znajomi nic się nie uczą, a wszystko zaliczają."));
        bc.add(new Loss(300, "Wynik z koła niższy niż się spodziewałeś."));
        bc.add(new Premium(50,"Za dobrą ocenę z ćwiczeń jest zwolnienie z egzaminu!"));
        bc.add(new Premium(200,"Przedmiot Cię zainteresował."));
        bc.add(new Premium(400,"Masz realne szanse na stypendium."));
        bc.add(new Move(7));
        bc.add(new Move(-4));
        bc.add(new Tasks(100,0,false));
        bc.add(new Tasks(0,150,true));
        CardStack blue = new CardStack(bc);
        
        red.shuffle();
        blue.shuffle();
        Chance.setStacks(red, blue);
    }
    
    private Player next()
    {
        Player p = players[nextPlayer++];
        if (nextPlayer == players.length) nextPlayer = 0;
        return p;
    }    
    
    public static final int NOT_STARTED = 0, STARTED = 1, ENDED = 2;
    private int gameState = NOT_STARTED;
    private PropertyChangeSupport pcs;
    private RandomUtils ru = new RandomUtils();   
    private GameIO gameIO;
    private Field[] fields;
    private Player[] players;
    private int premium;
    private int nextPlayer;
}
