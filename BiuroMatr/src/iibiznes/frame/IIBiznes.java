
package iibiznes.frame;

import iibiznes.fields.BoardInfo;
import iibiznes.fields.WrongFileException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JApplet;
import javax.swing.JOptionPane;

/**
 *
 * @author grzes
 */
public class IIBiznes extends JApplet
{
    public IIBiznes()
    {
        setContentPane(new MainPanel());
    }
    
    public void init()
    {     
        try {
            BoardInfo.readConf(IIBiznes.class);
        } catch (WrongFileException ex) {
            Logger.getLogger(IIBiznesFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Error while reading"
                    + " configuration.\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
