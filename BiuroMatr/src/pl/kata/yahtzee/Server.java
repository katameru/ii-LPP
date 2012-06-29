package pl.kata.yahtzee;


import java.util.Random;

import wtomigraj.Client;

public class Server implements Runnable {

	private Random rng;
	private CyclicList<Player> players;
	private Client client;
	
	public Server(String localPlayerName, Client client) {
		rng = new Random();
		players = new CyclicList<Player>();
		players.add(new Player(new PlayerState(localPlayerName), LocalCommunicator.getInstance(this)));
		this.client = client;
	}
	
	public void addPlayer(String playerName) {
		players.add(new Player(new PlayerState(playerName), new RemoteCommunicator(playerName, client)));
	}
	
	private boolean validMove(PlayerState player, BoxChoice choice) {
		return !player.isFilled(choice);
	}
	
	private boolean validReroll(PlayerState player) {
		return player.getRerollCount() < 3;
	}
	
	private Integer[] reroll(PlayerState player, boolean[] rerollMask) {
		Integer[] oldRoll = player.getCurrentRoll();
		Integer[] freshRoll = this.getRoll();
		Integer[] updatedRoll = new Integer[5];
		for(int i = 0; i<5; i++) {
			updatedRoll[i] = rerollMask[i] ? freshRoll[i] : oldRoll[i]; 
		}
		player.updateRoll(updatedRoll);
		return updatedRoll;
	}
	
	private Integer makeMove(PlayerState player, BoxChoice choice) {
		Integer score = 0;
		Integer[] roll = player.getCurrentRoll();
		switch(choice) {
		case ONES:   
			for(Integer d : roll) {if(d==1) score+=d;};
			break;
		case TWOS:   
			for(Integer d : roll) {if(d==2) score+=d;};
			break;
		case THREES: 
			for(Integer d : roll) {if(d==3) score+=d;};
			break;
		case FOURS:  
			for(Integer d : roll) {if(d==4) score+=d;};
			break;
		case FIVES:  
			for(Integer d : roll) {if(d==5) score+=d;};
			break;
		case SIXES:  
			for(Integer d : roll) {if(d==6) score+=d;};
			break;
		case CHANCE: 
			for(Integer d : roll) {         score+=d;};
			break;
		case THREE_OF_A_KIND:
			{
				int[] diceCount;
				diceCount = new int[7];
				for(Integer d : roll) {
					diceCount[d]+=1;
					if(diceCount[d] == 3) {
						score += 3*d;
						break;
					}
				}
			}
			break;
		case FOUR_OF_A_KIND:
			{
				int[] diceCount;
				diceCount = new int[7];
				for(Integer d : roll) {
					diceCount[d]+=1;
					if(diceCount[d] == 4) {
						score += 4*d;
						break;
					}
				}
			}
			break;
		case FULL_HOUSE:
			{
				int[] diceCount;
				diceCount = new int[7];
				for(Integer d : roll) {
					diceCount[d]+=1;
				}
				boolean twos=false, threes=false;
				for(int i = 1; i < 7; i++) {
					if(diceCount[i]==2) twos = true;
					if(diceCount[i]==3) threes = true;
				}
				if(twos && threes) score += 25;
			}
			break;
		case SMALL_STRAIGHT:
			{
				int[] diceCount;
				diceCount = new int[7];
				for(Integer d : roll) {
					diceCount[d]+=1;
				}
				boolean sm_straight=false;
				for(int i=1; i<4; i++) {
					sm_straight = sm_straight || (diceCount[i] > 1 && diceCount[i+1] > 1 && diceCount[i+2] > 1 && diceCount[i+3] > 1);
				}
				if(sm_straight) score+=30;
			}
			break;
		case LARGE_STRAIGHT:
			{
				int[] diceCount;
				diceCount = new int[7];
				for(Integer d : roll) {
					diceCount[d]+=1;
				}
				boolean l_straight=false;
				for(int i=1; i<3; i++) {
					l_straight = l_straight || (diceCount[i] > 1 && diceCount[i+1] > 1 && diceCount[i+2] > 1 && diceCount[i+3] > 1 && diceCount[i+4] > 1);
				}
				if(l_straight) score+=40;
			}
			break;
		case YAHTZEE:
			{
				int[] diceCount;
				diceCount = new int[7];
				for(Integer d : roll) {
					diceCount[d]+=1;
					if(diceCount[d] == 5) {
						score += 50;
						break;
					}
				}
			}
		}

		
		player.updateScore(choice, score);
		return score;
	}
	
	private void sendScoreUpdate(Player currentPlayer, BoxChoice choice, Integer score) {
		for(Player p : players) {
			p.getChannel().sendUpdate(currentPlayer, score);
		}
	}
	
	private void sendReroll(Player currentPlayer, Integer[] updatedRoll) {
		currentPlayer.getChannel().sendRoll(updatedRoll);
	}
	
	public void run() {
		while(!players.getCurrent().getState().isDone()) {
			Player currentPlayer = players.getCurrent();
			if(!currentPlayer.getChannel().requestReady()) {
				try {
					synchronized (this) {
						this.wait(60000);
					}
				} catch (InterruptedException e) {
				}
			}
			Action action = currentPlayer.getChannel().getAction();
			switch(action) {
			case BOX : 
				BoxChoice choice = currentPlayer.getChannel().getChoice();
				if(validMove(currentPlayer.getState(), choice)) {
					Integer score = makeMove(currentPlayer.getState(), choice);
					currentPlayer.getChannel().sendScore(score);
					sendScoreUpdate(currentPlayer, choice, score);
				}
				break;
			case REROLL:
				boolean[] rerollMask = currentPlayer.getChannel().getRerollMask();
				if(validReroll(currentPlayer.getState())) {
					Integer updatedRoll[] = reroll(currentPlayer.getState(), rerollMask);
					sendReroll(currentPlayer, updatedRoll);
				}
				break;
			case TIMEOUT:
				break;
			}
			players.next();
		}
	}


	public Integer[] getRoll() {
		Integer[] roll = new Integer[5];
		for(int i = 0; i < 5; i++) {
			roll[i] = rng.nextInt(5)+1;
		}
		return roll;
	}

	public void remoteChoice(String nick, BoxChoice choice) {
		for(Player p : players) {
			if(p.getState().getName() == nick) {
				RemoteCommunicator c = (RemoteCommunicator) p.getChannel();
				c.setChoice(choice);
			}
		}
	}
	
	public void remoteMask(String nick, boolean[] mask) {
		for(Player p : players) {
			if(p.getState().getName().equals(nick)) {
				RemoteCommunicator c = (RemoteCommunicator) p.getChannel();
				c.setMask(mask);
			}
		}
	}
	
}
