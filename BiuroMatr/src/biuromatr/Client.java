
package biuromatr;

import static biuromatr.Utils.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
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
        resetInterlocutor();
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
                interlocutor = dinfo.getJson().getString("nick");
                clientAddr = AddrInfo.fromJsonInfo(dinfo.getJson().getString("address"), dinfo.getJson().getInt("port"));
                toClient = new ReqSender(ds, receiver, clientAddr);
                JSONObject json = makeJSON("holepunch");
                toClient.send(json, resHandler);
            } catch (Exception ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
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
        if (state == State.CHAT && interlocutor != null)
        {
            resetInterlocutor();
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
        if (!clientConnected)
        {
            clientConnected = true;
            pcs.firePropertyChange("connected", false, true);
        }
    }
    
    private void handleChat(DatagramInfo dinfo)
    {
        confirm(dinfo);
        String mssg = "";
        try {
            mssg = dinfo.getJson().getString("mssg");
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        pcs.firePropertyChange("chatmssg", null, mssg);        
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
                JSONObject json = makeJSON("exit");
                toServer.send(json, resHandler);
            } catch (Exception ex) {
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
        JSONObject json = makeJSON("newclient");
        try {
            json.put("nick", nick);
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
    
    public void startChannel() throws ConnectionException
    {
        if (state != State.MENU) return ;
        JSONObject json = makeJSON("newchannel");
        try {
            json.put("name", myNick);
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
        toServer.send(json, resHandler);      
    }
    
    public void sendChatMssg(String mssg) throws ConnectionException
    {
        if (!clientConnected)
            throw new ConnectionException("You don't have connection to any client.");
        JSONObject json = makeJSON("chat");
        try {
            json.put("mssg", mssg);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        toClient.send(json, resHandler);
    }
    
    public void leaveChat()
    {
        if (clientConnected)
        {
            try {
                JSONObject json = makeJSON("quit");
                toClient.send(json, resHandler);
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
    protected Map<String, Handler> handlers = new TreeMap<>();
    
    /**
     * Response handler. It is used by send method of toClient and toServer.
     */
    Handler resHandler;
}
