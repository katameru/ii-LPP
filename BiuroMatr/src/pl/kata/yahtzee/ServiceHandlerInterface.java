package pl.kata.yahtzee;

import java.net.InetAddress;

public interface ServiceHandlerInterface {
	public void refreshChannels();
	public void join(String hostname);
	public void startChannel();
	public void connect(InetAddress ia, int portn, String string);
	public void disconnect();
	
}
