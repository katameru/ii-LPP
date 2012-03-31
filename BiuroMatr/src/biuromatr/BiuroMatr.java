
package biuromatr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static biuromatr.Utils.*;

/**
 * Biuro matrymonialne is an UDP server, waiting for two clients. It sends them
 * each other addresses. After that they can communicate with each other. 
 * 
 * @author grzes
 */
public class BiuroMatr implements Runnable
{
    public static void main(String[] args)
    {
        try
        {
            new Thread(new BiuroMatr()).start();
        } catch (SocketException ex)
        {
            Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    BiuroMatr() throws SocketException
    {
        ds = new DatagramSocket(6666);        
    }
    
    @Override
    public void run()
    {
        try
        {
            System.out.println("Server is running.\n");
            while (!finished)
            {
                int maxL = 65536;
                DatagramPacket dp = new DatagramPacket(new byte[maxL], maxL);
                ds.receive(dp);
                showPacketInfo(dp);
                handleMssg(dp);
            }
        } catch (Exception ex)
        {
            Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleMssg(DatagramPacket dp) throws UnsupportedEncodingException, IOException
    {
        String mssg = readData(dp.getData(), dp.getLength());
        AddrInfo ai = new AddrInfo(dp.getAddress(), dp.getPort());
        if (mssg.startsWith("|newclient|"))
        {
            if (clients >= 2) return;
            if (clients == 0)
            {
                addr[0] = ai;
                send(ds, addr[0], "HELLO");
                clients = 1;
            }
            else if (clients == 1)
            {
                if (addr[0].equals(ai)) send(ds, addr[0], "HELLO");
                else {
                    addr[1] = ai;
                    send(ds, addr[1], "HELLO");
                    clients = 2;
                    sendAddr();
                }
            }
        }        
        else if (mssg.startsWith("|thx|"))
        {
            if (ai.equals(addr[0])) got[0] = true;
            else if (ai.equals(addr[1])) got[1] = true;
        }
        else System.err.println("I don't know what to with message \"" + mssg + "\"");
    }

    private void sendAddr()
    {
        new Thread( new Runnable() {
                @Override
                public void run()
                {
                    try
                    {
                        for (int i = 0; i < 10 && !(got[0] && got[1]); ++i)
                        {
                            if (!got[0]) send(ds, addr[0], addr[1].toString());
                            if (!got[1]) send(ds, addr[1], addr[0].toString());
                            try {
                                synchronized(BiuroMatr.this)
                                {
                                    wait(waitTime);
                                }
                            } catch (Exception ex) {}                                
                        }   
                        finished = true;
                    } catch (Exception ex)
                    {
                        Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } ).start();
    }
    
    DatagramSocket ds;
    AddrInfo[] addr = new AddrInfo[2];
    boolean[]  got = new boolean[2];
    int clients = 0;
    boolean finished = false;
    final int waitTime = 5000;
}
