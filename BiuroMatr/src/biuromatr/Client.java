
package biuromatr;

import static biuromatr.Utils.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

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
    public Client(InetAddress serverIA, int port, String gameName)
    {        
        serverAddr = new AddrInfo(serverIA, port);
        pcs =  new PropertyChangeSupport(this);
        this.gameName = gameName;
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
                if ("request".equals(evt.getPropertyName()))
                {
                    DatagramInfo dinfo = (DatagramInfo) evt.getNewValue();
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
                if (resp.getType().isEmpty())
                    return ;
                Handler h = handlers.get(resp.getType());
                if (h != null) h.handle(resp);
                else System.err.println("I don't know what to do"
                        + " with response " + resp.getType());
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
                handleInvalidNick(dinfo);
            }
        };
        handlers.put("invalidnick", handler);            
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleEchoRequest(dinfo);
            }
        };        
        handlers.put("echorequest", handler);   
        
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
                handleExitAccpeted(dinfo);
            }
        };
        handlers.put("exitaccepted", handler);        
      
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleChannelAccepted(dinfo);
            }
        };
        handlers.put("channelaccepted", handler); 
  
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleChannelRejected(dinfo);
            }
        };
        handlers.put("channelrejected", handler);        
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleJoinAccepted(dinfo);
            }
        };
        handlers.put("joinaccepted", handler);     
                     
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleJoinRejected(dinfo);
            }
        };
        handlers.put("joinrejected", handler);     
              
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
                handleChannelCanceled(dinfo);
            }
        };
        handlers.put("channelcanceled", handler);     
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleUserLeft(dinfo);
            }
        };
        handlers.put("userleft", handler);     
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleError(dinfo);
            }
        };
        handlers.put("error", handler);
        
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
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleEmptyResponse(dinfo);
            }
        };
        handlers.put("emptyresponse", handler);
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleJoined(dinfo);
            }
        };
        handlers.put("joined", handler);
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleLeft(dinfo);
            }
        };
        handlers.put("left", handler);
    }
    
    private void handleRequest(final DatagramInfo dinfo)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Handler h = null;
                
                if ( knownSender(dinfo) )
                {
                    h = handlers.get(dinfo.getType());
                    if (h != null) h.handle(dinfo);
                    else {
                        System.err.println("AAAI don't know what to do "
                            + "with message " + dinfo.getType());
                    }
                }                  
            }
        }).start();        
    }
    
    private void handleWelcome(DatagramInfo dinfo)
    {
        try {
            myNick = dinfo.getJson().getString("nick");
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        resetConnections();
        pcs.firePropertyChange("state", State.DISC, State.MENU);
        state = State.MENU;
        handleChannelList(dinfo);
    }
    
    private void handleInvalidNick(DatagramInfo dinfo)
    {
        String ans = "";
        try {
            ans = dinfo.getJson().getString("desc");
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        pcs.firePropertyChange("problem", "", "Nick was not accepted."
                + "Server answered: \"" + ans + "\".");
    }
    
    private void handleEchoRequest(DatagramInfo dinfo)
    {
        confirm(dinfo);
    }
    
    private void handleChannelList(DatagramInfo dinfo)
    {
        String[] channels = new String[0];
        try {
            channels = getChannels(dinfo.getJson().getJSONArray("channels"));
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        pcs.firePropertyChange("channellist", null, channels);
    }
        
    private void handleExitAccpeted(DatagramInfo dinfo)
    {
        handleChannelList(dinfo);
    }
    
    private void handleChannelAccepted(DatagramInfo dinfo)
    {
        state = State.CHAT;
        iamhost = true;
        pcs.firePropertyChange("state", State.MENU, State.CHAT);
        pcs.firePropertyChange("connected", false, true);
    }
    
    private void handleChannelRejected(DatagramInfo dinfo)
    {
        String ans = "";
        try {
            ans = dinfo.getJson().getString("desc");
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        pcs.firePropertyChange("problem", "", "Channel was not accepted."
                + "Server answered: \"" + ans + "\".");
    }
        
    private void handleJoinRejected(DatagramInfo dinfo)
    {
        String ans = "";
        try {
            ans = dinfo.getJson().getString("desc");
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        pcs.firePropertyChange("problem", "", "Couldn't join."
                + "Server answered: \"" + ans + "\".");
    }
        
    /**
     * Method invoked when we get packet which doesn't need extra data
     * in answer.
     * @param mssg message sent by client.
     * @param ai clients address.
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    private void handleEmptyResponse(DatagramInfo dinfo)
    {   //we don't do anything, the only important thing is that request
        //has been delivered
    }    
    
    private void handleJoinAccepted(DatagramInfo dinfo)
    {
        //First comes packet joinaccepted. After that should come address of host.
        state = State.CHAT;
        iamhost = false;
        pcs.firePropertyChange("state", State.MENU, State.CHAT);
    }
    
    private void handleAddress(DatagramInfo dinfo)
    {
        /* If we are host we are in state CHAT for sure, because always first
         * will come 'channelaccepted' and after that some client may want to
         * join us (maybe except some really patologic cases).
         * If we are guest sometimes packet 'joinaccepted' will come after
         * 'address'. I dont have a really good idea what do in such case.
         * In code below i simply ignore packet with address. Hopefully soon
         * will come 'joinaccepted' and we will make a use from next 'address'
         * packet (server sends few of them). 
         */
        if (state != State.CHAT)
            return ;
        confirm(dinfo);
        String nick, address, port;
        AddrInfo ai;  
        try {
            nick = dinfo.getJson().getString("nick");
            address = dinfo.getJson().getString("address");
            port = dinfo.getJson().getString("port");
            ai = new AddrInfo(InetAddress.getByName(address), Integer.parseInt(port));
        } catch (JSONException | UnknownHostException | NumberFormatException ex) {
            System.out.println("Invalid datagram with address received.");
            return ;
        }
              
        if (iamhost)
        {
            try {
                ClientInfo ci = new ClientInfo(ai, nick, receiver);
                guests.put(nick, ci);
                addrs.put(ai, ci);
                JSONObject json = makeJSON("holepunch");
                ci.toClient.send(json, resHandler);
            } catch (ConnectionException ex) {
                guests.remove(nick);
                addrs.remove(ai);
                return ;
            }            
            try {
                JSONObject json = makeJSON("joined");
                json.put("nick", nick);
                propagate(json, null);
            } catch (JSONException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            pcs.firePropertyChange("joined", null, nick);
        }
        else
        {         
            try {
                hostInfo = new ClientInfo(ai, nick, receiver);
                JSONObject json = makeJSON("holepunch");
                hostInfo.toClient.send(json, resHandler);
                pcs.firePropertyChange("connected", false, true);
            } catch (ConnectionException ex) {
                hostInfo = null;
            }
        }
    }
    
    private void handleChannelCanceled(DatagramInfo dinfo)
    {
        confirm(dinfo);
        letKnowYouAreFree();
        pcs.firePropertyChange("problem", "", "Host left.");
    }
    
    private void handleUserLeft(DatagramInfo dinfo)
    {
        confirm(dinfo);
        ClientInfo ci = addrs.get(dinfo.getSender());
        if (ci != null)
        {
            addrs.remove(ci.ai);
            guests.remove(ci.nick);
             try {
                JSONObject json = makeJSON("left");
                json.put("nick", ci.nick);
                propagate(json, null);
            } catch (JSONException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            pcs.firePropertyChange("left", null, ci.nick);
        }
    }
    
    private void handleError(DatagramInfo dinfo)
    {
        pcs.firePropertyChange("state", state, State.DISC);
        state = State.DISC;
        pcs.firePropertyChange("problem", "", "Connecion rejected by server.");        
    }
    
    private void handleHolePunch(DatagramInfo dinfo)
    {
        confirm(dinfo, "icanhearyou");
    }
    
    private void handleICanHearYou(DatagramInfo dinfo)
    {
        //we do nothing, it just means our holepunch got through
    }
    
    private void handleChat(DatagramInfo dinfo)
    {
        confirm(dinfo);
        String mssg = "", author = "";
        try {
            mssg = dinfo.getJson().getString("mssg");
            author = dinfo.getJson().getString("author");
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        pcs.firePropertyChange("chatmssg", null, new Message(mssg, author));        
        if (iamhost) propagate(dinfo.getJson(), dinfo.getSender());
    }
    
    private void handleQuit(DatagramInfo dinfo)
    {
        /*
         * In fact quit message is unnecessary - server will send the
         * message about leaving anyway. I assume quit messages are sent
         * only by guests.
         */
        confirm(dinfo);
        ClientInfo ci = addrs.get(dinfo.getSender());
        if (ci != null)
        {
            addrs.remove(ci.ai);
            guests.remove(ci.nick);
             try {
                JSONObject json = makeJSON("left");
                json.put("nick", ci.nick);
                propagate(json, null);
            } catch (JSONException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            pcs.firePropertyChange("left", null, ci.nick);
        }
    }
    
    private void handleJoined(DatagramInfo dinfo)
    {
        confirm(dinfo);
        try {
            String nick = dinfo.getJson().getString("nick");
            pcs.firePropertyChange("joined", null, nick);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
    
    private void handleLeft(DatagramInfo dinfo)
    {
        confirm(dinfo);
        try {
            String nick = dinfo.getJson().getString("nick");
            pcs.firePropertyChange("left", null, nick);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
        
    private void confirm(DatagramInfo dinfo)
    {
        try {
            JSONObject res = emptyRes(dinfo.getId());
            send(ds, dinfo.getSender(), res);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void confirm(DatagramInfo dinfo, String type)
    {
        try {
            JSONObject res = makeRes(type, dinfo.getId());
            send(ds, dinfo.getSender(), res);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void letKnowYouAreFree()
    {
        if (state == State.CHAT)
        {
            try {
                JSONObject json = makeJSON("exit");
                json.put("game", gameName);
                toServer.send(json, resHandler);
            } catch (Exception ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }   
            resetConnections();   
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
        JSONObject json = makeJSON("newclient");
        try {
            json.put("nick", nick);
            json.put("game", gameName);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        toServer.send(json, resHandler);
    }
    
    public void close()
    {
        if (state != State.MENU) return ;
        receiver.close();
        pcs.firePropertyChange("state", state, State.DISC);
        state = State.DISC;
    }
    
    public void startChannel(String chName) throws ConnectionException
    {
        if (state != State.MENU) return ;
        JSONObject json = makeJSON("newchannel");
        try {
            json.put("name", chName);
            json.put("game", gameName);
            json.put("capacity", "32");
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        toServer.send(json, resHandler);
    }
    
    public void join(String channelName) throws ConnectionException
    {
        if (state != State.MENU) return ;
        JSONObject json = makeJSON("join");
        try {
            json.put("name", channelName);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        toServer.send(json, resHandler);          
    }
    
    public void refreshChannels() throws ConnectionException
    {
        if (state != State.MENU) return ;
        JSONObject json = makeJSON("sendchannels");
        try {
            json.put("game", gameName);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        toServer.send(json, resHandler);      
    }
    
    public void sendChatMssg(String mssg) throws ConnectionException
    {
        JSONObject json = makeJSON("chat");
        try {
            json.put("mssg", mssg);
            json.put("author", myNick);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!iamhost)
        {
            if (hostInfo == null)
                throw new ConnectionException("You don't have connection to host.");
            hostInfo.toClient.send(json, resHandler);
        }
        else propagate(json, null);
    }
    
    public void leaveChannel()
    {
        if (!iamhost)
        {
            try {
                JSONObject json = makeJSON("quit");
                hostInfo.toClient.send(json, resHandler);
            } catch (ConnectionException ex) {
               //we don't do anything, we can't contact host
               //but we are leaving anyway
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

    public boolean isHost()
    {
        return iamhost;
    }

    private void propagate(final JSONObject json, AddrInfo from)
    {
        for (final ClientInfo guest: guests.values())
        {
            if (guest.ai.equals(from))
                continue;
            new Thread( new Runnable() {

                @Override
                public void run()
                {
                    try {
                        guest.toClient.send(json, resHandler);
                    } catch (ConnectionException ex) {
                        guest.dead = true;
                    }
                }
            } ).start();                    
        }
    }
    
    private void resetConnections()
    {
        hostInfo = null;
        guests.clear();
        addrs.clear();
    }
    
    private boolean knownSender(DatagramInfo dinfo)
    {
        if (hostInfo != null && dinfo.getSender().equals(hostInfo.ai))
            return true;
        else return addrs.containsKey(dinfo.getSender()) ||
                    dinfo.getSender().equals(serverAddr);
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
     * Object containg host nick, ReqSender to host, etc.
     */
    private ClientInfo hostInfo;
    
    /**
     * Map guest_nick -> guest_info
     */
    private TreeMap<String, ClientInfo> guests = new TreeMap<>();
    /**
     * Map guest_address -> guest_info
     */
    private TreeMap<AddrInfo, ClientInfo> addrs = new TreeMap<>();
    
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
     * True when handlers where initialized.
     */
    private boolean hInit = false;
     
    /**
     * Map communicate -> Handler.
     */
    protected Map<String, Handler> handlers = new TreeMap<>();
    
    /**
     * Response handler. It is used by send method of toClient and toServer.
     */
    Handler resHandler;
    
    /**
     * Game for each this client is used.
     */
    final String gameName;
}
