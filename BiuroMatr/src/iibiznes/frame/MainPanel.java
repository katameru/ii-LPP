
package iibiznes.frame;

import biuromatr.Client;
import biuromatr.ConnectionException;
import biuromatr.Message;
import clientframe.GameInterface;
import clientframe.PanelChat;
import clientframe.PanelDisc;
import clientframe.PanelMenu;
import clientframe.WaitDialog;
import iibiznes.game.NewGameSettings;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author grzes
 */
public class MainPanel extends JPanel
                       implements PropertyChangeListener, GameInterface
{
    public MainPanel()
    {
        setLayout(new GridLayout());
        pdisc = new PanelDisc(this);
        pmenu = new PanelMenu(this);
        pcurr = pdisc;
        
        add(pdisc);
    }
    
    public void connect(InetAddress ia, int port, final String nick)
    {
        client = new Client(ia, port, "IIBiznes");
        try {
            client.init();
        } catch (SocketException ex) {
            Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        client.addPropertyChangeListener(this);
        
        final JDialog waitd = new WaitDialog(this);
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
            {
                try {
                    client.start(nick);
                } catch (ConnectionException ex) {
                    JOptionPane.showMessageDialog(MainPanel.this,
                            "Could not contact server.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done()
            {
                waitd.dispose();
            }
        }.execute();
        waitd.setVisible(true);        
    }
    
    public void disconnect()
    {
        client.close();
    }

    public void startChannel()
    { 
        final JDialog waitd = new WaitDialog(this);
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
            {
                try {
                    client.startChannel(client.getMyNick(), 5);
                } catch (ConnectionException ex) {
                    JOptionPane.showMessageDialog(MainPanel.this,
                            "Could not contact server.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done()
            {
                waitd.dispose();
            }
        }.execute();
        waitd.setVisible(true);        
    }

    public void join(final String hostname)
    { 
        final JDialog waitd = new WaitDialog(this);
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
            {
                try {
                    client.join(hostname);
                } catch (ConnectionException ex) {
                    JOptionPane.showMessageDialog(MainPanel.this,
                            "Could not contact server.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done()
            {
                waitd.dispose();
            }
        }.execute();
        waitd.setVisible(true);        
    }

    public void refreshChannels()
    { 
        final JDialog waitd = new WaitDialog(this);
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
            {
                try {
                    client.refreshChannels();
                } catch (ConnectionException ex) {
                    JOptionPane.showMessageDialog(MainPanel.this,
                            "Could not contact server.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done()
            {
                waitd.dispose();
            }
        }.execute();
        waitd.setVisible(true);        
    }

    public void sendChatMssg(final String text)
    {
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
            {
                try {
                    pchat.sendEnabled(false);
                    client.sendChatMssg(text);
                } catch (ConnectionException ex) {
                    JOptionPane.showMessageDialog(MainPanel.this,
                            ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done()
            {
                pchat.sendEnabled(true);
            }
        }.execute();
    }
    
    public void leaveGame()
    {
        final JDialog waitd = new WaitDialog(this);
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
            {
                client.leaveChannel();
                return null;
            }

            @Override
            protected void done()
            {
                waitd.dispose();
            }
        }.execute();
        waitd.setVisible(true);   
    }
    
    /*
     * Waits for property changes of client
     * 
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if ("state".equals(evt.getPropertyName()))
        {
            Client.State newState = (Client.State) evt.getNewValue();
            stateChanged(newState);
        }
        else if ("chatmssg".equals(evt.getPropertyName()))
        {
            Message mssg = (Message) evt.getNewValue();
            chatMssgReceived(mssg);
        }
        else if ("connected".equals(evt.getPropertyName()))
        {
            boolean con = (Boolean) evt.getNewValue();
            pchat.sendEnabled(con);
        }
        else if ("problem".equals(evt.getPropertyName()))
        {
            JOptionPane.showMessageDialog(this, evt.getNewValue().toString(),
                    "MainPanel", JOptionPane.INFORMATION_MESSAGE);
        }
        else if ("channellist".equals(evt.getPropertyName()))
        {
            String[] channels = (String[] ) evt.getNewValue();
            channelListChanged(channels);
        }
        else if ("joined".equals(evt.getPropertyName()))
        {
            String nick = evt.getNewValue().toString();
            pchat.append("***** " + nick + " joined *****");
            refreshGuests();
        }
        else if ("left".equals(evt.getPropertyName()))
        {
            String nick = evt.getNewValue().toString();
            pchat.append("***** " + nick + " left *****");
            refreshGuests();
        }
        else if ("gamedata".equals(evt.getPropertyName()))
        {
            JSONObject json = (JSONObject) evt.getNewValue();
            analizeGameData(json);
        }
        else System.err.println("Unknown property \""
                + evt.getPropertyName() + "\"");
    }
    
    void rollTheDices()
    {
        
    }
    
    private void stateChanged(Client.State newState)
    {
        remove(pcurr);
        switch (newState)
        {
            case DISC:
                add(pdisc);
                pcurr = pdisc;
                break;
            case MENU:
                add(pmenu);
                pcurr = pmenu;              
                break;
            case GAME:
                pchat = new PanelChat(this);
                resetGameFields();
                pcurr = createUninit();
                add(pcurr);
                break;
        }    
        this.getParent().repaint();
    }
    
    private void chatMssgReceived(Message mssg)
    {
        String beg = "<" + mssg.author + "> ";
        pchat.append(beg + mssg.content);
    }
    
    private void channelListChanged(String[] names)
    {
        pmenu.setHosts(names);
    }
    
    private void analizeGameData(JSONObject json)
    {
        try {
            String subtype = json.getString("subtype");
            if (subtype.equalsIgnoreCase("displayinfo"))
            {
                if (di == null) startGameGuest(json);
                else {
                    di.update(json);
                    pgame.update();
                }
            }
            else if (subtype.equalsIgnoreCase("settings"))
            {
                psetts.fromSettings(new NewGameSettings(json));
            }
            else System.err.println("MainPanel.analizeGameData: " + 
                    "I don't know what do with subtype \"" + subtype + "\".");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel createUninit()
    {
        JPanel res = new JPanel(new GridLayout(0,2));
        psetts = new SettingsForm(this);
        if (client.isHost())
        {
            refreshGuests();
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(psetts, BorderLayout.CENTER);
            JButton button = new JButton( "Start" );
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    startGameHost();
                }
            });
            panel.add(button, BorderLayout.SOUTH);
            res.add(panel);
        }
        else
        {
            psetts.setSpinnersEditable(false);
            res.add(psetts);
        }
        res.add(pchat);
        return res;
    }

    void refreshGuests()
    {
        if (!client.isHost())
            return ;
        
        String[] nicks1 = client.getGuestNicks();
        String[] nicks2 = new String[nicks1.length + 1];
        for (int i = 0; i < nicks1.length; ++i)
            nicks2[i+1] = nicks1[i];
        nicks2[0] = client.getMyNick();
        psetts.setNicks(nicks2);
        JSONObject json = psetts.getSettings().toJSON();
        try {
            json.put("type", "gamedata");
            json.put("subtype", "settings");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        try {
            client.sendData(json);
        } catch (ConnectionException ex) {
            ex.printStackTrace();
        }
    }
    
    private void startGameHost()
    {
        NewGameSettings setts = psetts.getSettings();
        di = new DisplayInfo(setts);
        di.findMe(client.getMyNick());
        pgame = new GamePanel(this, pchat, di);
        remove(pcurr);
        add(pgame);
        pcurr = pgame;
        try {
            JSONObject json = di.toJSON();
            try {
                json.put("type", "gamedata");
                json.put("subtype", "displayinfo");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            client.sendData(json);
        } catch (ConnectionException ex) {
            ex.printStackTrace();
        }
    }
    
    private void startGameGuest(JSONObject json)
    {
        try {
            di = new DisplayInfo(json);
            di.findMe(client.getMyNick());
            pgame = new GamePanel(this, pchat, di);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    
    private void resetGameFields()
    {
        di = null;
        pgame = null;
    }
    
    private DisplayInfo di;
    private PanelDisc pdisc;
    private PanelChat pchat;
    private GamePanel pgame;
    private PanelMenu pmenu;
    private SettingsForm psetts; 
    private JPanel pcurr;
    private Client client;
}
