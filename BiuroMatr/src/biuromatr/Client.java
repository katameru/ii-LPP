
package biuromatr;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.SocketException;
import java.util.TreeMap;
import java.util.Map;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.DatagramSocket;
import java.net.InetAddress;
import static biuromatr.Utils.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class designated for communication with server and other users.
 * @author grzes
 */
public class Client
{
    /**
     * Constructs a client.
     * @param serverIA address of server.
     * @param port servers port.
     */
    public Client(InetAddress serverIA, int port)
    {        
        serverAddr = new AddrInfo(serverIA, port);
        pcs =  new PropertyChangeSupport(this);
    }
    
    public void init() throws SocketException
    {
        ds = new DatagramSocket();        
        receiver = new Receiver(ds);
        toServer = new ReqSender(ds, receiver, serverAddr);
        PropertyChangeListener rListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if ("datagram".equals(evt.getPropertyName()))
                {
                    DatagramInfo dinfo = (DatagramInfo) evt.getNewValue();
                    if (!dinfo.isResponse()) //received packet is a request    
                        handleRequest(dinfo);                    
                }         
            }
        };
        receiver.addPropertyChangeListener(rListener);   
        receiver.startListening();
        initHandlers(); //no effect if handlers have been already initialized
        state = State.DISC;
        pcs.firePropertyChange("state", State.UNINIT, State.DISC);
    }
    
    protected void initHandlers()
    {
        if (hInit) return ;
        hInit = true;
        
        resHandler = new Handler() {
            @Override
            public void handle(DatagramInfo resp)
            {
                if (resp.getMssg()[0].isEmpty())
                    return ;
                Handler h = handlers.get(resp.getMssg()[0]);
                if (h != null) h.handle(resp);
                else System.err.println("I don't know what to do"
                        + " with response " + resp.getMssg()[0]);
            }
        };
        
        Handler handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleWelcome(dinfo);
            }
        };
        handlers.put("welcome", handler);
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleInvalidNick();
            }
        };
        handlers.put("invalidnick", handler);     
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleNickInUse(dinfo);
            }
        };        
        handlers.put("nickinuse", handler);           
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleYouThere(dinfo);
            }
        };        
        handlers.put("youthere", handler);   
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {   
                handleChannelList(dinfo);
            }
        };
        handlers.put("channellist", handler);  
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleChannelAcc(dinfo);
            }
        };
        handlers.put("channelacc", handler); 
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleChannelFull(dinfo);
            }
        };
        handlers.put("channelfull", handler);        
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleNoSuchChannel(dinfo);
            }
        };
        handlers.put("nosuchchannel", handler);        
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleJoinAcc(dinfo);
            }
        };
        handlers.put("joinacc", handler);     
              
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleAddress(dinfo);
            }
        };
        handlers.put("address", handler);     
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleYouAreInChannel(dinfo);
            }
        };
        handlers.put("youareinchannel", handler);     
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleChannelCanceled(dinfo);
            }
        };
        handlers.put("channelcanceled", handler);     
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleGuestDisc(dinfo);
            }
        };
        handlers.put("guestdisc", handler);     
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleIDontKnowYou(dinfo);
            }
        };
        handlers.put("idontknowyou", handler);
                
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleInvalidRequest(dinfo);
            }
        };
        handlers.put("invalidrequest", handler);
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleHolePunch(dinfo);
            }
        };
        handlers.put("holepunch", handler);
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleICanHearYou(dinfo);
            }
        };
        handlers.put("icanhearyou", handler);
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleChat(dinfo);
            }
        };
        handlers.put("chat", handler);
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleQuit(dinfo);
            }
        };
        handlers.put("quit", handler);
    }
    
    private void handleRequest(final DatagramInfo dinfo)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Handler h = null;
                if ( dinfo.getSender().equals(clientAddr) ||
                     dinfo.getSender().equals(serverAddr) )
                {
                    h = handlers.get(dinfo.getMssg()[0]);
                    if (h != null) h.handle(dinfo);
                    else {
                        System.err.println("AAAI don't know what to do "
                            + "with message " + dinfo.getMssg()[0]);
                    }
                }                  
            }
        }).start();        
    }
    
    private void handleWelcome(DatagramInfo dinfo)
    {
        myNick = dinfo.getMssg()[1];
        resetInterlocutor();
        pcs.firePropertyChange("state", State.DISC, State.MENU);
        state = State.MENU;
        try {
            toServer.send("sendchannels|", resHandler);
        } catch (ConnectionException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleInvalidNick()
    {
        pcs.firePropertyChange("problem", "", "Given nick is invalid.");
    }
    
    private void handleNickInUse(DatagramInfo dinfo)
    {
        pcs.firePropertyChange("problem", "", "Given nick is already used.");
    }
    
    private void handleYouThere(DatagramInfo dinfo)
    {
        confirm(dinfo, "iamthere|");
    }
    
    private void handleChannelList(DatagramInfo dinfo)
    {
        String[] channels = subarray(dinfo.getMssg(),
                1, dinfo.getMssg().length);
        pcs.firePropertyChange("channellist", null, channels);
    }
    
    private void handleChannelAcc(DatagramInfo dinfo)
    {
        state = State.CHAT;
        iamhost = true;
        pcs.firePropertyChange("state", State.MENU, State.CHAT);
    }
    
    private void handleChannelFull(DatagramInfo dinfo)
    {
        pcs.firePropertyChange("problem", "", "Chosen channel if full.");
    }
    
    private void handleNoSuchChannel(DatagramInfo dinfo)
    {
        pcs.firePropertyChange("problem", "", "There is no such channel.");
    }
    
    private void handleJoinAcc(DatagramInfo dinfo)
    {
        state = State.CHAT;
        iamhost = false;
        pcs.firePropertyChange("state", State.MENU, State.CHAT);
    }
    
    private void handleAddress(DatagramInfo dinfo)
    {
        confirm(dinfo);
        if (clientAddr == null)
        {            
            try {
                interlocutor = dinfo.getMssg()[1];
                clientAddr = AddrInfo.fromString(dinfo.getMssg()[2]);
                toClient = new ReqSender(ds, receiver, clientAddr);
                toClient.send("holepunch|", resHandler);
            } catch (Exception ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void handleYouAreInChannel(DatagramInfo dinfo)
    {
        letKnowYouAreFree();
        pcs.firePropertyChange("problem", "", "Must return to menu mode"
                + " due to error in communication with server.");
    }
    
    private void handleChannelCanceled(DatagramInfo dinfo)
    {
        confirm(dinfo);
        letKnowYouAreFree();
        pcs.firePropertyChange("problem", "", "Host left.");
    }
    
    private void handleGuestDisc(DatagramInfo dinfo)
    {
        confirm(dinfo);
        if (state == State.CHAT && interlocutor != null)
        {
            resetInterlocutor();
        }
    }
    
    private void handleIDontKnowYou(DatagramInfo dinfo)
    {
        pcs.firePropertyChange("state", state, State.DISC);
        state = State.DISC;
        pcs.firePropertyChange("problem", "", "Connecion rejected by server.");        
    }
    
    private void handleInvalidRequest(DatagramInfo dinfo)
    {
        pcs.firePropertyChange("problem", "", "Host states that message \""
                + glue(dinfo.getMssg()) + "\" is invalid.");
    }
    
    private void handleHolePunch(DatagramInfo dinfo)
    {
        confirm(dinfo, "icanhearyou|");
    }
    
    private void handleICanHearYou(DatagramInfo dinfo)
    {
        if (!clientConnected)
        {
            clientConnected = true;
            pcs.firePropertyChange("connected", false, true);
        }
    }
    
    private void handleChat(DatagramInfo dinfo)
    {
        confirm(dinfo);
        pcs.firePropertyChange("chatmssg", null, dinfo.getMssg()[1]);        
    }
    
    private void handleQuit(DatagramInfo dinfo)
    {
        /*
         * In fact quit message is unnecessary - server will send the
         * message about leaving anyway.
         */
        confirm(dinfo);
        if (state == State.CHAT && interlocutor != null)
        {
            resetInterlocutor();
        }
    }
    
    private void confirm(DatagramInfo dinfo)
    {
        try {
            send(ds, dinfo.getSender(), "res|" + dinfo.getId() + "|");
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void confirm(DatagramInfo dinfo, String desc)
    {
        try {
            send(ds, dinfo.getSender(), "res|" + dinfo.getId() + "|" + desc);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void resetInterlocutor()
    {
        clientAddr = null;
        toClient = null;
        clientConnected = false;
        interlocutor = null;
        pcs.firePropertyChange("connected", true, false);
    }
    
    private void letKnowYouAreFree()
    {
        if (state == State.CHAT)
        {
            try {
                toServer.send("iamfree|", resHandler);
            } catch (ConnectionException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }   
            resetInterlocutor();   
            pcs.firePropertyChange("state", state, State.MENU);
            state = State.MENU;
        }        
    }
        
    /**
     * Registers on the server with given nick.
     * @param nick nickname which we want to use on the server.
     * @throws ConnectionException if we can't contact server.
     */
    public void start(String nick) throws ConnectionException
    {     
        if (state != State.DISC) return ;
        toServer.send("newclient|" + nick, resHandler);
    }
    
    public void close()
    {
        if (state != State.MENU) return ;
        receiver.close();
        pcs.firePropertyChange("state", state, State.DISC);
        state = State.DISC;
    }
    
    public void startChannel() throws ConnectionException
    {
        if (state != State.MENU) return ;
        toServer.send("newchannel|", resHandler);
    }
    
    public void join(String hostname) throws ConnectionException
    {
        if (state != State.MENU) return ;
        toServer.send("join|" + hostname, resHandler);          
    }
    
    public void refreshChannels() throws ConnectionException
    {
        if (state != State.MENU) return ;
        toServer.send("sendchannels|", resHandler);      
    }
    
    public void sendChatMssg(String mssg) throws ConnectionException
    {
        if (!clientConnected)
            throw new ConnectionException("You don't have connection to any client.");
        toClient.send("chat|" + mssg, resHandler);
    }
    
    public void leaveChat()
    {
        if (clientConnected)
        {
            try {
                toClient.send("quit|", resHandler);
            } catch (ConnectionException ex) {
               //we don't do anything, he is dead but we are leaving anyway
            }
        }
        letKnowYouAreFree();
    }
    
    
    public void addPropertyChangeListener(PropertyChangeListener pcl)
    {
        pcs.addPropertyChangeListener(pcl);
    }
    
    public State getState()
    {
        return state;
    }

    public String getMyNick()
    {
        return myNick;
    }

    public String getInterlocutor()
    {
        return interlocutor;
    }

    public boolean isHost()
    {
        return iamhost;
    }
        
    public enum State {
        UNINIT, DISC, MENU, CHAT
    };
    
    /**
     * Current state of client.
     */
    private State state = State.UNINIT;
    
    /**
     * Socket for sending and receiving datagrams.
     */
    private DatagramSocket ds;
    
    /**
     * Object for receiving datagrams.
     */
    private Receiver receiver;
    
    /**
     * Some of received packets have important for other objects and pcs will
     * let them know.
     */
    private PropertyChangeSupport pcs;
    
    /**
     * Address of server.
     */
    private final AddrInfo serverAddr;
    
    /**
     * Sends datagrams to server.
     */
    private ReqSender toServer;
    
    /**
     * Address of other client application, with which we are communicating.
     */
    private AddrInfo clientAddr = null;
    
    /**
     * Sends datagrams to client.
     */
    private ReqSender toClient;    
    
    /**
     * True when interlocutor sends packet icanhearyou.
     */
    private boolean clientConnected = false;
    
    /**
     * When state is CHAT then iamhost field indicates if this client is host
     * of the channel.
     */
    private boolean iamhost = false;
    /**
     * Our nick on the server.
     */
    private String myNick;
    
    /**
     * Name of out interlocutor;
     */
    private String interlocutor;
    
    /**
     * True when handlers where initialized.
     */
    private boolean hInit = false;
     
    /**
     * Map communicate -> Handler.
     */
    protected Map<String, Handler> handlers = new TreeMap<String, Handler>();
    
    /**
     * Response handler. It is used by send method of toClient and toServer.
     */
    Handler resHandler;
}
