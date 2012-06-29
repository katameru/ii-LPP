package pl.kata.yahtzee;

import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONObject;

import wtomigraj.Client;

public class RemoteCommunicator extends Communicator {
	private Client client;
	private String playerName;
	private GameClientInterface gameClient;
	
	private BoxChoice choice;
	private boolean[] mask;
	private Integer[] roll;
	private Server server;
	private Integer score;

	public RemoteCommunicator(String playerName, Client client) {
		this.client = client;
		this.setPlayerName(playerName);
	}
	
	public void register(Server server) {
		this.server = server;
	}

	@Override
	Action getAction() {
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
	public void register(GameClientInterface gameClient) {
		this.gameClient = gameClient;
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
	void sendRoll(Integer[] roll) {
		JSONArray jroll;
		try {
			jroll = new JSONArray(Arrays.asList(roll));
			JSONObject data = new JSONObject();
			data.put("roll", jroll);
			data.put("type", "gamedata");
			client.sendData(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	void sendScore(Integer score) {
		try {
			JSONObject data = new JSONObject();
			data.put("score", score);
			data.put("type", "gamedata");
			client.sendData(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	void sendUpdate(Player player, Integer score) {
		try {
			JSONObject data = new JSONObject();
			data.put("nick", player.getState().getName());
			data.put("score", score);
			data.put("type", "gamedata");
			client.sendData(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	void sendChoice(BoxChoice choice) {
		try {
			JSONObject data = new JSONObject();
			data.put("choice", choice);
			data.put("type", "gamedata");
			data.put("author", client.getMyNick());
			client.sendData(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	void sendRerollMask(boolean[] mask) {
		JSONArray jmask;
		try {
			jmask = new JSONArray(Arrays.asList(mask));
			JSONObject data = new JSONObject();
			data.put("type", "gamedata");
			data.put("mask", jmask);
			data.put("author", client.getMyNick());
			client.sendData(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void handleData(JSONObject data) {
		if(data.has("nick")) {
			gameClient.statusUpdate(data.optString("nick"), data.optInt("score"));
		}
		if(!data.has("nick") && data.has("score")) {
			this.score = data.optInt("score");
			synchronized (gameClient) {
				gameClient.notify();
			}
		}
		if(data.has("roll")) {
			JSONArray encRoll = data.optJSONArray("roll");
			for(int i=0; i<5; i++) {
				roll[i] = encRoll.optInt(i);
			}
			synchronized (gameClient) {
				gameClient.notify();
			}
		}
	}
	
	public synchronized void setMask(boolean mask[]) {
		this.mask = mask;
		synchronized (server) {
			server.notify();
		}
	}
	
	public synchronized void setChoice(BoxChoice choice) {
		this.choice = choice;
		synchronized (server) {
			server.notify();
		}
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

}
