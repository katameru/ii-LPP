
package wtomigraj;

/**
 * Exception for situations when received data is inconsistent with protocol.
 * @author grzes
 */
public class InvalidDataException extends Exception
{
    /**
     * Creates InvalidDataException object with no message.
     */
    public InvalidDataException()
    {
        
    }
    
    /**
     * Creates InvalidDataException object with given message.
     * @param message message describing reason of throwing this exception.
     */
    public InvalidDataException(String message)
    {
        super(message);
    }
    
}
