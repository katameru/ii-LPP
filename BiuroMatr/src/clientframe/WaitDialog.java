
package clientframe;

import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author grzes
 */
public class WaitDialog extends JDialog
{
    public WaitDialog(JFrame parent)
    {
        super(parent, "Please wait.", true);
        add(new JLabel("Please wait for a while", JLabel.CENTER));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setToTheMiddle(parent);
    }    
    
    public WaitDialog(JApplet applet)
    {
        super((JFrame) null, "Please wait.", true);
        add(new JLabel("Please wait for a while", JLabel.CENTER));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setToTheMiddle(applet);
    }    
    
    private void setToTheMiddle(JFrame parent)
    {
        int w = 200,
            h = 100,
            px = parent.getLocation().x,
            py = parent.getLocation().y,
            ph = parent.getHeight(),
            pw = parent.getWidth();
        int x = px + (pw - w)/2,
            y = py + (ph - h)/2;
        setSize(w,h);
        setLocation(x,y);
    } 
    
    private void setToTheMiddle(JApplet applet)
    {
        int w = 200,
            h = 100,
            px = applet.getLocation().x,
            py = applet.getLocation().y,
            ph = applet.getHeight(),
            pw = applet.getWidth();
        int x = px + (pw - w)/2,
            y = py + (ph - h)/2;
        setSize(w,h);
        setLocation(x,y);
    }
}
