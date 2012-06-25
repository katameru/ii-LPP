
package wtomigraj;

import java.net.DatagramPacket;
import org.json.JSONException;
import org.json.JSONObject;


/**
 *
 * @author grzes
 */
public class DatagramInfo
{
    /**
     * Extracts data from datagram and creates a DatagramInfo object.
     * @param dp
     * @throws InvalidDataException 
     */
    public DatagramInfo (DatagramPacket dp) throws InvalidDataException
    {
        sender = new AddrInfo(dp.getAddress(), dp.getPort());
        String jsonString = Utils.readData(dp.getData(), dp.getLength());
        try {
            json = new JSONObject(jsonString);
            type = json.getString("type");
            response = json.getBoolean("res");
            id = json.getLong("id");
        } catch (JSONException ex) {
            throw new InvalidDataException(ex.getMessage());
        }
    }

    public long getId()
    {
        return id;
    }
    
    public String getType()
    {
        return type;
    }
    
    public JSONObject getJson()
    {
        return json;
    }

    public AddrInfo getSender()
    {
        return sender;
    }


    public boolean isResponse()
    {
        return response;
    }

    
    private final boolean response;
    private final long id;
    private final String type; 
    private final JSONObject json;
    private final AddrInfo sender;
}
