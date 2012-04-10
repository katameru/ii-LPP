
package biuromatr;

import java.net.DatagramPacket;

/**
 *
 * @author grzes
 */
public class DatagramInfo
{
    public DatagramInfo (DatagramPacket dp) throws InvalidDataException
    {
        sender = new AddrInfo(dp.getAddress(), dp.getPort());
        String data = Utils.readData(dp.getData(), dp.getLength());
        
//        System.out.println("Creating datagramInfo from " + data);
        
        if (data.startsWith("res|")) {
            response = true;
            data = data.substring(4);
        }
        else response = false;
        int i = data.indexOf("|");
        if (i == -1) throw new InvalidDataException();
        String idstr = data.substring(0, i);
        try {
            id = Long.parseLong(idstr);
        } catch (NumberFormatException ex) {
            throw new InvalidDataException();
        }
        mssg = data.substring(i+1).split("\\|");
        
//        System.out.println("Id: " + id + "\nSender: " +
//                sender + "\nMssg: " + Utils.glue(mssg) + "\n\n");
    }

    public long getId()
    {
        return id;
    }

    public String[] getMssg()
    {
        return mssg;
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
    private final AddrInfo sender;
    private final String[] mssg;
}
