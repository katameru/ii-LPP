
package wtomigraj;

import java.net.InetAddress;

/**
 * Class AddrInfo keeps IP address and port in one object.
 * @author grzes
 */
public class AddrInfo implements Comparable<AddrInfo>
{
    /**
     * Constructs AddrInfo object from address and port.
     * @param iaddr Internet address.
     * @param port Port.
     */
    public AddrInfo(InetAddress iaddr, int port)
    {
        this.iaddr = iaddr;
        this.port = port;
    }

    /**
     * Returns Internet address contained in this object.
     * @return Internet address contained in this object.
     */
    public InetAddress getIaddr()
    {
        return iaddr;
    }

    /**
     * Returns port contained in this object.
     * @return port contained in this object.
     */
    public int getPort()
    {
        return port;
    }
    
    /**
     * Returns Internet address contained in this object as string.
     * @return Internet address contained in this object as string.
     */
    public String getAdressString()
    {
        return iaddr.getHostAddress();
    }  
    
    /**
     * Creates AddrInfo from string representation of address and port number.
     * @param address string representation of address;
     * @param port port number.
     * @return AddrInfo object containing given address and port.
     * @throws Exception when string with address is invalid.
     */
    public static AddrInfo fromJsonInfo(String address, int port) throws Exception
    {
        InetAddress iaddr = InetAddress.getByName(address);
        return new AddrInfo(iaddr, port);
    }
    
    /**
     * Method returns true iff this and given address point to the same location.
     * @param ai Other AddrInfo object.
     * @return true iff this and given address point to the same location.
     */
    public boolean equals(AddrInfo ai)
    {
        if (ai == null) return false;
        else return    iaddr.equals(ai.iaddr)
                    && port == ai.port;
    }
    
    /**
     * Method compares this and given address. First it compares addresses,
     * if they are equal it compares ports.
     * @param o Other AddrInfo object.
     * @return -1  when this object is lexicographically first,
     *          1 if it second,
     *          0 when addresses are equal.
     */
    @Override
    public int compareTo(AddrInfo o)
    {
        int c = iaddr.toString().compareTo(o.iaddr.toString());
        if (c == 0) return (port < o.port ? -1 : (port == o.port ? 0 : 1));
        else return c;
    }
    
    /**
     * Return string representation of this AddrInfo object in form 
     * "host_address:port".
     * @return  string representation of this AddrInfo object.
     */
    @Override
    public String toString()
    {
        return iaddr.getHostAddress() + ":" + port;
    }

    private InetAddress iaddr;
    private int port;
}
