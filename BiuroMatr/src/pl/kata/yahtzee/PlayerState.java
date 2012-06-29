package pl.kata.yahtzee;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PlayerState {
	private String playerName;
	private Map<BoxChoice, Integer> scores;
	private Integer upperScore;
	private Integer upperBonus;
	private Integer lowerScore;
	private Integer[] currentRoll;
	private int reroll;
	
	
	
	public PlayerState(String localPlayerName) {
		this.playerName = localPlayerName;
		this.scores = new HashMap<BoxChoice, Integer>();
		this.upperScore = 0;
		this.upperBonus = 0;
		this.lowerScore = 0;
		this.currentRoll = new Integer[6];
		Arrays.fill(this.currentRoll, 0);
		this.reroll = 0;
	}

	public boolean isFilled(BoxChoice choice) {
		return scores.containsKey(choice);
	}
	
	public Integer getScore(BoxChoice choice) {
		return scores.get(choice);
	}
	
	public Integer[] getCurrentRoll() {
		return currentRoll;
	}
	
	public void updateRoll(Integer[] roll) {
		currentRoll = roll;
	}
	
	public boolean updateScore(BoxChoice choice, Integer score) {
		if(isFilled(choice)) { return false; }
		
		scores.put(choice, score);

		if(choice.isUpper()) {
			upperScore += score;
		} else {
			lowerScore += score;
		}
		if(upperScore > 63) {
			upperBonus = 35;
		}
		return true;
	}
	
	public boolean isDone()	{
		return scores.size() == BoxChoice.values().length;
	}
	
	public Integer totalScore() {
		return upperScore + upperBonus + lowerScore;
	}

	public int getRerollCount() {
		return reroll;
	}

	public String getName() {
		return playerName;
	}
	
}
