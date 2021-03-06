package pl.kata.yahtzee;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;

public class GameSelect extends JPanel
{
	private static final long serialVersionUID = 1L;
	public GameSelect(GameClientInterface parent)
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
                parent.getHandler().refreshChannels();
            }
        });
        
        join.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String hostname = getSelected();
                if (hostname != null)
                    parent.getHandler().join(hostname);
            }
        });
        
        beHost.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                parent.getHandler().startChannel();
            }
        });
        
        disc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                parent.getHandler().disconnect();
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
    
    private GameClientInterface parent;
    private JList<String> hosts = new JList<String>();
    private JButton refresh = new JButton("Refresh");
    private JButton join = new JButton("Join");
    private JButton beHost = new JButton("Be Host");
    private JButton disc = new JButton("Disconect");
    
}
