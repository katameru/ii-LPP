
package biuromatr;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.Timer;
import java.util.Map;
import java.util.TreeMap;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static biuromatr.Utils.*;


/**
 * \"Biuro matrymonialne\" is an UDP server arranges connection between clients.
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
        initHandlers();
        receiver = new Receiver(ds);
        PropertyChangeListener rListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if ("datagram".equals(evt.getPropertyName()))
                {
                    DatagramInfo dinfo = (DatagramInfo) evt.getNewValue();
                    if (!dinfo.isResponse())
                    {
                        //received packet is a request
                        Handler h = null;
                        handleRequest(dinfo);
                    }
                }
            }
        };
        receiver.addPropertyChangeListener(rListener);   
        receiver.startListening();
        installPresenceChecker();
        try {
            System.out.println("Server is running.");
            receiver.getListening().join();
        } catch (InterruptedException ex) {
            Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                sendYouThere();
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
     * Method sends to all clients youthere packet.
     */
    private void sendYouThere()
    {
        for (final ClientInfo ci: addrs.values())
        {
            new Thread(new Runnable() {
                @Override
                public void run()
                {           
                    try {
                        ci.toClient.send("youthere|", resHandler);
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
     * youthere.
     */
    private void deleteDeadClients()
    {
        //System.out.println("Dead check: " + System.currentTimeMillis());
        List <String> deadNicks = new ArrayList<String>();
        List <AddrInfo> deadAddrs = new ArrayList<AddrInfo>();
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
     * Analyzes data in received packet.
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
                         !"newclient".equals(dinfo.getMssg()[0]) )
                        handleUnknownClient(dinfo);
                    else {
                        Handler h = handlers.get(dinfo.getMssg()[0]);
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
                String com = resp.getMssg()[0];
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
                try
                {
                    handleIAmThere(dinfo);
                } catch (IOException ex)
                {
                    Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        handlers.put("iamthere", handler);
        
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
                    handleIAmFree(dinfo);
                } catch (IOException ex)
                {
                    Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        handlers.put("iamfree", handler);
        
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
        String pre = "res|" + dinfo.getId() + "|";
        send(ds, dinfo.getSender(), pre + "idontknowyou|");
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
        String pre = "res|" + dinfo.getId() + "|";
        send(ds, dinfo.getSender(), pre + "invalidrequest|");
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
        String pre = "res|" + dinfo.getId() + "|";
        String[] tab = dinfo.getMssg();
        //tab[0] - newclient, and tab[1] is nickname
        String nick = "";
        try {
            nick = tab[1];
        } catch (ArrayIndexOutOfBoundsException ex) {
            //we doesn't have to do anything, nick stays an empty string, so it
            //will be rejected by next if statement. [it is impossible anyway]
        }
        if (!isNickOK(nick))
        {
            send(ds, dinfo.getSender(), pre + "invalidnick|");
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
            send(ds, dinfo.getSender(), pre  + "welcome|" + nick);
        }
        else if (ci.ai.equals(dinfo.getSender()))
        {   //the same client sent to us request for the same nick
            //it may mean that our confirmation packet had been lost
            send(ds, dinfo.getSender(), pre + "welcome|" + nick);
        }
        else
        {   //nick was taken by someone else
            send(ds, dinfo.getSender(), pre + "nickinuse|");
        }            
    }
    
    /**
     * Method invoked when we get packet confirming presence.
     * @param mssg message sent by client.
     * @param ai clients address.
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    private void handleIAmThere(DatagramInfo dinfo) throws IOException
    {   //we don't do anything, client would be considered dead if he
        //didn't respond.
    }    
    
    private void handleNewChannel(DatagramInfo dinfo) throws IOException 
    {
        String pre = "res|" + dinfo.getId() + "|";
        ClientInfo ci = addrs.get(dinfo.getSender());
        String nick = ci.nick;
        Channel ch = channels.get(nick);
        if (ch != null)
        {   //client already is in a channel. It may be duplicated datagram
            //or some strange error.
            send(ds, dinfo.getSender(),
                    pre + "youareinchannel|" + ch.guest.nick);
            return ;
        }
        channels.put(nick, new Channel(ci));
        send(ds, dinfo.getSender(), pre + "channelacc|");
        updateChannelString();
    }

    private void handleIAmFree(DatagramInfo dinfo) throws IOException
    {
        String pre = "res|" + dinfo.getId() + "|";
        ClientInfo ci = addrs.get(dinfo.getSender());
        leaveChannel(ci.nick); //if client wasn't in channel it has no effect.
        send(ds, dinfo.getSender(), pre + "channellist" + channelString);
    }
       
    private void handleSendChannels(DatagramInfo dinfo) throws IOException
    {        
        String pre = "res|" + dinfo.getId() + "|";
        send(ds, dinfo.getSender(), pre + "channellist" + channelString);
    }
    
    private void handleJoin(DatagramInfo dinfo) throws IOException
    {
        String pre = "res|" + dinfo.getId() + "|";
        String hostnick = "";
        try {
            hostnick = dinfo.getMssg()[1];
        } catch (ArrayIndexOutOfBoundsException ex) {
            send(ds, dinfo.getSender(), pre + "invalidrequest|");
            return ;
        }
        Channel ch = channels.get(hostnick);
        ClientInfo guest = addrs.get(dinfo.getSender());
        if (channels.get(guest.nick) != null)
        {
            if (ch != null && guest.nick.equals(ch.guest.nick))
            {  //it is duplicated request to join this channel. 
                send(ds, guest.ai, pre + "joinacc|");
            }
            else send(ds, guest.ai,
                    pre + "youareinchannel|" + channels.get(guest.nick));            
        }
        else if (ch == null) send(ds, guest.ai, pre + "nosuchchannel|");
        else if (ch.isFull())
        {
            //we already know that dinfo sender is not guest of channel ch
            send(ds, dinfo.getSender(), pre + "channelfull|");
        }
        else {
            send(ds, guest.ai, pre + "joinacc|");
            ch.setGuest(guest);
            channels.put(guest.nick, ch);
            updateChannelString();
            sendAddr(ch);
        }
    } 
    
    private void sendAddr(final Channel ch)
    {
        ClientInfo guest = ch.guest,
                   host  = ch.host;
        try {
            host.toClient.send("address|" + guest.nick + "|" + guest.ai,
                    resHandler);
        } catch (ConnectionException ex) {
            //host of channel could not be reached
            host.dead = true;
            try {
                leaveChannel(host.nick);
            } catch (IOException ex1) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return ;
        }
        
        try {
            guest.toClient.send("address|" + host.nick + "|" + host.ai, resHandler);
        } catch (ConnectionException ex) {
            guest.dead = true;
            try {
                leaveChannel(guest.nick);
            } catch (IOException ex1) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex1);
            }
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
                ch.setGuest(null); //host will wait for new iterlocutor
                try {
                    ch.host.toClient.send("guestdisc|" + nick, resHandler);
                } catch (ConnectionException ex) {
                    Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
                }
                updateChannelString();
            } 
        }        
    }
    
    private void closeChannel(Channel ch) throws IOException
    {  
        channels.remove(ch.host.nick);
        if (ch.guest != null)
        {
            channels.remove(ch.guest.nick);
            try {
                ch.guest.toClient.send("channelcanceled|" + ch.host.nick, resHandler);
            } catch (ConnectionException ex) {
                Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        updateChannelString();
    }

    private boolean isNickOK(String nick)
    {
        return nick != null && !nick.isEmpty() && nick.length() < 32;
    }
       
    private void updateChannelString()
    {
        /* TODO we should append one channel only once. Currently it is
         * accidentally correct, because channel is designated for two clients,
         * should be changed.
         */
        if (channels.isEmpty()) channelString = "|";
        else {
            channelString = "";
            for (Channel ch: channels.values())
                if (!ch.isFull()) channelString += "|" + ch.host.nick;
        }
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
    private Map <String, ClientInfo> clients = new TreeMap<String, ClientInfo>();
    
    /**
     * Map with all addresses of clients connected to server. Value set is the 
     * same as in clients.
     */   
    private Map <AddrInfo, ClientInfo> addrs = new TreeMap<AddrInfo, ClientInfo>();
    
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
    private String channelString = "|";
    
    /**
     * Map containing nicks of clients and channels in which they are.
     */
    private TreeMap <String, Channel> channels = new TreeMap<String, Channel>();
    
    private Receiver receiver;
    
    private Map<String, Handler> handlers = new TreeMap<String, Handler>();
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
    Channel(ClientInfo host)
    {
        this.host = host;
    }
    
    boolean isFull()
    {
        return guest != null;
    }
    
    void setGuest(ClientInfo guest)
    {
        this.guest = guest;
    }
    
    ClientInfo host, guest = null;
}
