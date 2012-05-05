
package biuromatr;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    
    private static void send(DatagramSocket ds, AddrInfo addr, String mssg)
            throws IOException
    {
        DatagramPacket dp = new DatagramPacket(new byte[0], 0);
        dp.setData(mssg.getBytes("UTF8"));
        dp.setLength(dp.getData().length);
        dp.setAddress(addr.getIaddr());
        dp.setPort(addr.getPort());
        
        
        System.out.println("Sending: " + Charset.forName("UTF8")
                .decode(ByteBuffer.wrap(dp.getData())));
        //System.out.println("Data length: " + dp.getData().length + "\n");
        
        ds.send(dp);
    }
    
    public static void send(DatagramSocket ds, AddrInfo addr, JSONObject json)
            throws IOException
    {
        send(ds, addr, json.toString());
    }

    public static JSONObject makeJSON(String type)
    {
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
        } catch (JSONException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json;
    }    
    
    public static JSONObject emptyRes(long id)
    {
        return makeRes("emptyresponse", id);
    }    
    
    public static JSONObject makeRes(String type, long id)
    {
        JSONObject res = makeJSON(type);
        try {
            res.put("res", true);
            res.put("id", id);
        } catch (JSONException ex) {
            Logger.getLogger(BiuroMatr.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
    
    public static String[] getChannels(JSONArray arr) throws JSONException
    {
        String[] channels = new String[arr.length()];
        for (int i = 0; i < channels.length; ++i)
        {
            channels[i] = arr.getString(i);
        }
        return channels;
    }

    public static boolean validateMessage(JSONObject msg)
    {
        try {
            msg.getBoolean("res");
            msg.getInt("id");
            switch(msg.getString("type")) {
                case "newclient" :
                    msg.getString("nick");
                    return true;
                case "newchannel" :
                    msg.getString("name");
                    return true;
                case "join" :
                    msg.getString("name");
                    return true;
                case "error" :
                    msg.getString("desc");
                    return true;
                case "invalidnick" :
                    msg.getString("desc");
                    return true;
                case "channellist" :
                    JSONArray arr = msg.getJSONArray("channels");
                    for(int i = 0; i < arr.length(); i++)
                    {
                        arr.getString(i);
                    }
                    return true;
                case "welcome" :
                    msg.getString("nick");
                    return true;
                case "channelrejected" :
                    msg.getString("desc");
                    return true;
                case "joinrejected" :
                    msg.getString("desc");
                    return true;
                case "address" :
                    msg.getString("nick");
                    msg.getString("address");
                    msg.getString("port");
                    return true;
                case "chat" :
                    msg.getString("mssg");
                    return true;
                case "joinaccepted" :
                case "exitaccepted" :
                case "channelaccepted" :
                case "userleft" :
                case "emptyresponse" :
                case "exit" :
                case "sendchannels" :
                case "holepunch" :
                case "echorequest" :
                case "echoresponse" :
                    return true;
            }
          return false;
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
