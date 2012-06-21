
package iibiznes.frame;

import java.awt.Dimension;
import java.awt.Toolkit;
/**
 *
 * @author grzes
 */
public class Utils
{
    public static void center(java.awt.Component comp)
    {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int H = (int) dim.getHeight(), W = (int) dim.getWidth();
        int h = comp.getHeight(), w = comp.getWidth();
        int posY = (H-h)/2;
        int posX = (W-w)/2;
        comp.setLocation(posX, posY);
    }
}
