
package biuromatr;

/**
 * Deriving classes should handle incoming messages.
 * @author grzes
 */
public interface Handler
{
    /**
     * Method dealing with received message.
     * @param dinfo Incoming packet.
     */
    void handle(DatagramInfo dinfo);
}
