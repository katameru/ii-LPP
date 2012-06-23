
package clientframe;

import biuromatr.Client;
import biuromatr.ConnectionException;
import biuromatr.Message;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**
 *
 * @author grzes
 */
public class SimpleChat extends JApplet implements PropertyChangeListener, GameInterface
{
    public SimpleChat()
    {
        pdisc = new PanelDisc(this);
        pmenu = new PanelMenu(this);
        pchat = new PanelChat(this);
        pcurr = pdisc;
        
        add(pdisc);
    }
    
    public void init()
    {
        
    }
    
    public void connect(InetAddress ia, int port, final String nick)
    {
        client = new Client(ia, port, "SimpleChat");
        try {
            client.init();
        } catch (SocketException ex) {
            Logger.getLogger(SimpleChat.class.getName()).log(Level.SEVERE, null, ex);
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
                    JOptionPane.showMessageDialog(SimpleChat.this,
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
                    client.startChannel(client.getMyNick(), 32);
                } catch (ConnectionException ex) {
                    JOptionPane.showMessageDialog(SimpleChat.this,
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
                    JOptionPane.showMessageDialog(SimpleChat.this,
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
                    JOptionPane.showMessageDialog(SimpleChat.this,
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
                    JOptionPane.showMessageDialog(SimpleChat.this,
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
                    "SimpleChat", JOptionPane.INFORMATION_MESSAGE);
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
        }
        else if ("left".equals(evt.getPropertyName()))
        {
            String nick = evt.getNewValue().toString();
            pchat.append("***** " + nick + " left *****");
        }
        else System.err.println("Unknown property \""
                + evt.getPropertyName() + "\"");
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
            case UNINIT:
                pchat.clear();
                add(pchat);
                pcurr = pchat;
                break;
            case GAME:
                System.err.println("SimpleChat nie ma trybu gry...");
        }    
        revalidate();
        repaint();
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
    
    private PanelDisc pdisc;
    private PanelChat pchat;
    private PanelMenu pmenu;
    private JPanel pcurr;
    private Client client;
}
