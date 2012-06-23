
package clientframe;

import java.awt.Component;
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
    
    public WaitDialog(Component comp)
    {
        super((JFrame) null, "Please wait.", true);
        add(new JLabel("Please wait for a while", JLabel.CENTER));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setToTheMiddle(comp);
    } 
    
    private void setToTheMiddle(Component comp)
    {
        int w = 200,
            h = 100,
            px = comp.getLocation().x,
            py = comp.getLocation().y,
            ph = comp.getHeight(),
            pw = comp.getWidth();
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
