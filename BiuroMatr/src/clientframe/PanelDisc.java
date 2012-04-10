
package clientframe;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author grzes
 */
public class PanelDisc extends JPanel
{
    PanelDisc(ClientFrame parent)
    {
        this.parent = parent;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(title);
        JPanel middle = new JPanel(new GridLayout(5,2,5,30));
        middle.add(new JLabel()); middle.add(new JLabel()); 
        middle.add(lblAddr); middle.add(addr); 
        middle.add(lblPort); middle.add(port); 
        middle.add(lblNick); middle.add(nick); 
        middle.add(new JLabel()); middle.add(new JLabel()); 
        add(middle);
        add(bttn);
        
        bttn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try {
                    InetAddress ia = InetAddress.getByName(addr.getText());
                    int portn = Integer.parseInt(port.getText());
                    PanelDisc.this.parent.connect(ia, portn, nick.getText());
                } catch (UnknownHostException ex) {
                    JOptionPane.showMessageDialog(PanelDisc.this.parent,
                            ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(PanelDisc.this.parent, 
                            "Not a valid port number.",
                            "Error", JOptionPane.ERROR_MESSAGE);                    
                }
            }
        });
    }
    
    private ClientFrame parent;
    private JTextField addr = new JTextField("localhost"),
               port = new JTextField("6666"),
               nick = new JTextField("");
    private JLabel lblAddr = new JLabel("IP address", JLabel.TRAILING),
           lblPort = new JLabel("Port", JLabel.TRAILING),
           lblNick = new JLabel("Nickname", JLabel.TRAILING),
           title = new JLabel("Server localization"); 
    private JButton bttn = new JButton("Connect");
}
