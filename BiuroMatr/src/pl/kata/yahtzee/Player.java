package pl.kata.yahtzee;

public class Player {
	private Communicator channel;
	private PlayerState state;
	
	public Communicator getChannel() {
		return channel;
	}
	public PlayerState getState() {
		return state;
	}
	public Player(PlayerState state, Communicator channel) {
		this.channel = channel;
		this.state = state;
	}
	
}
