
package biuromatr;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static biuromatr.Utils.*;

/**
 * Class designated for listening on one DatagramSocket for incoming packets.
 * It does not confirm received packets.
 * @author grzes
 */
public class Receiver
{    
    public Receiver(DatagramSocket ds)
    {
        this.ds = ds;
        pcs = new PropertyChangeSupport(this);
    }
    
    /**
     * Creates thread for handling arriving datagrams.
     */
    public void startListening()
    {     
        if (started) return ;
        started = true;
        listening = new Thread( new Runnable() {
            @Override
            public void run()
            {
                listen();
            }
        });
        listening.start();        
    }
    
    public Thread getListening()
    {
        return listening;
    }
    
    /**
     * In a loop waits for incoming datagrams until this Receiver is closed.
     */
    void listen()
    {
        DatagramPacket dp = new DatagramPacket(new byte[maxL], maxL);
        while (!closed)
        {
            try
            {
                ds.receive(dp);
                DatagramInfo dinfo;
                try {
                    dinfo = new DatagramInfo(dp);
                    System.out.println("Received: " + dinfo.getType());
                    if (dinfo.isResponse())
                        pcs.firePropertyChange("response", null, dinfo);
                    else pcs.firePropertyChange("request", null, dinfo);
                } catch (InvalidDataException ex) {
                    System.err.println("An invalid datagram received.");
                    showPacketInfo(dp);
                }
            } catch (Exception ex)
            {
                if (!ds.isClosed())
                    Logger.getLogger(Client.class.getName())
                            .log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Closes socket and turns off thread listening for incoming datagrams.
     */
    public void close()
    {
        closed = true;
        pcs.firePropertyChange("closed", false, true);
        ds.close();
    }  
    
    /**
     * Adds PropertyChangeListener.
     * @param pcl Listener interested in incoming packages.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl)
    {
        pcs.addPropertyChangeListener(pcl);
    }

    public DatagramSocket getDs()
    {
        return ds;
    }
    
    /**
     * Socket on which receiver listens.
     */
    private DatagramSocket ds;
    
    /**
     * Lets know about: closing of Receiver and/or incoming packets.
     */
    private PropertyChangeSupport pcs;
    
    /**
     * Maximal buffer of datagram.
     */
    private final int maxL = 65536;
    
    /**
     * True after closing Receiver.
     */
    private boolean closed = false;
    
    /**
     * True when receiver is listening on its port.
     */
    private boolean started = false;
    
    private Thread listening;
}
