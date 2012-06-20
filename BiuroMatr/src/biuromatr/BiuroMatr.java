
package biuromatr;

import static biuromatr.Utils.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * \"Biuro matrymonialne\" is an UDP server which 
 * arranges connection between clients.
 * 
 * @author grzes
 */
public class BiuroMatr implements Runnable
{
    /** Main method just starts the server thread.
     * 
     * @param args it takes one argument, which is port number used by server.
     * If it is not given then 6666 is taken.
     */
    public static void main(String[] args)
    {
        try
        {
            int port = 6666;
            if (args.length > 0)
            {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException ex) {
                    System.err.println("Argument should be an integer value.");
                    System.exit(1);
                }
                if (port < 1024 || port > 65535) {
                    System.err.println("Port number should lie in a range "
                            + "1024-65535");
                    System.exit(1);
                }
            }
            new Thread(new BiuroMatr(port)).start();
        } catch (SocketException ex)
        {
            Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Constructor creates socket object bound to port 6666.
     * @throws SocketException 
     */
    BiuroMatr(int port) throws SocketException
    {
        ds = new DatagramSocket(port);   
    }
    
    /**
     * Implementation of method from interface Runnable. In an infinite loop
     * waits for new messages.
     */
    @Override
    public void run()
    {
        initGameChannels();
        initHandlers();
        receiver = new Receiver(ds);
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
       // installPresenceChecker();
        try {
            System.out.println("Server is running.");
            receiver.getListening().join();
        } catch (InterruptedException ex) {
            Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void initGameChannels()
    {
        channelNames.put("SimpleChat", new TreeSet<String>());
    }
    
    /**
     * Installs two timers. First sends to every client request to confirm
     * his presence. Second deletes clients, who didn't answer for 
     * some time.
     */
    private void installPresenceChecker()
    {
        TimerTask task1 = new TimerTask() {
            @Override
            public void run() {
                sendEchoRequest();
            }
        };
        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                deleteDeadClients();
            }            
        };
        Timer t1 = new Timer(), t2 = new Timer();
        t1.schedule(task1, 5000, youTherePeriod);
        t2.schedule(task2, 1000, youTherePeriod);
    }    
    
    /**
     * Method sends to all clients echorequest packet.
     */
    private void sendEchoRequest()
    {
        for (final ClientInfo ci: addrs.values())
        {
            new Thread(new Runnable() {
                @Override
                public void run()
                {           
                    try {
                        ci.toClient.send( Utils.makeJSON("echorequest"),
                                          resHandler );
                    } catch (ConnectionException ex) {
                        System.out.println("Setting dead");
                        ci.dead = true;
                    }
                }
            }).start();
        }        
    }
    
    /**
     * Method iterates trough all clients and checks if they keep answering to
     * echorequest.
     */
    private void deleteDeadClients()
    {
        //System.out.println("Dead check: " + System.currentTimeMillis());
        List <String> deadNicks = new ArrayList<>();
        List <AddrInfo> deadAddrs = new ArrayList<>();
        for (ClientInfo ci: clients.values()) {
            if (ci.dead)
            {
                deadNicks.add(ci.nick);
                deadAddrs.add(ci.ai);
            }
        }
        synchronized (this) {
            for (AddrInfo ai: deadAddrs)
                addrs.remove(ai);
            for (String nick: deadNicks)
                clients.remove(nick);
        }
        try {   
            for (String nick: deadNicks)
                leaveChannel(nick);
        } catch (IOException ex) {
            Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
        
    /**
     * Analyzes data from received packet in a new thread.
     * @param dp received packet.
     */
    private void handleRequest(final DatagramInfo dinfo)
    {
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    if ( addrs.get(dinfo.getSender()) == null &&
                         !"newclient".equals(dinfo.getType()) )
                        handleUnknownClient(dinfo);
                    else {
                        Handler h = handlers.get(dinfo.getType());
                        if (h == null) handleInvalidRequest(dinfo);
                        else h.handle(dinfo);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(BiuroMatr.class.getName())
                            .log(Level.SEVERE, null, ex);                    
                }
            }
        }).start();
    }
    
