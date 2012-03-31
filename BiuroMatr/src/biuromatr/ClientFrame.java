
package biuromatr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author grzes
 */
public class ClientFrame extends JFrame
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater( new Runnable(){

            @Override
            public void run()
            {
                JFrame frame = new ClientFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400, 300);
                frame.setVisible(true);
            }            
        });
    }
    
    ClientFrame()
    {
        setLayout(new BorderLayout());
        add(area, BorderLayout.CENTER);
        area.setEditable(false);
        JPanel south = createSouth();
        add(south, BorderLayout.SOUTH);
        add(bttnConn, BorderLayout.NORTH);
        
        
        pcl = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (evt.getPropertyName().equalsIgnoreCase("mssg"))
                {
                    area.append("Dude: " + evt.getNewValue() + "\n");
                }
                else if (evt.getPropertyName().equalsIgnoreCase("connected"))
                {
                    area.append("<|Connection established|>\n");                    
                }
            }
        };
        bttnConn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (client == null)
                {
                    try
                    {
                        client = new Client();
                        client.addPropertyChangeListener(pcl);
                        client.start();
                    } catch (Exception ex)
                    {
                        JOptionPane.showMessageDialog(null, ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        
    }
    
    private JPanel createSouth()
    {
        GridBagLayout gbl = new GridBagLayout();
        JPanel panel = new JPanel(gbl);
        GridBagConstraints gbc = new GridBagConstraints();

        field = new JTextField();
        field.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!field.getText().equals(""))
                {
                    send(field.getText());
                    field.setText("");
                }
            }
        });
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 10.0;
        gbl.setConstraints(field, gbc);
        panel.add(field);

        bttnSend = new JButton("Send");
        bttnSend.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                send(field.getText());
                field.setText("");
            }
        });
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        gbl.setConstraints(bttnSend, gbc);
        panel.add(bttnSend);

        return panel;
    }
    
    private void send(String text)
    {
        try {
            client.write(text);
            area.append("Me:   " + text + "\n");
        } catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
            
    JTextArea area = new JTextArea();
    JTextField field = new JTextField();
    JButton bttnSend = new JButton("Send"),
            bttnConn = new JButton("Connect");
    Client client;
    PropertyChangeListener pcl;

}
