
package biuromatr;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static biuromatr.Utils.*;

/**
 *
 * @author grzes
 */
public class Client
{
    Client() throws Exception
    {
        myId = System.currentTimeMillis();
        serverAddr = new AddrInfo(InetAddress.getByName("localhost"), 6666);
        ds = new DatagramSocket();    
        pcs =  new PropertyChangeSupport(this);
    }
    
    private class Handle implements Runnable
    {
        public Handle(boolean fromServer, String mssg)
        {
            this.fromServer = fromServer;
            this.mssg = mssg;
        }
        
        @Override
        public void run()
        {
            try
            {
                if (fromServer) handleSrvrMssg(mssg);
                else handleClientMssg(mssg);
            } catch (Exception ex)
            {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        boolean fromServer;
        String mssg;
    }
    
    public void start() throws Exception
    {               
        new Thread( new Runnable() {
            @Override
            public void run()
            {
                listen();
            }
        }).start();
            
        contactServer();
        if (!srvrRes) throw new Exception("Could not connect to server.");
        
    }
    
    private void contactServer() throws UnknownHostException, IOException
    {
        for (int i = 0; i < 10 && !srvrRes; ++i)
        {
            send(ds, serverAddr, "|newclient|" + myId);
            synchronized (this) {
                try
                {
                    wait(2000);
                } catch (InterruptedException ex)
                {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    void listen()
    {
        DatagramPacket dp = new DatagramPacket(new byte[maxL], maxL);
        while (true)
        {
            try
            {
                ds.receive(dp);
                showPacketInfo(dp);
                String mssg = readData(dp.getData(), dp.getLength());
                new Thread(new Handle(dpFromServer(dp), mssg)).start();
            } catch (Exception ex)
            {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
        
    private boolean dpFromServer(DatagramPacket dp)
    {
        return dp.getAddress().equals(serverAddr.getIaddr()) &&
                dp.getPort() == serverAddr.getPort();
    }
    
    synchronized private void handleSrvrMssg(String mssg) throws Exception
    {
        String[] tab = mssg.split(":");
        srvrRes = true;
        if (tab[0].equalsIgnoreCase("HELLO")) {}
        else if (tab[0].equalsIgnoreCase("AddrInfo"))
            connect(mssg);
        else System.err.println("I don't know what to with message \"" + mssg + "\".");
    }
    
    synchronized private void handleClientMssg(String mssg) throws Exception
    {
        if (clientAddr == null)
            return;
        String lower = mssg.toLowerCase();
        if (lower.startsWith("|holepunch|"))
        {
            send(ds, clientAddr, "|icanhearyou|");
        }
        else if (lower.startsWith("|icanhearyou|"))
        {
            if (!connected)
            {
                connected = true;
                pcs.firePropertyChange("connected", false, true);
            }
        }
        else if (lower.startsWith("|youthere|"))
        {
            send(ds, clientAddr, "|iamhere|");
        }
        else if (lower.startsWith("|iamhere|"))
        {
            //TODO
            //we need to update last time our interlocutor was still connected
        }
        else if (lower.startsWith("|chat|"))
        {
            pcs.firePropertyChange("mssg", null, mssg.substring(6));
        }
        else System.err.println("I don't know what to with message \"" + mssg + "\".");
    }
    
    void write(String mssg) throws Exception
    {
        if (clientAddr == null)
            throw new Exception("You don't have connection to any client.");
        send(ds, clientAddr, "|chat|" + mssg);        
    }
    
    
    private void connect(String addrInfoStr) throws Exception
    {
        send(ds, serverAddr, "|thx|");
        clientAddr = AddrInfo.fromString(addrInfoStr);
        for (int i = 0; i < 10 && !connected; ++i)
        {
            send(ds, clientAddr, "|holepunch|");
            synchronized (this)
            {
                wait(2000);
            }
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl)
    {
        pcs.addPropertyChangeListener(pcl);
    }
        
    final int maxL = 65536;
    DatagramSocket ds;
    PropertyChangeSupport pcs;
    final long myId;
    final AddrInfo serverAddr;
    AddrInfo clientAddr = null;
    boolean srvrRes = false, //server responded
            connected = false; //connected to second client
}
