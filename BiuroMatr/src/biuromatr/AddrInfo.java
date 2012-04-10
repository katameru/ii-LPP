
package biuromatr;

import java.net.InetAddress;

/**
 *
 * @author grzes
 */
public class AddrInfo implements Comparable<AddrInfo>
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
        return iaddr.getHostAddress() + ":" + port;
    }
    
    @Override
    public String toString()
    {
        return toString(iaddr, port);
    }
    
    public static AddrInfo fromString(String str) throws Exception
    {
        String[] tab = str.split(":");
        if (tab.length < 2)
            throw new Exception("Address not valid.");
        InetAddress iaddr = InetAddress.getByName(tab[0]);
        int port = Integer.parseInt(tab[1]);
        return new AddrInfo(iaddr, port);
    }
    
    public boolean equals(AddrInfo ai)
    {
        if (ai == null) return false;
        else return    iaddr.equals(ai.iaddr)
                    && port == ai.port;
    }
    
    @Override
    public int compareTo(AddrInfo o)
    {
        int c = iaddr.toString().compareTo(o.iaddr.toString());
        if (c == 0) return Integer.compare(port, o.port);
        else return c;
    }

    private InetAddress iaddr;
    private int port;
}
