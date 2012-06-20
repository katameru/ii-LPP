
package clientframe;

import java.net.InetAddress;

/**
 *
 * @author grzes
 */
public interface GameInterface
{

    public void leaveGame();

    public void sendChatMssg(String text);

    public void connect(InetAddress ia, int portn, String text);

    public void refreshChannels();

    public void join(String hostname);

    public void startChannel();

    public void disconnect();
    
}
