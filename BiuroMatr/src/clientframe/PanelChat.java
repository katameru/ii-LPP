
package clientframe;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author grzes
 */
public class PanelChat extends JPanel
{

    public PanelChat(ClientFrame parent)
    {
        this.parent = parent;setLayout(new BorderLayout());
        add(new JScrollPane(area), BorderLayout.CENTER);
        area.setEditable(false);
        area.setText("Wait for connection...\n");
        JPanel south = createSouth();
        add(south, BorderLayout.SOUTH);
    }
    
    private JPanel createSouth()
    {
        GridBagLayout gbl = new GridBagLayout();
        JPanel panel = new JPanel(gbl);
        GridBagConstraints gbc = new GridBagConstraints();

        field.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!field.getText().equals(""))
                    send();
            }
        });
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 10.0;
        gbl.setConstraints(field, gbc);
        panel.add(field);

        bttnSend.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                send();
            }
        });
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        gbl.setConstraints(bttnSend, gbc);
        bttnSend.setEnabled(false);
        panel.add(bttnSend);

        
        bttnDisc.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                parent.leaveChat();
            }
        });
        gbl.setConstraints(bttnDisc, gbc);
        panel.add(bttnDisc);
        
        return panel;
    }
    
    private void send()
    {
        if (bttnSend.isEnabled())
        {
            area.append("<Me> " + field.getText() + "\n");
            parent.sendChatMssg(field.getText());
            field.setText("");     
        }
    }
    
    void clear()
    {
        area.setText("");
    }
    
    void append(String text)
    {
        area.append(text + "\n");
    }
    
    void sendEnabled(boolean on)
    {
        bttnSend.setEnabled(on);
    }
    
    private ClientFrame parent;
    private JTextArea area = new JTextArea();
    private JTextField field = new JTextField();
    private JButton bttnSend = new JButton("Send");
    private JButton bttnDisc = new JButton("Disc");
}
