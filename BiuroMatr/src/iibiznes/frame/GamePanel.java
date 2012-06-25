
package iibiznes.frame;

import clientframe.PanelChat;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import iibiznes.fields.BoardInfo;
import iibiznes.game.NewGameSettings;
import java.util.TreeMap;
import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.Image;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.JScrollBar;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 *
 * @author grzes
 */
public class GamePanel extends JPanel
{
    public GamePanel(MainPanel mainPanel, PanelChat pchat, DisplayInfo di)
    {
        this.mainPanel = mainPanel;
        this.pchat = pchat;
        this.di = di;
        createPanes();
    }
    
    public void update()
    {
        boardLabel.setIcon(new ImageIcon(drawBoard()));
        playerPanel.update();
    }
    
   /* private void createPanes()
    {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);
        c.fill = GridBagConstraints.BOTH;   
        
        c.gridheight = 2;
        c.gridwidth = GridBagConstraints.RELATIVE;
        boardLabel.setHorizontalAlignment(JLabel.CENTER);
        gridbag.setConstraints(boardLabel, c);
        add(boardLabel);                
                
        Component tabs = createTabs();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tabs, c);
        add(tabs);
        
        c.gridheight = 1;
        c.gridwidth = 0;
        pchat.setPreferredSize(new Dimension(300,200));
        add(pchat);        
        
        diary = createDiary();
        c.gridwidth = GridBagConstraints.RELATIVE;
        JScrollPane scroll = new JScrollPane(diary);
        gridbag.setConstraints(scroll, c);
        add(scroll);
        
        Component controlPanel = createControlPanel();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(controlPanel, c);
        add(controlPanel);
        
        update();
    }*/
    
    private void createPanes()
    {      
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel upper = new JPanel();
        upper.setLayout(new BoxLayout(upper, BoxLayout.X_AXIS));
        upper.add(boardLabel);                
                
        Component tabs = createTabs();
        upper.add(tabs);
        
        JPanel lower = new JPanel();
        lower.setLayout(new BoxLayout(lower, BoxLayout.X_AXIS));
        lower.add(pchat);
        
        diary = createDiary();
        scrollDiary = new JScrollPane(diary);
        scrollDiary.setPreferredSize(new Dimension(300,200));
        lower.add(scrollDiary);
        
        Component controlPanel = createControlPanel();
        lower.add(controlPanel);
        
        add(upper);
        add(lower);
        
        update();        
    }
    
    private JTextPane createDiary()
    {
        JTextPane comp = new JTextPane();
        comp.setBackground(Color.BLACK);
        comp.setForeground(Color.WHITE);
        comp.setEditable(false);
        comp.setSize(300,200);
        comp.setFont(new Font("Arial", Font.PLAIN, 10));
        //comp.setPreferredSize(new Dimension(250,200));
        
        /************** Adding style to diary **************************/        
        StyledDocument doc = comp.getStyledDocument();
        Style defStyle = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);
        for (String colstr: NewGameSettings.defColors)
        {
            Color c = BoardInfo.colorForName(colstr);
            Style s = doc.addStyle(c + "", defStyle);
            StyleConstants.setForeground(s, c);
        }
        Style s = doc.addStyle(Color.WHITE + "", defStyle);
        StyleConstants.setForeground(s, Color.WHITE);

        return comp;
    }
    
    private Component createTabs()
    {        
        playerPanel = new PlayerPanel(di);
        JPanel fieldPanel = createFieldPanel();
        JPanel chargesPanel = new ChargesForm();
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Gracz", playerPanel);
       // tabs.add("Pola", fieldPanel);
        tabs.add("Opłaty", chargesPanel);
        
      //  tabs.setMinimumSize(new Dimension(250,150));
        return tabs;        
    }
        
    private JPanel createFieldPanel()
    {
        return null;
    }

    private JPanel createControlPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JButton buttonTasks = new JButton("<html>Ułóż zadanie");
        buttonTasks.setEnabled(false);
        panel.add(buttonTasks);
        
        buttonRoll = new JButton(new ImageIcon(BoardInfo.getDicesImg()));
        buttonRoll.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                buttonRoll.setEnabled(false);
                mainPanel.rollTheDicesPressed();
            }
        });
        buttonRoll.setEnabled(false);
        panel.add(buttonRoll);
     //   panel.setMinimumSize(new Dimension(100,150));
        return panel;
    }
    
    
    private Image drawBoard()
    {
        Image board = BoardInfo.getBoardImg();
        BufferedImage img = new BufferedImage(board.getWidth(null),
                board.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = img.createGraphics();
        gr.drawImage(board, 0, 0, null);
        drawPlayers(gr, di);
        return img;
    }
    
    private void drawPlayers(Graphics2D gr, DisplayInfo di)
    {
        //map position -> nrs_of_players_at_this_position
        TreeMap<Integer, ArrayList<Integer>> map = 
                new TreeMap<Integer, ArrayList<Integer>>();
        for (int i = 0; i < di.players; ++i)
        {
            int pos = di.positions[i];
            if (pos < 0) continue;
            if (map.get(pos) == null) {
                ArrayList<Integer> list = new ArrayList<Integer>();
                list.add(i);
                map.put(pos, list);
            }
            else map.get(pos).add(i);
        }
        for (Map.Entry<Integer, ArrayList<Integer>> e: map.entrySet())
        {
            int pos = e.getKey();
            ArrayList<Integer> list = e.getValue();
            int R = 7, r = 4, midX = BoardInfo.getFields()[pos].midX,
                    midY = BoardInfo.getFields()[pos].midY;
            double angle = 0;
            for (Integer p: list)
            {
                int x = (int) (midX + R*Math.cos(angle));
                int y = (int) (midY + R*Math.sin(angle));
                gr.setColor(di.colors[p]);
                gr.fillOval(x-r,y-r,2*r,2*r);
                angle += 2*Math.PI/list.size();
            }
        }
    }

    void enableRoll(boolean on)
    {
        buttonRoll.setEnabled(on);
    }
    
    public void addToDiary(String mssg, Color c)
    {
        StyledDocument doc = diary.getStyledDocument();
        try {
            doc.insertString(doc.getLength(),
                    mssg + "\n", doc.getStyle(c.toString()));
        } catch (BadLocationException ex) {
            Logger.getLogger(GamePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        JScrollBar bar = scrollDiary.getVerticalScrollBar();
        bar.setValue(bar.getMaximum());
    }
 
    private MainPanel mainPanel;
    private DisplayInfo di;
    private JLabel boardLabel = new JLabel();
    private PlayerPanel playerPanel;
    private PanelChat pchat;
    private JTextPane diary;
    private JScrollPane scrollDiary;
    private JButton buttonRoll;
}
