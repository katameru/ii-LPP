
package wtomigraj;

import java.net.DatagramPacket;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * DatagramInfo is used for extracting data from UDP packets which contain
 * JSON string in their data field.
 * @author grzes
 */
public class DatagramInfo
{
    /**
     * Extracts data from datagram and creates a DatagramInfo object, assuming
     * that data field of given datagram is a string in a JSON format.
     * @param dp UDP datagram packet.
     * @throws InvalidDataException when given datagram packet does not contain
     * string in JSON format or corresponding JSON object doesn't contain 
     * mandatory fields.
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

    /**
     * Returns id of communicate.
     * @return id of communicate.
     */
    public long getId()
    {
        return id;
    }
    

    /**
     * Returns type of communicate.
     * @return type of communicate.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Returns JSON contained in communicate.
     * @return JSON contained in communicate.
     */
    public JSONObject getJson()
    {
        return json;
    }

    /**
     * Returns sender of communicate.
     * @return sender of communicate.
     */
    public AddrInfo getSender()
    {
        return sender;
    }

    /**
     * Returns true iff this communicate is a response.
     * @return true iff this communicate is a response.
     */
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
