package pl.kata.yahtzee;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;

import org.json.JSONObject;

import wtomigraj.Client;
import wtomigraj.Message;

public class GameClient implements GameClientInterface, PropertyChangeListener {

	private Communicator communicator;
	public Client serviceClient;
	public GUEY gui;
	private Server gameServer;
	private ServiceHandlerInterface handler;
	
	
	public GameClient() {
		this.gui = new GUEY(this);
		this.handler = new ServiceHandler(this);
		gui.launch();
	}
	
	public void propertyChange(PropertyChangeEvent evt)
    {
		if ("state".equals(evt.getPropertyName()))
        {
            
        }
		else if (("gamedata".equals(evt.getPropertyName())))
		{
			JSONObject data = (JSONObject) evt.getNewValue();
			communicator.handleData(data);
		}
		else if ("chatmssg".equals(evt.getPropertyName()))
        {
            Message mssg = (Message) evt.getNewValue();
            chatMssgReceived(mssg);
        }
        else if ("connected".equals(evt.getPropertyName()))
        {
           
        }
        else if ("channellist".equals(evt.getPropertyName()))
        {
            String[] channels = (String[] ) evt.getNewValue();
            channelListChanged(channels);
        }
        else if ("joined".equals(evt.getPropertyName()))
        {
            String nick = evt.getNewValue().toString();
            if(serviceClient.isHost()) {
            	gameServer.addPlayer(nick);
            }
            gui.append("***** " + nick + " joined *****");
        }
        else if ("left".equals(evt.getPropertyName()))
        {
            String nick = evt.getNewValue().toString();
            gui.append("***** " + nick + " left *****");
        }
        else if ("problem".equals(evt.getPropertyName()))
        {
            JOptionPane.showMessageDialog(gui.getActiveFrame(), evt.getNewValue().toString(),
                    "ClientFrame", JOptionPane.INFORMATION_MESSAGE);
        }
        else System.err.println("Unknown property \""
                + evt.getPropertyName() + "\"");
    }
	
	private void channelListChanged(String[] channels) {
		gui.displayChannelList(channels);
	}

	private void chatMssgReceived(Message mssg) {
		String beg = "<" + mssg.author + "> ";
		gui.append(beg + mssg.content);
	}
	
	
	
	public void prepareGame() {
		if(serviceClient.isHost()) {
			gameServer = new Server(serviceClient.getMyNick(), serviceClient);
			Thread serverThread = new Thread(gameServer);
			serverThread.start();
			communicator = LocalCommunicator.getInstance(gameServer);
		} else {
			communicator = new RemoteCommunicator(serviceClient.getMyNick(), serviceClient);
		}
		communicator.register(this);
		gui.getActiveFrame().dispose();
        gui.gameStart();
        new Thread(new Runnable() {
			
			@Override
			public void run() {
				boolean[] mask = {true, true, true, true, true};
				reroll(mask);
			}
		}).start();
        
	}
	
	public void choose(BoxChoice choice) {
		communicator.sendChoice(choice);
	}
	
	public void respondScore(BoxChoice choice, Integer score) {
		gui.displayScore(choice, score);
	}

	public void reroll(boolean[] mask) {
		communicator.sendRerollMask(mask);
	}
	
	public void respondRoll(Integer[] roll) {
		gui.displayRoll(roll);
	}


	@Override
	public void statusUpdate(String playerName, Integer score) {
		gui.updateScore(playerName, score);
	}

	public ServiceHandlerInterface getHandler() {
		return handler;
	}



}
