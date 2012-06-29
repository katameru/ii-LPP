
package wtomigraj;

/**
 * Exception for situations when data could not be send.
 * @author grzes
 */
public class ConnectionException extends Exception
{
    /**
     * Creates ConnectionException object with no message.
     */
    public ConnectionException() {}
    
    /**
     * Creates ConnectionException object with given message.
     * @param mssg message describing reason of throwing this exception.
     */
    public ConnectionException(String mssg) {
        super(mssg);
    }
}
