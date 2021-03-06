
package wtomigraj;

import static wtomigraj.Utils.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
     *  Constructs a client.
     * @param serverIA address of server.
     * @param port port on which server listens.
     * @param gameName name of game we want to play.
     */
    public Client(InetAddress serverIA, int port, String gameName)
    {        
        serverAddr = new AddrInfo(serverIA, port);
        pcs =  new PropertyChangeSupport(this);
        this.gameName = gameName;
    }
    
    /**
     * Inits fields necessary for Client to work.
     * @throws SocketException when user doesn't have right to create a socket.
     */
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
    
    private void initHandlers()
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
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleGameData(dinfo);
            }
        };
        handlers.put("gamedata", handler);
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                handleStartGame(dinfo);
            }
        };
        handlers.put("startgame", handler);
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
        state = State.UNINIT;
        iamhost = true;
        pcs.firePropertyChange("state", State.MENU, State.UNINIT);
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
        state = State.UNINIT;
        iamhost = false;
        pcs.firePropertyChange("state", State.MENU, State.UNINIT);
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
        
        if (state != State.UNINIT)
            return ;
        confirm(dinfo);
        final String nick, address;
        final int port;
        final AddrInfo ai;  
        try {
            nick = dinfo.getJson().getString("nick");
            address = dinfo.getJson().getString("address");
            port = dinfo.getJson().getInt("port");
            ai = new AddrInfo(InetAddress.getByName(address), port);
        } catch (Exception ex) {
            System.out.println("Invalid datagram with address received.");
            return ;
        }
              
        if (iamhost)
        {
            final ClientInfo ci = new ClientInfo(ai, nick, receiver);
            guests.put(nick, ci);
            addrs.put(ai, ci);
            final JSONObject json = makeJSON("holepunch");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ci.toClient.send(json, resHandler);
                        JSONObject json2 = makeJSON("joined");
                        json2.put("nick", nick);
                        propagate(json2, null);
                        pcs.firePropertyChange("joined", null, nick);
                    } catch (ConnectionException ex) {
                        ex.printStackTrace();
                        guests.remove(nick);
                        addrs.remove(ai);
                    } catch (JSONException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();    
        }
        else
        {         
            hostInfo = new ClientInfo(ai, nick, receiver);
            final JSONObject json = makeJSON("holepunch");
            new Thread( new Runnable() {
                @Override
                public void run() {
                    try {
                        hostInfo.toClient.send(json, resHandler);
                    } catch (ConnectionException ex) {
                        ex.printStackTrace();
                        hostInfo = null;
                    }
                }
            }).start();            
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
        try {
            String nick = dinfo.getJson().getString("nick");
            ClientInfo ci = guests.get(nick);
            if (ci != null)
            {
                addrs.remove(ci.ai);
                guests.remove(ci.nick);
                JSONObject json = makeJSON("left");
                json.put("nick", ci.nick);
                propagate(json, null);
                pcs.firePropertyChange("left", null, ci.nick);
            }
         } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
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
        pcs.firePropertyChange("connected", false, true);
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
        /*
         * This handler is used by guests when host informs about leave of one
         * player. When server informs host about leave of one of his guests
         * handleUserLeft is used.
         */
        confirm(dinfo);
        try {
            String nick = dinfo.getJson().getString("nick");
            pcs.firePropertyChange("left", null, nick);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }       
    
    private void handleGameData(DatagramInfo dinfo)
    {
        confirm(dinfo);
        pcs.firePropertyChange("gamedata", null, dinfo.getJson());
    }   
    
    private void handleStartGame(DatagramInfo dinfo)
    {
        confirm(dinfo);
        state = State.GAME;
        pcs.firePropertyChange("state", State.UNINIT, State.GAME);
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
        if (state == State.GAME || state == State.UNINIT)
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
    
    /**
     * Closes communication socket and turn to state DISC.
     */
    public void close()
    {
        if (state != State.MENU) return ;
        receiver.close();
        pcs.firePropertyChange("state", state, State.DISC);
        state = State.DISC;
    }
    
    /**
     * Sends to server request to start new channel.
     * @param chName channel name.
     * @param capacity capacity of channel.
     * @throws ConnectionException when contacting server failed.
     */
    public void startChannel(String chName, int capacity) throws ConnectionException
    {
        if (state != State.MENU) return ;
        JSONObject json = makeJSON("newchannel");
        try {
            json.put("name", chName);
            json.put("game", gameName);
            json.put("capacity", capacity);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        toServer.send(json, resHandler);
    }
    
    /**
     * Sends to server request to join a channel.
     * @param channelName channel name.
     * @throws ConnectionException  when contacting server failed.
     */
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
    
    /**
     * Sends to server request to get new channels.
     * @throws ConnectionException when contacting server failed.
     */
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
    
    /**
     * Sends chat message. If it is invoked by host then mssg is sent to all
     * his guests, else mssg is sent to host who will propagate it to other
     * guests.
     * @param mssg String with message.
     * @throws ConnectionException  when contacting another client failed.
     */
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
    
    /**
     * Sends data encapsulated in json object. If method is invoked by host
     * then json is sent to all his guests. Otherwise it sent to host.
     * @param json JSONObject with data.
     * @throws ConnectionException when contacting another client failed.
     */
    public void sendData(JSONObject json) throws ConnectionException
    {
        if (!iamhost)
        {
            if (hostInfo == null)
                throw new ConnectionException("You don't have connection to host.");
            hostInfo.toClient.send(json, resHandler);
        }
        else propagate(json, null);        
    }
    
    /**
     * Method is designated to be invoked by host. It sends a data in a 
     * form of a JSONObject to guest with given nick. If it is invoked by 
     * guest then it has no effect.
     * @param json JSONObject with data.
     * @param nick Nick of guest to which we are sending data.
     */
    public void sendData(final JSONObject json, String nick)
    {
        if (!iamhost) return;
        final ClientInfo ci = guests.get(nick);
        try {
            ci.toClient.send(json, resHandler);
        } catch (ConnectionException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Informs host and server about leaving channel.
     */
    public void leaveChannel()
    {
        if (!iamhost)
        {
            try {
                JSONObject json = makeJSON("quit");
                hostInfo.toClient.send(json, resHandler);
            } catch (Exception ex) {
               //we don't do anything, we can't contact host
               //but we are leaving anyway
            }
        }
        letKnowYouAreFree();
    }
    
    /**
     * This method should be used by host to inform server that we want to
     * start a game.
     */
    public void startGame()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject json = makeJSON("startgame");
                    toServer.send(json, resHandler);
                } catch (ConnectionException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
        state = State.GAME;
        pcs.firePropertyChange("state", State.UNINIT, State.GAME);
    }
    
    /**
     * Adds Property Listener.
     * @param pcl a property listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl)
    {
        pcs.addPropertyChangeListener(pcl);
    }
    
    /**
     * Returns state of client.
     * @return state of client.
     */
    public State getState()
    {
        return state;
    }

    /**
     * Returns nick of this client.
     * @return nick of this client.
     */
    public String getMyNick()
    {
        return myNick;
    }

    /**
     * Returns true if and only if this client is a host.
     * @return 
     */
    public boolean isHost()
    {
        return iamhost;
    }
    
    /**
     * It should be used by host to get nicks of his guests.
     * @return nicks of guests of this clients. If client is not a host or has
     * not guests then 0-element array is returned.
     */
    public String[] getGuestNicks()
    {
        Object[] array = guests.keySet().toArray();
        String[] res = new String[array.length];
        for(int i = 0; i < array.length; ++i)
            res[i] = array[i].toString();
        return res;
    }

    private void propagate(final JSONObject json, AddrInfo from)
    {
        int next = 0;
        Thread[] t = new Thread[guests.size() - (from == null ? 0 : 1)];
        for (final ClientInfo guest: guests.values())
        {
            if (guest.ai.equals(from))
                continue;
            t[next] = new Thread( new Runnable() {

                @Override
                public void run()
                {
                    try {
                        guest.toClient.send(json, resHandler);
                    } catch (ConnectionException ex) {
                        guest.dead = true;
                    }
                }
            } );
            t[next++].start();            
        }
        for (int i = 0; i < t.length; ++i)
            try {
                t[i].join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
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
    
    /**
     * Possible states of Client.
     */
    public enum State {
        /**
         * Disconnected.
         */
        DISC,
        /**
         * Menu mode.
         */
        MENU,
        /**
         * Uninitialized (not started) game
         */
        UNINIT,
        /**
         * (started) game.
         */
        GAME
    };
    
    /**
     * Current state of client.
     */
    private State state = State.DISC;
    
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
    private TreeMap<String, ClientInfo> guests = new TreeMap<String, ClientInfo>();
    /**
     * Map guest_address -> guest_info
     */
    private TreeMap<AddrInfo, ClientInfo> addrs = new TreeMap<AddrInfo, ClientInfo>();
    
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
    private Map<String, Handler> handlers = new TreeMap<String, Handler>();
    
    /**
     * Response handler. It is used by send method of toClient and toServer.
     */
    private Handler resHandler;
    
    /**
     * Game for each this client is used.
     */
    private final String gameName;
}
