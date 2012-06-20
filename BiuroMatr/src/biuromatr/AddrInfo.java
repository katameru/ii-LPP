
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
    
    public String getAdressString()
    {
        return iaddr.getHostAddress();
    }  
    
    public static AddrInfo fromJsonInfo(String address, int port) throws Exception
    {
        InetAddress iaddr = InetAddress.getByName(address);
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
