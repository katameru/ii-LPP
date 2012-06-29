package pl.kata.yahtzee;

import org.json.JSONArray;
import org.json.JSONObject;

public class LocalCommunicator extends Communicator {
	
	public static LocalCommunicator getInstance(Server server) {
		if(singleton == null) {
			singleton = new LocalCommunicator(server);
		}
		return singleton;
			
	}
	private GameClientInterface gameClient;
	private BoxChoice choice;
	private boolean[] mask;
	private Integer[] roll;
	private Server server;
	private Integer score;
	
	
	private static LocalCommunicator singleton;
	
	private LocalCommunicator(Server server) {
		this.choice = null;
		this.mask = null;
		this.roll = null;
		this.score = null;
		this.server = server;
	}
	
	@Override
	public synchronized Action getAction() {
		if(mask != null) {
			return Action.REROLL;
		}
		if(choice != null) {
			return Action.BOX;
		}
		return Action.TIMEOUT;
	}

	@Override
	public synchronized BoxChoice getChoice() {
		BoxChoice tmp = choice;
		choice = null;
		return tmp;
	}

	@Override
	public synchronized boolean[] getRerollMask() {
		boolean[] tmp = mask;
		mask = null;
		return tmp;
	}

	@Override
	public synchronized Integer[] getRoll() {
		Integer[] tmp = roll;
		this.roll = null;
		return tmp;
	}

	@Override
	public synchronized Integer getScore() {
		Integer tmp = score;
		this.score = null;
		return tmp;
	}
	
	@Override
	public synchronized void register(GameClientInterface client) {
		this.gameClient = client;  
	}

	@Override
	public synchronized boolean requestReady() {
		return (mask != null || choice != null);
	}

	@Override
	public synchronized boolean responseReady() {
		return (roll != null || score != null);
	}

	@Override
	public synchronized void sendRoll(Integer[] roll) {
		this.roll = roll;
		synchronized (gameClient) {
			gameClient.notify();
		}
	}

	@Override
	public synchronized void sendScore(Integer score) {
		this.score = score;
		synchronized (gameClient) {
			gameClient.notify();
		}
	}

	@Override
	public void sendUpdate(Player player, Integer score) {
		gameClient.statusUpdate(player.getState().getName(), score);
	}

	@Override
	public synchronized void sendChoice(BoxChoice choice) {
		this.choice = choice;
		synchronized (server) {
			server.notify();
		}
	}

	@Override
	public synchronized void sendRerollMask(boolean[] mask) {
		this.mask = mask;
		synchronized (server) {
			server.notify();
		}
	}

	@Override
	void handleData(JSONObject data) {
		if(data.has("choice")) {
			server.remoteChoice(data.optString("author"), (BoxChoice) data.opt("choice"));
		}
		if(data.has("mask")) {
			JSONArray encMask = data.optJSONArray("mask");
			boolean[] smask = new boolean[5];
			for(int i=0; i<5; i++) {
				smask[i] = encMask.optBoolean(i);
			}
			server.remoteMask(data.optString("author"), smask);
		}
		synchronized (server) {
			server.notify();
		}
	}
}
