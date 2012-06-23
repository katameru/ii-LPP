
package iibiznes.game;

import biuromatr.Client;
import biuromatr.Utils;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.json.JSONObject;

/**
 *
 * @author grzes
 */
public class GameIO
{

    public GameIO(JTextPane messages, Client client)
    {
        this.messages = messages;
        this.client = client;
        doc = messages.getStyledDocument();
    }

    public Game getGame()
    {
        return game;
    }

    public void setGame(Game game)
    {
        this.game = game;
    }
    
    public JTextComponent getMessages()
    {
        return messages;
    }

    public void setMessages(JTextPane messages)
    {
        this.messages = messages;
    }
    
    public void appendMssg(String mssg, Player p)
    {
        Color c = (p == null ? Color.WHITE : p.getColor());
        try {
            doc.insertString(doc.getLength(),
                    mssg + "\n", doc.getStyle(c.toString()));
        } catch (BadLocationException ex) {
            Logger.getLogger(GameIO.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        JOptionPane.showMessageDialog(null, "Dwukrotnie wyrzuciłeś dublet."
                + " Udajesz się na obowiązkowe kucie.",
                "Idziesz do ryjca!", JOptionPane.INFORMATION_MESSAGE);
        String str = p.name + " dwukrotnie wyrzucił dublet, idzie do ryjca!";
        appendMssg(str, p);
    }
    
    public void goToPrison(Player p)
    {
        JOptionPane.showMessageDialog(null, "Stanąłeś na polu "
                + "\"Idziesz do ryjca!\". Udajesz się na obowiązkowe kucie.",
                "Idziesz do ryjca!", JOptionPane.INFORMATION_MESSAGE);
        String str = "Gracza " + p.name + " czeka obowiązkowa sesja w ryjcu!";
        appendMssg(str, p);
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
            str = p.name + " czeka na ruch " + (p.getInPrison() + 1) + " tury";
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
            JOptionPane.showMessageDialog(null,
                    "Masz za mało ChceniaSię aby zdać " + b.getFieldInfo().name,
                    "Nie zdasz", JOptionPane.INFORMATION_MESSAGE);
            appendMssg(p.getName() + " nie stać na zdanie.", p);
            return ;
        }
        int res = JOptionPane.showOptionDialog(null,
                "Czy chcesz zdać " + b.getFieldInfo().name + "?\n"
                + "Koszt: " + b.getPrice() + " ChceniaSię.",
                "Kupujesz?", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (res == JOptionPane.NO_OPTION)
            appendMssg("Nie kupuje.", p);
        else
        {
            game.wantBuy(p, b);
            appendMssg("Kupuje!", p);
        }
    }
    
    public void myField(Player p)
    {
        appendMssg("Stoi na własnym polu.", p);
    }
    
    public void premium(Player p)
    {
        appendMssg(p.getName() + " przechodzi przez Start, ChcenieSię rośnie!", p);
    }
    
   /* public Pair takeARoll(String str)
    {
        return RollDialog.rollTwoDices(null, str);
    }*/
    
    public void charge(Player player, Player owner, int charge)
    {
        JOptionPane.showMessageDialog(null, "To pole należy do gracza " 
                + owner.getName() + ", płacisz mu " + charge + " CS.",
                "Mniej Ci się chce", JOptionPane.INFORMATION_MESSAGE);
        appendMssg(player.getName() + " płaci graczowi " + owner.getName() + " "
                + charge + " CS.", player);
    }

    public void loss(Player pl, String desc, int loss)
    {        
        JOptionPane.showMessageDialog(null, desc + "\nStrata: " + loss + " CS",
                "Mniej Ci się chce", JOptionPane.INFORMATION_MESSAGE);
        appendMssg("ChcenieSię gracza " + pl.getName()
                + " zmniejsza się o " + loss + ".", pl);
    }
    
    public void card(Player pl, String desc)
    {
        JOptionPane.showMessageDialog(null, desc,
                "Szansa", JOptionPane.INFORMATION_MESSAGE);
        appendMssg(pl.getName() + " wyciąga kartę \"" + desc + "\".",
                pl);
    }
    
    void winner(Player winner)
    {
        appendMssg("Koniec! Zwycięża " + winner.getName() + "!", null);
        JOptionPane.showMessageDialog(null,
                "Koniec! Zwycięża " + winner.getName() + "!",
                "Koniec gry", JOptionPane.INFORMATION_MESSAGE);
    }
    
    Game game;
    JTextPane messages;    
    StyledDocument doc;
    Client client;
}
