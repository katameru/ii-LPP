
package iibiznes.game;

import biuromatr.Client;
import biuromatr.Utils;
import iibiznes.frame.GamePanel;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author grzes
 */
public class GameIO
{

    public GameIO(GamePanel pgame, Client client)
    {
        this.pgame = pgame;
        this.client = client;
    }

    public Game getGame()
    {
        return game;
    }

    public void setGame(Game game)
    {
        this.game = game;
    }
    
    public void appendMssg(String mssg, Player p)
    {
        Color c = (p == null ? Color.WHITE : p.getColor());
        pgame.addToDiary(mssg, c);
        try {
            JSONObject json = Utils.makeJSON("gamedata");
            json.put("subtype", "diary");
            json.put("mssg", mssg);
            json.put("playerNr", (p == null ? -1 : p.id));
            client.sendData(json);
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
    }
    
    public void rolled(Player p, int dice1, int dice2)
    {
        String str = p.name + " wyrzucił " + dice1 + "+" + dice2;
        appendMssg(str, p);
    }
    
    public void turn(Player p)
    {
        String str = "\nKolej ma " + p.name;
        appendMssg(str, p);       
    }
    
    public void doubleDouble(Player p)
    {
        String str = p.name + " dwukrotnie wyrzucił dublet, idzie do ryjca!";
        appendMssg(str, p);
        if (playerIsHost(p))
        {
            JOptionPane.showMessageDialog(null, "Dwukrotnie wyrzuciłeś dublet."
                + " Udajesz się na obowiązkowe kucie.",
                "Idziesz do ryjca!", JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            sendMssgDialog("Dwukrotnie wyrzuciłeś dublet. Udajesz się na "
                    + "obowiązkowe kucie.", JOptionPane.INFORMATION_MESSAGE, p.getName());
        }
    }
    
    public void goToPrison(Player p)
    {
        String str = "Gracza " + p.name + " czeka obowiązkowa sesja w ryjcu!";
        appendMssg(str, p);
        if (playerIsHost(p))
        {
            JOptionPane.showMessageDialog(null, "Stanąłeś na polu "
                    + "\"Idziesz do ryjca!\". Udajesz się na obowiązkowe kucie.",
                    "Idziesz do ryjca!", JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            sendMssgDialog("Stanąłeś na polu \"Idziesz do ryjca!\". Udajesz się"
                    + " na obowiązkowe kucie.", JOptionPane.INFORMATION_MESSAGE, p.getName());
        }
    }
    
    public void unsupported(String str)
    {
        JOptionPane.showMessageDialog(null,
                "Funkcja \"" + str + "\" nie jest jeszcze zaimplemetnowana"
                , str, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void inPrison(Player p)
    {
        String str;
        if (p.getInPrison() > 0)
            str = p.name + " czeka na ruch " + p.getInPrison() + " tury";
        else str = p.name + " czeka na ruch jedną turę";
        appendMssg(str, p);         
    }
    
    public void arrives(Player pl)
    {
        String str = pl.name + " staje na polu " +
                game.getFields()[pl.getPosition()].getFieldInfo().name;
        appendMssg(str, pl);         
    }
    
    public void begin()
    {
        appendMssg("Rozpoczynamy nową grę!", null);               
    }
    
    public void maybeBuy(Player p, Buyable b)
    {
        if (p.getCS() < b.getPrice())
        {            
            if (playerIsHost(p))
            {            
                JOptionPane.showMessageDialog(null,
                        "Masz za mało motywacji aby zdać " + b.getFieldInfo().name,
                        "Nie zdasz", JOptionPane.INFORMATION_MESSAGE);
            }
            else
            {
                sendMssgDialog("Masz za mało motywacji aby zdać " + b.getFieldInfo().name,
                        JOptionPane.INFORMATION_MESSAGE, p.getName());
            }            
            appendMssg(p.getName() + " nie stać na zdanie.", p);
            return ;
        }
        
        int res;
        if (playerIsHost(p))
        {
            res = JOptionPane.showOptionDialog(null,
                    "Czy chcesz zdać " + b.getFieldInfo().name + "?\n"
                    + "Koszt: " + b.getPrice() + " motywacji.",
                    "Kupujesz?", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, null, null);
        }
        else
        {
            sendMssgDialog("Czy chcesz zdać " + b.getFieldInfo().name + "?\n"
                    + "Koszt: " + b.getPrice() + " motywacji.",
                    JOptionPane.QUESTION_MESSAGE, p.getName());    
            waitForAnswer();
            res = answer;
        }
        if (res == JOptionPane.NO_OPTION)
            appendMssg("Nie podchodzi do egzaminu.", p);
        else
        {
            game.wantBuy(p, b);
            appendMssg("Zdaje!", p);
        }
    }
    
    public void myField(Player p)
    {
        appendMssg("Stoi na własnym polu.", p);
    }
    
    public void premium(Player p)
    {
        appendMssg(p.getName() + " przechodzi przez Start, motywacja rośnie!", p);
    }
    
   /* public Pair takeARoll(String str)
    {
        return RollDialog.rollTwoDices(null, str);
    }*/
    
    public void charge(Player player, Player owner, int charge)
    {
        appendMssg(player.getName() + " płaci graczowi " + owner.getName() + " "
                + charge + " motywacji.", player);
        if (playerIsHost(player))
        {
            JOptionPane.showMessageDialog(null, "To pole należy do gracza " 
                    + owner.getName() + ", płacisz mu " + charge + " motywacji.",
                    "Mniej Ci się chce", JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            sendMssgDialog("To pole należy do gracza " 
                    + owner.getName() + ", płacisz mu " + charge + " motywacji.",
                    JOptionPane.INFORMATION_MESSAGE, player.getName());
        }
    }

    public void loss(Player pl, String desc, int loss)
    {        
        appendMssg("Motywacja gracza " + pl.getName()
                + " zmniejsza się o " + loss + ".", pl);
        if (playerIsHost(pl))
        {
            JOptionPane.showMessageDialog(null, desc + "\nStrata: " + loss + " motywacji",
                    "Mniej Ci się chce", JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            sendMssgDialog(desc + "\nStrata: " + loss + " motywacji",
                    JOptionPane.INFORMATION_MESSAGE, pl.getName());
        }
    }
    
    public void card(Player pl, String desc)
    {
        appendMssg(pl.getName() + " wyciąga kartę \"" + desc + "\".",
                pl);
        if (playerIsHost(pl))
        {
            JOptionPane.showMessageDialog(null, desc,
                    "Szansa", JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            sendMssgDialog(desc, JOptionPane.INFORMATION_MESSAGE, pl.getName());
        }
    }
    
    void winner(Player winner)
    {
        appendMssg("Koniec! Zwycięża " + winner.getName() + "!", null);
        JOptionPane.showMessageDialog(null,
                "Koniec! Zwycięża " + winner.getName() + "!",
                "Koniec gry", JOptionPane.INFORMATION_MESSAGE);
        for (String nick: client.getGuestNicks())
            sendMssgDialog("Koniec! Zwycięża " + winner.getName() + "!",
                    JOptionPane.INFORMATION_MESSAGE, nick);
    }
    
    
    synchronized private void waitForAnswer()
    {
        answer = NO_ANSWER;
        while (answer == NO_ANSWER)
        {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(ToPrison.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    synchronized public void gotAnswer(int ans)
    {
        answer = ans;
        notify();
    }
    
    public void sendMssgDialog(String mssg, int mssgType, String plNick)
    {
        try {
            JSONObject json = Utils.makeJSON("gamedata");
            json.put("subtype", "mssgdialog");
            json.put("mssg", mssg);
            json.put("mssgtype", mssgType);
            client.sendData(json, plNick);
        } catch (JSONException ex) {
            Logger.getLogger(GameIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean playerIsHost(Player p)
    {
        return client.getMyNick().equals(p.getName());
    }
    
    void loser(Player pl)
    {
        appendMssg(pl.getName() + " traci całą swoją motywację"
                + " i odpada ze studiów.", pl);
        String desc = "Skończyła Ci się motywacja. Zostajesz skreślony z listy studentów.";
        if (playerIsHost(pl))
        {
            JOptionPane.showMessageDialog(null, desc,
                    "Szansa", JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            sendMssgDialog(desc, JOptionPane.INFORMATION_MESSAGE, pl.getName());
        }
    }
    
    void left(Player pl)
    {
        appendMssg(pl.getName() + " odpuszcza sobie studia.", pl);
    }
    
    Game game;
    GamePanel pgame; 
    Client client;
    int answer;
    private final int NO_ANSWER = -452397;

}
