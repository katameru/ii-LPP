
package iibiznes.frame;

import java.awt.Dimension;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import iibiznes.game.GameIO;
import javax.swing.JButton;
import iibiznes.fields.BoardInfo;
import iibiznes.game.Player;
import java.util.TreeMap;
import javax.swing.JTabbedPane;
import iibiznes.game.Game;
import iibiznes.game.NewGameSettings;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.Image;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import static iibiznes.frame.Utils.*;

/**
 *
 * @author grzes
 */
public class GameFrame extends JFrame
{
    public GameFrame(NewGameSettings setts)
    {
        setTitle("II-Biznes");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
        setLayout(new BorderLayout());
        
        
        game = new Game(setts);
        createPanes();
        createGameIO(setts);
        game.start();
        pack();
        center(this);
    }
    
    private void createPanes()
    {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);
        c.fill = GridBagConstraints.BOTH;   
        
        c.gridheight = 3;
        c.gridwidth = GridBagConstraints.REMAINDER;
        boardLabel.setHorizontalAlignment(JLabel.CENTER);
        gridbag.setConstraints(boardLabel, c);
        add(boardLabel);        
        
        c.gridheight = 2;
        c.gridwidth = 0;
        Component lower = createLower();
        lower.setPreferredSize(new Dimension(800,300));
        gridbag.setConstraints(lower, c);
        add(lower);
        
        updateBoardImg();
    }
    
    private Component createLower()
    {        
        Component tabs = createTabs();
        diary = createDiary();
        Component controlPanel = createControlPanel();
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(gridbag);
        c.fill = GridBagConstraints.BOTH;      
        
        c.weightx = 2.0;
        c.gridwidth = 1;
        gridbag.setConstraints(tabs, c);
        panel.add(tabs);
        
        c.weightx = 2.0;
        c.gridwidth = GridBagConstraints.RELATIVE;
        JScrollPane scroll = new JScrollPane(diary);
        gridbag.setConstraints(scroll, c);
        panel.add(scroll);
        
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(controlPanel, c);
        panel.add(controlPanel);
        
        return panel;
    }
    
    private JTextPane createDiary()
    {
        JTextPane comp = new JTextPane();
        comp.setBackground(Color.BLACK);
        comp.setForeground(Color.WHITE);
        comp.setEditable(false);
        comp.setSize(300,200);
        comp.setFont(new Font("Arial", Font.PLAIN, 10));
        comp.setPreferredSize(new Dimension(250,200));
        return comp;
    }
    
    private Component createTabs()
    {
        pPanels = new ManyComponents();
        for (Player p: game.getPlayers())
        {
            PlayerPanel pp = new PlayerPanel(p, game);
            pPanels.put(p.getId(), pp);
            p.addPropertyChangeListener(pp);
        }
        pPanels.setCurr(game.curr().getId());
        
        JPanel fieldPanel = createFieldPanel();
        JPanel chargesPanel = new ChargesForm(game);
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Gracz", pPanels);
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
                game.rollTheDices();
                pPanels.setCurr(game.curr().getId());
                repaint();
                updateBoardImg();
            }
        });
        panel.add(roll);
     //   panel.setMinimumSize(new Dimension(100,150));
        return panel;
    }
    
    private void updateBoardImg()
    {
        Image img = game.drawBoard();
        boardLabel.setIcon(new ImageIcon(img) );
    }
    
    private void createGameIO(NewGameSettings setts)
    {
        gameIO = new GameIO(this, diary);
        gameIO.setGame(game);
        game.setGameIO(gameIO);
    }
    
    Game game;
    GameIO gameIO;
    JLabel boardLabel = new JLabel();
    ManyComponents pPanels;
    JTextPane diary;
}


class ManyComponents extends JPanel
{
    public void put(Integer key, Component value)
    {
        comps.put(key, value);
       // value.setVisible(false);
       // add(value);
    }
    
    public void setCurr(int nr)
    {
        if (curr != null) remove(curr);
        curr = comps.get(nr);
        if (curr != null) add(curr);
    }
    
    Component curr;
    TreeMap<Integer, Component> comps = new TreeMap<Integer, Component>();
}