
package biuromatr;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReqSender is derivation for Request Sender.
 * It is a class for sending datagrams which demand response or at
 * least datagram delivery confirmation.
 * @author grzes
 */
public class ReqSender implements PropertyChangeListener
{
    public ReqSender(DatagramSocket ds, Receiver receiver, AddrInfo addr)
    {
        this.ds = ds;
        this.addr = addr;
                              
        receiver.addPropertyChangeListener(this);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if ("datagram".equals(evt.getPropertyName()))
        {
            DatagramInfo dinfo = (DatagramInfo) evt.getNewValue();
            if (!addr.equals(dinfo.getSender()))
                return ;
            if (dinfo.isResponse())
            {
                if (dinfo.getId() == currNr)
                {
                    confirmed = true;
                    response = dinfo;
                }
            }
        }
    }
    
    /**
     * Sends message. String "id|" (where id is identification number of packet)
     * is added at the beginning of message. It blocks the thread until 
     * confirmation is received or method gives up.
     * @param mssg
     * @throws ConnectionException 
     */
    public void send(String mssg, Handler rHandler) throws ConnectionException
    {
        if (ds.isClosed()) throw new ConnectionException("Socket is closed.");
        currNr = number++;
        confirmed = false;
        for (int i = 0; i < retrials && !confirmed; ++i)
        {
            try {
                Utils.send(ds, addr, currNr + "|" + mssg);
            } catch (IOException ex) {
                throw new ConnectionException(ex.getMessage());
            }
            synchronized(this)
            {
                try {
                    wait( waitTime );
                } catch (InterruptedException ex) {
                    Logger.getLogger(ReqSender.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        currNr = -1;
        if (!confirmed)
        {
            System.out.println("Sending \"" + mssg + "\"\n to " + addr + " failed.");
            throw new ConnectionException("Delivery of packet was"
                + " not confirmed");
        }
        else rHandler.handle(response);
    }

    public int getRetrials()
    {
        return retrials;
    }

    public void setRetrials(int retrials)
    {
        this.retrials = retrials;
    }

    public long getWaitTime()
    {
        return waitTime;
    }

    public void setWaitTime(long waitTime)
    {
        this.waitTime = waitTime;
    }
    
    /**
     * Socket used for sending datagrams.
     */
    private DatagramSocket ds;
    
    /**
     * Address to which this ReqSender is sending datagrams.
     */
    private AddrInfo addr;
    
    /**
     * Next identification number of packet;
     */
    private static long number = 1;    
    
     /**
      * How many times we send the same packet, unless we get confirmation that
      * it has been received.
      */
     private int retrials = 5;
     
     /**
      * Time between trials of sending.
      */
     private long waitTime = 3000; 
     
     /** 
      * Identification of packet which is currently being sent.
      */
     private long currNr;
     
     /**
      * True when packet which is currently being sent is confirmed to be
      * delivered.
      */
     private boolean confirmed = false;
     
     /**
      * Respoponse.
      */
     private DatagramInfo response;
}