    protected void initHandlers()
    {            
        resHandler = new Handler()
        {
            @Override
            public void handle(DatagramInfo resp)
            {
                String com = resp.getType();
                if (com.isEmpty()) return;
                Handler h = handlers.get(com);
                if (h != null) h.handle(resp);
                else System.err.println("I don't know what to do"
                        + " with response " + com);
            }
        };        
        
        
        Handler handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                try
                {
                    handleNewClient(dinfo);
                } catch (IOException ex)
                {
                    Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        handlers.put("newclient", handler);
        
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
                try
                {
                    handleNewChannel(dinfo);
                } catch (IOException ex)
                {
                    Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        handlers.put("newchannel", handler);
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                try
                {
                    handleExit(dinfo);
                } catch (IOException ex)
                {
                    Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        handlers.put("exit", handler);
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                try
                {
                    handleSendChannels(dinfo);
                } catch (IOException ex)
                {
                    Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        handlers.put("sendchannels", handler);
        
        handler = new Handler() {
            @Override
            public void handle(DatagramInfo dinfo)
            {
                try
                {
                    handleJoin(dinfo);
                } catch (IOException ex)
                {
                    Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        handlers.put("join", handler);
    }    
  
    /**
     * Method invoked when we get packet from address which is not on the
     * list of our clients addresses.
     * @param mssg message sent by client.
     * @param ai clients address.
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    private void handleUnknownClient(DatagramInfo dinfo) throws IOException
    {
        JSONObject res = makeRes("error", dinfo.getId());
        try {
            res.put("desc", "You didn't register.");
        } catch (JSONException ex) {
            Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
        }
        send(ds, dinfo.getSender(), res);
    }    
    
    /**
     * Method invoked when we get packet from address which is not on the
     * list of our clients addresses.
     * @param mssg message sent by client.
     * @param ai clients address.
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    private void handleInvalidRequest(DatagramInfo dinfo) throws IOException
    {
        JSONObject res = makeRes("error", dinfo.getId());
        try {
            res.put("desc", "Invalid request.");
        } catch (JSONException ex) {
            Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
        }
        send(ds, dinfo.getSender(), res);
    }

    /**
     * Method invoked when new client registers.
     * @param mssg message sent by client.
     * @param ai clients address.
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    private void handleNewClient(DatagramInfo dinfo) throws IOException
    {
        JSONObject res = emptyRes(dinfo.getId());
        String nick = null;
        String game = null;
        try {
            nick = dinfo.getJson().getString("nick");
            game = dinfo.getJson().getString("game");
        } catch (JSONException ex) {
            try {
                res.put("type", "error");
                res.put("desc", "Request 'newclient' should"
                        + " contain fields 'nick' and 'game'.");                
            } catch (JSONException ex2) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex2);
            }
            send(ds, dinfo.getSender(), res);
            return ;
        }
        if (!isNickOK(nick))
        {
            try {
                res.put("type", "invalidnick");
                res.put("desc", "Given nick is invalid.");                
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }
            send(ds, dinfo.getSender(), res);
            return ;
        }
        Set<String> chNames = channelNames.get(game);
        if (chNames == null)
        {
            try {
                res.put("type", "error");
                res.put("desc", "Unknown game " + game + ".");                
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }
            send(ds, dinfo.getSender(), res);
            return ;            
        }
        
        ClientInfo ci = clients.get(nick);
        if (ci == null)
        {   //there is no client with such nick so far.
            ClientInfo newCi = new ClientInfo(dinfo.getSender(), nick, receiver);
            synchronized(this) {
                clients.put(nick, newCi);
                addrs.put(dinfo.getSender(), newCi);
            }
            try {
                res.put("type", "welcome");
                res.put("nick", nick);
                res.put("channels", chNames);
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }
            send(ds, dinfo.getSender(), res);
        }
        else if (ci.ai.equals(dinfo.getSender()))
        {   //the same client sent to us request for the same nick
            //it may mean that our confirmation packet had been lost
            try {
                res.put("type", "welcome");
                res.put("nick", nick);
                res.put("channels", channelNames);
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }
            send(ds, dinfo.getSender(), res);
        }
        else
        {   //nick was taken by someone else
            try {
                res.put("type", "invalidnick");
                res.put("desc", "Nick is already taken.");
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }
            send(ds, dinfo.getSender(), res);
        }            
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
    
    private void handleNewChannel(DatagramInfo dinfo) throws IOException 
    {
        JSONObject res = emptyRes(dinfo.getId());
        ClientInfo ci = addrs.get(dinfo.getSender());
        
        /******************* Extracting datagram content ***********/
        String chName = "";
        int capacity = 0;
        String game = "";
        try {
            capacity = dinfo.getJson().getInt("capacity");
            game = dinfo.getJson().getString("game");
            chName = dinfo.getJson().getString("name");
        } catch (JSONException ex) {
            try {
                res.put("type", "error");
                res.put("desc", "NewChannel request should "
                        + "contain fields 'name', 'capacity' and 'game'.");
                send(ds, dinfo.getSender(), res);
            } catch (JSONException ex1) { }
            return ;
        }   
        
        /************ Checking if client is already in a channel ***********/
        Channel ch = channels.get(ci.nick);
        if (ch != null)
        {   //client already is in a channel. It may be duplicated datagram
            //or some strange error.
            try {              
                res.put("type", "channelrejected");
                res.put("desc", "You are in channel already.");
                res.put("name", ch.name);
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }            
            send(ds, dinfo.getSender(), res);
            return ;
        }
        
        /**************** Checking if channel name is valid ***************/
        if (!isNickOK(chName))
        {
            try {
                res.put("type", "channelrejected");
                res.put("desc", "Given channel name is invalid.");
                res.put("name", chName); 
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }               
            send(ds, dinfo.getSender(), res);
            return ;
        }
        if (getChannelByName(chName) != null)
        {
            try {
                res.put("type", "channelrejected");
                res.put("desc", "Given channel name is already taken.");
                res.put("name", chName);   
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }             
            send(ds, dinfo.getSender(), res);
            return ;
        }
        
        /**************** And at last starting channel ******************/
        startChannel(dinfo, ci, chName, capacity, game);
    }
    
    private void startChannel(DatagramInfo dinfo, ClientInfo ci, String chName,
                              int capacity, String game) throws IOException
    {     
        JSONObject res = emptyRes(dinfo.getId());
        Set<String> names = channelNames.get(game);
        if (names == null)
        {
            try {
                res.put("type", "error");
                res.put("desc", "Unknown game " + game);   
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }
            send(ds, dinfo.getSender(), res);
        }
        else 
        {
            channels.put(ci.nick, new Channel(ci, chName, game, capacity));
            try {
                res.put("type", "channelaccepted");
                res.put("name", chName);   
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }
            send(ds, dinfo.getSender(), res);
            names.add(chName);        
        }
    }

    private void handleExit(DatagramInfo dinfo) throws IOException
    {
        JSONObject res = makeRes("exitaccepted", dinfo.getId());
        ClientInfo ci = addrs.get(dinfo.getSender());
        leaveChannel(ci.nick); //if client wasn't in channel it has no effect.
        try {
            String game = dinfo.getJson().getString("game");
            res.put("channels", channelNames.get(game));
        } catch (JSONException ex) {
            Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
        }
        send(ds, dinfo.getSender(), res);
    }
       
    private void handleSendChannels(DatagramInfo dinfo) throws IOException
    {        
        JSONObject res = makeRes("channellist", dinfo.getId());
        
        try {
            Set<String> names = null;
            String game = "";
            try {
                game = dinfo.getJson().getString("game");
                names = channelNames.get(game);
                if (names == null) throw new NullPointerException();
            } catch (JSONException ex) {
                res.put("error", channelNames);
                res.put("desc", "Request 'channellist' should contain field 'game'");
                send(ds, dinfo.getSender(), res);
                return ;
            } catch (NullPointerException ex) {
                res.put("error", channelNames);
                res.put("desc", "Unknown game " + game);
                send(ds, dinfo.getSender(), res);
                return ;
            }
            res.put("channels", names);
            send(ds, dinfo.getSender(), res);
        } catch (JSONException ex) {
            Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleJoin(DatagramInfo dinfo) throws IOException
    {
        JSONObject res = emptyRes(dinfo.getId());
        String name;
        try {
            name = dinfo.getJson().getString("name");
        } catch (JSONException ex) {
            try {
                res.put("type", "invalidrequest");
                res.put("desc", "Channel name is invalid.");
            } catch (JSONException ex1) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex1);
            }
            send(ds, dinfo.getSender(), res);
            return ;
        }
        
        Channel ch = getChannelByName(name);
        ClientInfo guest = addrs.get(dinfo.getSender());
        if (channels.get(guest.nick) != null)
        {
            //client is already in a channel        
            try {
                if (ch != null && ch.guests.contains(guest))
                {  //it is duplicated request to join this channel. 
                    res.put("type", "joinaccepted");
                    res.put("name", name);
                    send(ds, guest.ai, res);
                }
                else
                {
                    res.put("type", "joinrejected");
                    res.put("desc", "You are already in a channel.");
                    res.put("name", channels.get(guest.nick));
                    send(ds, guest.ai, res);     
                }        
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);            
            }
        }
        else if (ch == null) 
        {
            try {
                res.put("type", "joinrejected");
                res.put("desc", "No such channel.");
                res.put("name", name);
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }
            send(ds, guest.ai, res);
        }
        else if (ch.isFull())
        {
            //we already know that dinfo sender is not guest of channel ch
            try {
                res.put("type", "joinrejected");
                res.put("desc", "Channel is full.");
                res.put("name", name);
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }
            send(ds, guest.ai, res);
        }
        else
        {
            try {
                res.put("type", "joinaccepted");
                res.put("name", name);
            } catch (JSONException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }
            send(ds, guest.ai, res);            
            ch.setGuest(guest);
            channels.put(guest.nick, ch);
            sendAddrToEachOther(guest, ch.host);
        }
    } 
    
    /**
     * Sends each others addresses to host and client of given channel.
     * @param ch channel.
     */
    private void sendAddrToEachOther(final ClientInfo guest, final ClientInfo host)
    {
        new Thread(new Runnable() {

            @Override
            public void run()
            {
                sendAddr(host, guest);
            }
        }).start();
        
        sendAddr(guest, host);
    }
    
    /**
     * Sends address of snd to fst.
     * @param fst Client who will receive address of snd.
     * @param snd Client who's address is being sent.
     */
    private void sendAddr(ClientInfo fst, ClientInfo snd)
    {
        JSONObject json = makeJSON("address");
        try {
            json.put("nick", snd.nick);
            json.put("address", snd.ai.getAdressString());
            json.put("port", snd.ai.getPort());
        } catch (JSONException ex) {
            Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            fst.toClient.send(json, resHandler);
        } catch (ConnectionException ex) {
            //fst could not be reached
            fst.dead = true;
            try {
                leaveChannel(fst.nick);
            } catch (IOException ex1) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return ;
        }        
    }
    
    private void leaveChannel(String nick) throws IOException
    {
        Channel ch = channels.get(nick);      
        if (ch != null)
        {   //client was in a channel    
            if (nick.equals(ch.host.nick))
            {   //client was host of this channel
                closeChannel(ch);
            }
            else {
                channels.remove(nick);
                ch.removeGuest(clients.get(nick)); //host will wait for new iterlocutor
                try {
                    JSONObject json = makeJSON("userleft");
                    try {
                        json.put("nick", nick);
                    } catch (JSONException ex) {
                        Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    ch.host.toClient.send(json, resHandler);
                } catch (ConnectionException ex) {
                    Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
        }        
    }
    
    private void closeChannel(Channel ch) throws IOException
    {  
        channels.remove(ch.host.nick);
        channelNames.remove(ch.name);
        for(ClientInfo guest : ch.guests)
        {
            channels.remove(guest.nick);
            try {
                JSONObject json = makeJSON("channelcanceled");
                guest.toClient.send(json, resHandler);
            } catch (ConnectionException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private Channel getChannelByName(String name)
    {
        for (Channel ch: channels.values())
        {
            if (ch.name.equals(name)) {
                return ch;
            }
        }
        return null;
    }

    private boolean isNickOK(String nick)
    {
        return nick != null && !nick.isEmpty() && nick.length() < 32;
    }
       
    public void close()
    {
        receiver.close();
    }
    
    /**
     * Socket used by server.
     */
    private DatagramSocket ds;
    
    /**
     * Map containing nicknames of clients with their addresses. Value set
     * is the same as in addrs.
     */
    private Map <String, ClientInfo> clients = new TreeMap<>();
    
    /**
     * Map with all addresses of clients connected to server. Value set is the 
     * same as in clients.
     */   
    private Map <AddrInfo, ClientInfo> addrs = new TreeMap<>();
    
    /**
     * Capacity of the buffer of data in datagram packets.
     */
    private final int maxL = 65536;
    
    /**
     * Period in miliseconds between sending youthere packages.
     */
    private final long youTherePeriod = 30000;
    
    /**
     * String containing nicks of clients who opened chat channels. It is
     * updated every time when any change happens.
     */
    private Map<String, Set<String>> channelNames = new TreeMap<>();
    
    /**
     * Map containing nicks of clients and channels in which they are.
     */
    private TreeMap <String, Channel> channels = new TreeMap<>();
    
    private Receiver receiver;
    
    private Map<String, Handler> handlers = new TreeMap<>();
    private Handler resHandler;
}
/**
 * Class containing information about client.
 * @author grzes
 */
class ClientInfo
{
    ClientInfo(AddrInfo ai, String nick, Receiver receiver)
    {
        this.ai = ai;
        this.nick = nick;
        dead = false;
        toClient = new ReqSender(receiver.getDs(), receiver, ai);
    }
    ReqSender toClient;
    final AddrInfo ai;
    final String nick;
    boolean dead;
}

class Channel
{
    Channel(ClientInfo host, String name, String game, int capacity)
    {
        this.host = host;
        this.name = name;
        this.capacity = capacity;
        this.guests = new HashSet<>();
    }
    
    boolean isFull()
    {
        return guests.size() == capacity - 1;
    }
    
    void setGuest(ClientInfo guest)
    {
        this.guests.add(guest);
    }
    
    boolean removeGuest(ClientInfo guest)
    {
        return guests.remove(guest);
    }
    
    ClientInfo host;
    Set<ClientInfo> guests;
    String name; 
    String game;
    int capacity;
}
