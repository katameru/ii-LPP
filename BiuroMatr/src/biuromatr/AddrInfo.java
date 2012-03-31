
package biuromatr;

import java.net.InetAddress;

/**
 *
 * @author grzes
 */
public class AddrInfo
{
    public AddrInfo(InetAddress iaddr, int port)
    {
        this.iaddr = iaddr;
        this.port = port;
    }

    public InetAddress getIaddr()
    {
        return iaddr;
    }

    public int getPort()
    {
        return port;
    }
    
    String toString(InetAddress iaddr, int port)
    {
        return "AddrInfo:" + iaddr.getHostAddress() + ":" + port;
    }
    
    @Override
    public String toString()
    {
        return toString(iaddr, port);
    }
    
    public static AddrInfo fromString(String str) throws Exception
    {
        String[] tab = str.split(":");
        if (tab.length < 3)
            throw new Exception("Address not valid.");
        if (!tab[0].equalsIgnoreCase("AddrInfo"))
            throw new Exception("Message doesn't start with \"AddrInfo\".");
        InetAddress iaddr = InetAddress.getByName(tab[1]);
        int port = Integer.parseInt(tab[2]);
        return new AddrInfo(iaddr, port);
    }
    
    public boolean equals(AddrInfo ai)
    {
        return iaddr.equals(ai.iaddr) && port == ai.port;
    }

    private InetAddress iaddr;
    private int port;

}
