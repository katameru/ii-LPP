
package clientframe;

import biuromatr.Client;
import biuromatr.ConnectionException;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 *
 * @author grzes
 */
public class ClientFrame extends JFrame implements PropertyChangeListener
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
        pdisc = new PanelDisc(this);
        pmenu = new PanelMenu(this);
        pchat = new PanelChat(this);
        pcurr = pdisc;
        setTitle("Biuro Matrymonialne");
        
        add(pdisc);
    }
    
    
    void connect(InetAddress ia, int port, final String nick)
    {
        client = new Client(ia, port);
        try {
            client.init();
        } catch (SocketException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
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
                    JOptionPane.showMessageDialog(ClientFrame.this,
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
    
    void disconnect()
    {
        client.close();
    }

    void startChannel()
    { 
        final JDialog waitd = new WaitDialog(this);
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
            {
                try {
                    client.startChannel();
                } catch (ConnectionException ex) {
                    JOptionPane.showMessageDialog(ClientFrame.this,
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
                    JOptionPane.showMessageDialog(ClientFrame.this,
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

    void refreshChannels()
    { 
        final JDialog waitd = new WaitDialog(this);
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
            {
                try {
                    client.refreshChannels();
                } catch (ConnectionException ex) {
                    JOptionPane.showMessageDialog(ClientFrame.this,
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

    void sendChatMssg(final String text)
    {
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
            {
                try {
                    pchat.sendEnabled(false);
                    client.sendChatMssg(text);
                } catch (ConnectionException ex) {
                    JOptionPane.showMessageDialog(ClientFrame.this,
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
    
    void leaveChat()
    {
        final JDialog waitd = new WaitDialog(this);
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
            {
                client.leaveChat();
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
            String mssg = evt.getNewValue().toString();
            chatMssgReceived(mssg);
        }
        else if ("connected".equals(evt.getPropertyName()))
        {
            boolean con = (Boolean) evt.getNewValue();
            pchat.sendEnabled(con);
            if (client.isHost())
            {
                if (con) pchat.append("*** CLIENT CONNECTED ***");
                else pchat.append("*** CLIENT DISCONNECTED ***");
            }
        }
        else if ("problem".equals(evt.getPropertyName()))
        {
            JOptionPane.showMessageDialog(this, evt.getNewValue().toString(),
                    "ClientFrame", JOptionPane.INFORMATION_MESSAGE);
        }
        else if ("channellist".equals(evt.getPropertyName()))
        {
            String[] channels = (String[] ) evt.getNewValue();
            channelListChanged(channels);
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
            case CHAT:
                pchat.clear();
                add(pchat);
                pcurr = pchat;
                break;
        }    
        Dimension d = getSize();
        setSize(d.width+1, d.height+1);
    }
    
    private void chatMssgReceived(String mssg)
    {
        String beg = "<" + client.getInterlocutor() + "> ";
        pchat.append(beg + mssg);
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
