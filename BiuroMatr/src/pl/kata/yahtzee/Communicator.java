package pl.kata.yahtzee;

import org.json.JSONObject;

abstract class Communicator {
	abstract Action getAction();
	abstract BoxChoice getChoice();
	abstract boolean[] getRerollMask();
	abstract void sendRoll(Integer[] roll);
	abstract void sendUpdate(Player player, Integer totalScore);
	abstract void sendScore(Integer score);
	abstract boolean requestReady();
	
	abstract void register(GameClientInterface client);
	abstract void sendChoice(BoxChoice choice);
	abstract void sendRerollMask(boolean[] mask);
	abstract Integer[] getRoll();
	abstract Integer getScore();
	abstract boolean responseReady();
	abstract void handleData(JSONObject data);
}