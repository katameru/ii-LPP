
package wtomigraj;

/**
 * Simple class for keeping a message content and its author.
 * @author grzes
 */
public class Message
{
    /**
     * Creates Message object.
     * @param content content of the message.
     * @param author author of the message.
     */
    public Message(String content, String author)
    {
        this.content = content;
        this.author = author;
    }
    /**
     * Content of the message.
     */
    public String content;
    
    /**
     * Author of the message.
     */
    public String author;
}
