
package iibiznes.frame;

import clientframe.PanelChat;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import iibiznes.fields.BoardInfo;
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
        boardLabel.setIcon(new ImageIcon(drawBoard(di)) );
        playerPanel.update();
    }
    
    private void createPanes()
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
        return comp;
    }
    
    private Component createTabs()
    {        
        playerPanel = new PlayerPanel(di);
        JPanel fieldPanel = createFieldPanel();
        JPanel chargesPanel = new ChargesForm();
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Gracz", playerPanel);
        tabs.add("Pola", fieldPanel);
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
        JButton roll = new JButton(new ImageIcon(BoardInfo.getDicesImg()));
        roll.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                mainPanel.rollTheDices();
            }
        });
        panel.add(roll);
     //   panel.setMinimumSize(new Dimension(100,150));
        return panel;
    }
    
    
    private Image drawBoard(DisplayInfo di)
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
    
 
    MainPanel mainPanel;
    DisplayInfo di;
    JLabel boardLabel = new JLabel();
    PlayerPanel playerPanel;
    PanelChat pchat;
    JTextPane diary;
}

/*  

private void createGameIO(NewGameSettings setts)
{
    gameIO = new GameIO(this, diary);
    gameIO.setGame(game);
    game.setGameIO(gameIO);
}
 

*/