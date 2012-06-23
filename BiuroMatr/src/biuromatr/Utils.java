
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
    
    /**
     * Sends a string.
     * @param ds Socket used to send datagram. 
     * @param addr Address to which we send datagram.
     * @param mssg Contents of datagram.
     * @throws IOException 
     */
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
    
    /**
     * Sends a json using ds to addr.
     * @param ds Socket used to send datagram. 
     * @param addr Address to which we send datagram.
     * @param json JSONObject that is being sent.
     * @throws IOException 
     */
    public static void send(DatagramSocket ds, AddrInfo addr, JSONObject json)
            throws IOException
    {
        send(ds, addr, json.toString());
    }
    
    /**
     * Returns subarray of an original array. New array is located, which 
     * contains references to objects from original array. Indices copied are
     * from the rand [beg, end).
     * @param arr Array from which we want a fragment.
     * @param beg First index.
     * @param end end-1 is index of last taken element.
     * @return newly allocated array.
     */
    public static String[] subarray(String[] arr, int beg, int end)
    {
        int l = end - beg;
        String[] res = new String[l];
        for (int i = 0; i < l; ++i)
            res[i] = arr[i+beg];
        return res;
    }
    
    /**
     * Creates a JSONObject with given type of message.
     * @param type type of message.
     * @return JSONObject.
     */
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
        
    /**
     * Creates a JSONObject with type of message "emptyresponse" responding to 
     * datagram with given id. Usable to 
     * confirm delivery of a message which doesn't really need response.
     * @param id id of message to which we are responding.
     * @return JSONObject.
     */
    public static JSONObject emptyRes(long id)
    {
        return makeRes("emptyresponse", id);
    }    
        
    /**
     * Creates a JSONObject with given type of message responding to 
     * datagram with given id.
     * @param type type of message.
     * @param id id of message to which we are responding.
     * @return JSONObject.
     */
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
    
    /**
     * Takes a JSONArray object, and assumes that array contain names of
     * channels. Returns that names as a an array of Strings.
     * @param arr JSONArray of channel names.
     * @return String array of channel names.
     * @throws JSONException 
     */
    public static String[] getChannels(JSONArray arr) throws JSONException
    {
        String[] channels = new String[arr.length()];
        for (int i = 0; i < channels.length; ++i)
        {
            channels[i] = arr.getString(i);
        }
        return channels;
    }

   /* public static boolean validateMessage(JSONObject msg)
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
    }*/
}
