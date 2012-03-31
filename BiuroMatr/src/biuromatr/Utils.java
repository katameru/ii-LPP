
package biuromatr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 *
 * @author grzes
 */
public class Utils
{
    
    public static void showPacketInfo(DatagramPacket dp)
    {
        System.out.println("Message received from " + dp.getAddress());
        System.out.println("From port: " + dp.getPort());
        System.out.println("Data length: " + dp.getLength());
        String content = Charset.forName("UTF8")
                .decode(ByteBuffer.wrap(
                dp.getData(), 0, dp.getLength())).toString();
        System.out.println("Content: " + content + "\n");        
    }
    
    public static String readData(byte[] tab, int length)
    {
        return Charset.forName("UTF8")
                .decode(ByteBuffer.wrap(tab, 0, length)).toString();
    }
    
    public static void send(DatagramSocket ds, AddrInfo addr, String mssg)
            throws UnsupportedEncodingException, IOException
    {
        DatagramPacket dp = new DatagramPacket(new byte[0], 0);
        dp.setData(mssg.getBytes("UTF8"));
        dp.setLength(dp.getData().length);
        dp.setAddress(addr.getIaddr());
        dp.setPort(addr.getPort());
        
        
        System.out.println("Sending: " + Charset.forName("UTF8")
                .decode(ByteBuffer.wrap(dp.getData())));
        System.out.println("Data length: " + dp.getData().length + "\n");
        
        ds.send(dp);
    }
}
