
package biuromatr;

import java.io.IOException;
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
     /**
     * Prints to standard output some information about datagram.
     * @param dp datagram packet about which we want to show information.
     */
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
    
    /**
     * Reads data from datagram buffer, assuming that data is an UTF8 String.
     * @param tab buffer of datagram.
     * @param length length of data.
     * @return String representing message packed in this datagram.
     */
    public static String readData(byte[] tab, int length)
    {
        return Charset.forName("UTF8")
                .decode(ByteBuffer.wrap(tab, 0, length)).toString();
    }
    
    public static void send(DatagramSocket ds, AddrInfo addr, String mssg)
            throws IOException
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
    
    public static String glue(String[] parts)
    {
        String res = "";
        if (parts.length == 1) res = parts[0] + "|";
        else {
            for (int i = 0; i < parts.length; ++i)
                res += parts[i] + (i == parts.length-1 ? "" : "|");
        }
        return res;
    }
    
    public static String[] subarray(String[] arr, int beg, int end)
    {
        int l = end - beg;
        String[] res = new String[l];
        for (int i = 0; i < l; ++i)
            res[i] = arr[i+beg];
        return res;
    }
}
