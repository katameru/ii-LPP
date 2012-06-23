
package iibiznes.frame;

import biuromatr.Client;
import biuromatr.ConnectionException;
import biuromatr.Message;
import biuromatr.Utils;
import clientframe.GameInterface;
import clientframe.PanelChat;
import clientframe.PanelDisc;
import clientframe.PanelMenu;
import clientframe.WaitDialog;
import iibiznes.game.Game;
import iibiznes.game.GameIO;
import iibiznes.game.NewGameSettings;
import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author grzes
 */
public class MainPanel extends JPanel
                       implements PropertyChangeListener, GameInterface
{
    MainPanel()
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
            if (client.isHost())
            {
                if (client.getState() == Client.State.UNINIT) refreshGuests();
                else if (client.getState() == Client.State.GAME) playerLeft(nick);
            }
        }
        else if ("gamedata".equals(evt.getPropertyName()))
        {
            JSONObject json = (JSONObject) evt.getNewValue();
            analizeGameData(json);
        }
        else System.err.println("Unknown property \""
                + evt.getPropertyName() + "\"");
    }
    
    void rollTheDicesPressed()
    {
        if (client.isHost())
        {
            updateGameLogic();
            turnStuff();
        }
        else
        {
            try {
                pgame.enableRoll(false);
                JSONObject json = Utils.makeJSON("gamedata");
                json.put("subtype", "roll");
                json.put("nick", client.getMyNick());
                client.sendData(json);
            } catch (JSONException | ConnectionException ex) {
                ex.printStackTrace();
                pgame.enableRoll(true);
            }
        }
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
    
    private void updateGameLogic()
    {
        game.rollTheDices();
        game.updateDisplay(di);
        pgame.update();
        sendDisplayInfo();
    }
    
    private void stateChanged(Client.State newState)
    {
        switch (newState)
        {
            case DISC:
                setCurrentPanel(pdisc);
                break;
            case MENU:
                setCurrentPanel(pmenu);      
                break;
            case UNINIT:
                resetGameFields();
                JPanel uninitPanel = createUninit();
                setCurrentPanel(uninitPanel);
                break;
            case GAME:
                //guest will do proper initialization while receiving first displayinfo
                if (client.isHost()) startGameHost();
                else setCurrentPanel(new JPanel());
        }    
        revalidate();
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
                if (di == null) firstDIPacket(json);
                else {
                    di.update(json);
                    pgame.update();
                }
            }
            else if (subtype.equalsIgnoreCase("yourturn"))
            {
                pgame.enableRoll(true);
            }
            else if (subtype.equalsIgnoreCase("settings"))
            {
                psetts.fromSettings(new NewGameSettings(json));
            }
            else if (subtype.equalsIgnoreCase("diary"))
            {
                addToDiary(json);
            }
            else if (subtype.equalsIgnoreCase("roll"))
            {
                rollReceived(json);
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
                    client.startGame();
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
        pchat = new PanelChat(this);
        res.add(pchat);
        return res;
    }
    
    private void startGameHost()
    {        
        NewGameSettings setts = psetts.getSettings();
        di = new DisplayInfo(setts);
        di.findMe(client.getMyNick());
        pgame = new GamePanel(this, pchat, di);
        setCurrentPanel(pgame);
        
        game = new Game(setts);
        createGameIO();
        sendStartToClients();
        turnStuff();
        game.start();
    }
    
    private void firstDIPacket(JSONObject json)
    {
        try {
            di = new DisplayInfo(json);
            di.findMe(client.getMyNick());
            pgame = new GamePanel(this, pchat, di);
            setCurrentPanel(pgame);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    
    private void setCurrentPanel(JPanel panel)
    {
        remove(pcurr);
        add(panel);
        pcurr = panel;
        revalidate();
    }
    
    private void resetGameFields()
    {
        di = null;
        game = null;
        pgame = null;
    }
    
    private void sendDisplayInfo()
    {
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
    
    private void sendStartToClients()
    {
        final JSONObject json1 = Utils.makeJSON("startgame");
        final JSONObject json2 = di.toJSON();
        try {
            json2.put("type", "gamedata");
            json2.put("subtype", "displayinfo");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        final String[] nicks  = client.getGuestNicks();
        Thread[] t = new Thread[nicks.length];
        for (int i = 0; i < nicks.length; ++i)
        {
            final String nick = nicks[i];
            t[i] = new Thread( new Runnable() {
                @Override
                public void run()
                {
                    client.sendData(json1, nick);
                    client.sendData(json2, nick);
                }
            } );
            t[i].start();
        }        
        for (int i = 0; i < nicks.length; ++i)
        {
            try {
                t[i].join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void turnStuff()
    {
        final String nick = game.curr().name;
        if (client.getMyNick().equals(nick))
        {
            //It is hosts turn.
            pgame.enableRoll(true);
        }
        else {
            final JSONObject json = Utils.makeJSON("gamedata");
            try {
                json.put("subtype", "yourturn");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            new Thread( new Runnable() {
                @Override
                public void run() {
                    client.sendData(json, nick);
                }
            }).start();
        }
    }
    
    private void playerLeft(String nick)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private void createGameIO()
    {
        gameIO = new GameIO(pgame.diary, client);
        gameIO.setGame(game);
        game.setGameIO(gameIO);
    }
 
    private void addToDiary(JSONObject json)
    {
        try {
            StyledDocument doc = pgame.diary.getStyledDocument();
            String mssg = json.getString("mssg");
            int playerNr = json.getInt("playerNr");
            Color c = Color.WHITE;
            try { c = di.colors[playerNr]; }
                catch (ArrayIndexOutOfBoundsException ex) {}
            doc.insertString(doc.getLength(),
                    mssg + "\n", doc.getStyle(c.toString()));
        } catch (JSONException  | BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private Game game;
    private GameIO gameIO;
    private DisplayInfo di;
    private PanelDisc pdisc;
    private PanelChat pchat;
    private GamePanel pgame;
    private PanelMenu pmenu;
    private SettingsForm psetts; 
    private JPanel pcurr;
    private Client client;

    private void rollReceived(JSONObject json)
    {
        if (!client.isHost()) return ;
        String nick;
        try {
            nick = json.getString("nick");
            if (game.curr().getName().equals(nick))
                rollTheDicesPressed();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }            
    }
}
