package pl.kata.yahtzee;

import java.net.InetAddress;

@SuppressWarnings("unused")
public interface GameClientInterface {
	void statusUpdate(String playerName, Integer score);
	ServiceHandlerInterface getHandler();
	//public void setupClientConnection(InetAddress addr, Integer port, String name);
}
