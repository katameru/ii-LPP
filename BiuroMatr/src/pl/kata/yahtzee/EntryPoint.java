package pl.kata.yahtzee;

import java.awt.EventQueue;

public class EntryPoint {
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new GameClient();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
