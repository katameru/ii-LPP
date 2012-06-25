
package iibiznes.frame;

import iibiznes.fields.BoardInfo;
import iibiznes.fields.WrongFileException;
import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
/**
 *
 * @author grzes
 */
public class IIBiznesFrame extends JFrame
{

    public static void main(String[] args)
    {
        String pathToConf = "conf/";
        if (args.length > 0)
        {
            pathToConf = args[0];
        }
        try {
            BoardInfo.readConf(IIBiznesFrame.class);
        } catch (WrongFileException ex) {
            Logger.getLogger(IIBiznesFrame.class.getName()).log(Level.SEVERE, null, ex);
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
                JFrame frame = new IIBiznesFrame();
             //   center(frame);
                frame.setVisible(true);
            }
        });
    }

    IIBiznesFrame()
    {
        setSize(800,600);
        setTitle("II-Biznes");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setContentPane(new MainPanel());
    }
    
}
