
package iibiznes.frame;

import iibiznes.game.NewGameSettings;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import iibiznes.fields.BoardInfo;
import iibiznes.fields.WrongFileException;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import static iibiznes.frame.Utils.*;
/**
 *
 * @author grzes
 */
public class IIBiznes extends JFrame
{

    public static void main(String[] args)
    {
        String pathToConf = "conf/";
        if (args.length > 0)
        {
            pathToConf = args[0];
        }
        try {
            BoardInfo.readConf(pathToConf);
        } catch (WrongFileException ex) {
            Logger.getLogger(IIBiznes.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Error while reading"
                    + " configuration.\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return ;
        }
        
        SwingUtilities.invokeLater( new Runnable()
        {
            @Override
            public void run()
            {
                JFrame frame = new IIBiznes();
                center(frame);
                frame.setVisible(true);
            }
        });
    }

    IIBiznes()
    {
        setSize(400,500);
        setTitle("II-Biznes");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        startPanel = new SettingsForm();
        startPanel.addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (evt.getPropertyName().equals("NewGame"))
                {
                    startGame( (NewGameSettings) evt.getNewValue());
                }
            }
        });
                
        add(startPanel, BorderLayout.CENTER);
        south = createSouth();
        add(south, BorderLayout.SOUTH);
    }
    
    private JPanel createSouth()
    {
        JPanel panel = new JPanel(new GridLayout(1,4));
        panel.add(new JLabel());
        panel.add(new JLabel());
        
        JButton exit = new JButton("Zamknij");
        exit.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });
        panel.add(exit);
        
        JButton start = new JButton("Start");
        start.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                NewGameSettings setts = startPanel.getSettings();
                startGame(setts);
            }
        });
        panel.add(start);
        return panel;
    }
    
    void startGame(NewGameSettings setts)
    {
        setVisible(false);
        final JFrame frame = new GameFrame(setts);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e)
            {
                IIBiznes.this.setVisible(true);
            }
        });
    }
    
    private SettingsForm startPanel;
    private JPanel south;
}
