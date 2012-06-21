
package iibiznes.game;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 *
 * @author grzes
 */
public class GameIO
{

    public GameIO(JFrame parent, JTextPane messages)
    {
        this.parent = parent;
        this.messages = messages;
        doc = messages.getStyledDocument();
        defStyle = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);
    }

    public Game getGame()
    {
        return game;
    }

    public void setGame(Game game)
    {
        this.game = game;
        initStyles();
    }
    
    private void initStyles()
    {
        Player[] players = game.getPlayers();
        for (Player p: players)
        {
            Color c = p.getColor();
            Style s = doc.addStyle(c + "", defStyle);
            StyleConstants.setForeground(s, c);
        }
    }

    public JTextComponent getMessages()
    {
        return messages;
    }

    public void setMessages(JTextPane messages)
    {
        this.messages = messages;
    }
    
    public void appendMssg(String mssg, Color c)
    {
        try {
            doc.insertString(doc.getLength(), mssg + "\n", doc.getStyle(c.toString()));
        } catch (BadLocationException ex) {
            Logger.getLogger(GameIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void rolled(Player p, int dice1, int dice2)
    {
        String str = p.name + " wyrzucił " + dice1 + "+" + dice2;
        appendMssg(str, p.color);
    }
    
    public void turn(Player p)
    {
        String str = "\nKolej ma " + p.name;
        appendMssg(str, p.color);       
    }
    
    public void doubleDouble(Player p)
    {
        JOptionPane.showMessageDialog(parent, "Dwukrotnie wyrzuciłeś dublet."
                + " Udajesz się na obowiązkowe kucie.",
                "Idziesz do ryjca!", JOptionPane.INFORMATION_MESSAGE);
        String str = p.name + " dwukrotnie wyrzucił dublet, idzie do ryjca!";
        appendMssg(str, p.color);
    }
    
    public void goToPrison(Player p)
    {
        JOptionPane.showMessageDialog(parent, "Stanąłeś na polu "
                + "\"Idziesz do ryjca!\". Udajesz się na obowiązkowe kucie.",
                "Idziesz do ryjca!", JOptionPane.INFORMATION_MESSAGE);
        String str = "Gracza " + p.name + " czeka obowiązkowa sesja w ryjcu!";
        appendMssg(str, p.color);
    }
    
    public void unsupported(String str)
    {
        JOptionPane.showMessageDialog(parent,
                "Funkcja \"" + str + "\" nie jest jeszcze zaimplemetnowana"
                , str, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void inPrison(Player p)
    {
        String str;
        if (p.getInPrison() > 0)
            str = p.name + " czeka na ruch " + (p.getInPrison() + 1) + " tury";
        else str = p.name + " czeka na ruch jedną turę";
        appendMssg(str, p.color);         
    }
    
    public void arrives(Player pl)
    {
        String str = pl.name + " staje na polu " +
                game.getFields()[pl.getPosition()].getFieldInfo().name;
        appendMssg(str, pl.color);         
    }
    
    public void begin()
    {
        appendMssg("Rozpoczynamy nową grę!", Color.PINK);               
    }
    
    public void maybeBuy(Player p, Buyable b)
    {
        if (p.getCS() < b.getPrice())
        {
            JOptionPane.showMessageDialog(parent,
                    "Masz za mało ChceniaSię aby zdać " + b.getFieldInfo().name,
                    "Nie zdasz", JOptionPane.INFORMATION_MESSAGE);
            appendMssg(p.getName() + " nie stać na zdanie.", p.getColor());
            return ;
        }
        int res = JOptionPane.showOptionDialog(parent,
                "Czy chcesz zdać " + b.getFieldInfo().name + "?\n"
                + "Koszt: " + b.getPrice() + " ChceniaSię.",
                "Kupujesz?", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (res == JOptionPane.NO_OPTION)
            appendMssg("Nie kupuje.", p.getColor());
        else
        {
            game.wantBuy(p, b);
            appendMssg("Kupuje!", p.getColor());
        }
    }
    
    public void myField(Player p)
    {
        appendMssg("Stoi na własnym polu.", p.getColor());
    }
    
    public void premium(Player p)
    {
        appendMssg(p.getName() + " przechodzi przez Start, ChcenieSię rośnie!",
                   p.getColor());
    }
    
    public Pair takeARoll(String str)
    {
        return RollDialog.rollTwoDices(parent, str);
    }
    
    public void charge(Player player, Player owner, int charge)
    {
        JOptionPane.showMessageDialog(parent, "To pole należy do gracza " 
                + owner.getName() + ", płacisz mu " + charge + " CS.",
                "Mniej Ci się chce", JOptionPane.INFORMATION_MESSAGE);
        appendMssg(player.getName() + " płaci graczowi " + owner.getName() + " "
                + charge + " CS.", player.getColor());
    }

    public void loss(Player pl, String desc, int loss)
    {        
        JOptionPane.showMessageDialog(parent, desc + "\nStrata: " + loss + " CS",
                "Mniej Ci się chce", JOptionPane.INFORMATION_MESSAGE);
        appendMssg("ChcenieSię gracza " + pl.getName()
                + " zmniejsza się o " + loss + ".", pl.getColor());
    }
    
    public void card(Player pl, String desc)
    {
        JOptionPane.showMessageDialog(parent, desc,
                "Szansa", JOptionPane.INFORMATION_MESSAGE);
        appendMssg(pl.getName() + " wyciąga kartę \"" + desc + "\".",
                pl.getColor());
    }
    
    JFrame parent;
    Game game;
    JTextPane messages;
    StyledDocument doc;
    Style defStyle;

    void winner(Player winner)
    {
        appendMssg("Koniec! Zwycięża " + winner.getName() + "!", Color.WHITE);
        JOptionPane.showMessageDialog(parent,
                "Koniec! Zwycięża " + winner.getName() + "!",
                "Koniec gry", JOptionPane.INFORMATION_MESSAGE);
        parent.dispose();
    }
}
