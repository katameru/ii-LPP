
package clientframe;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 *
 * @author grzes
 */
public class PanelMenu extends JPanel
{

    public PanelMenu(GameInterface parent)
    {
        this.parent = parent;
        setLayout(new BorderLayout());
        add(hosts, BorderLayout.CENTER);
        add(createSouth(), BorderLayout.SOUTH);
        createListeners();
    }
    
    private JPanel createSouth()
    {
        JPanel panel = new JPanel(new GridLayout(1,4,10,20));
        panel.add(refresh);
        panel.add(join);
        panel.add(beHost);
        panel.add(disc);
        return panel;
    }
    
    private void createListeners()
    {
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                parent.refreshChannels();
            }
        });
        
        join.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String hostname = getSelected();
                if (hostname != null)
                    parent.join(hostname);
            }
        });
        
        beHost.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                parent.startChannel();
            }
        });
        
        disc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                parent.disconnect();
            }
        });
    }
    
    private String getSelected()
    {
        Object val = hosts.getSelectedValue();
        return val == null ? null : val.toString();
    }
    
    public void setHosts(String[] names)
    {
        hosts.setListData(names);
    }
    
    private GameInterface parent;
    private JList hosts = new JList();
    private JButton refresh = new JButton("Refresh");
    private JButton join = new JButton("Join");
    private JButton beHost = new JButton("Be Host");
    private JButton disc = new JButton("Disconect");
    
}
