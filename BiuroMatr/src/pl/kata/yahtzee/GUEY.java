package pl.kata.yahtzee;

import java.awt.Dimension;

import javax.swing.JFrame;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import java.awt.Component;
import javax.swing.border.EtchedBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class GUEY {

	private class ChoiceUnit {
		public ChoiceUnit(JButton choiceButton, JLabel scoreLabel) {
			this.choiceButton = choiceButton;
			this.scoreLabel = scoreLabel;
		}
		private JButton choiceButton;
		private JLabel scoreLabel;
		
		public void setScore(Integer score) {
			this.choiceButton.setEnabled(false);
			this.scoreLabel.setText(String.valueOf(score));
		}
		
	}
	
	private JFrame gameFrame;
	private JFrame launcherFrame;
	private JFrame gameSelectFrame;
	private JTextArea chatHistory;
	private Icon[] diceImages;
	private JToggleButton[] dice;
	private Map<BoxChoice, ChoiceUnit> scoreFields;
	private GameClient client;
	private GameSelect selector;
	private JFrame activeFrame;

	/**
	 * Launch the application.
	 */
	

	/**
	 * Create the application.
	 */
	public GUEY(GameClient client) {
		this.client = client;
		//initialize();	
	}
/*
	public void connect(InetAddress ia, int portn, String string) {
		gameFrame.dispose();
		initialize();
		client = new GameClient(this);
		gameFrame.setVisible(true);
	}
	*/
	
	/**
	 * Initialize the contents of the frame.
	 */
	public void gameStart() {
		gameFrame = new JFrame();
		gameFrame.setBounds(100, 100, 600, 550);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameFrame.getContentPane().setLayout(new GridLayout(1, 0, 0, 0));
		activeFrame = gameFrame;
		

		
		diceImages = new Icon[7];
		for(int i=0; i<7; i++) {
			Icon im = new ImageIcon("C:/Users/kata/Pictures/" + String.valueOf(i) + ".png");
			diceImages[i] = im;
		}
		scoreFields = new HashMap<BoxChoice, ChoiceUnit>();
		
		Box uiContainer = Box.createHorizontalBox();
		gameFrame.getContentPane().add(uiContainer);
		
		Box chatPane = Box.createVerticalBox();
		chatPane.setMinimumSize(new Dimension(0, 600));
		chatPane.setMaximumSize(new Dimension(300, 600));
		uiContainer.add(chatPane);
		
		chatHistory = new JTextArea();
		chatPane.add(chatHistory);
		
		Box messageContainer = Box.createHorizontalBox();
		messageContainer.setMaximumSize(new Dimension(300, 40));
		chatPane.add(messageContainer);
		
		JTextField messageField = new JTextField();
		messageContainer.add(messageField);
		messageField.setColumns(10);
		
		JButton sendButton = new JButton("Send");
		messageContainer.add(sendButton);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		uiContainer.add(horizontalStrut);
		
		Box diceContainer = Box.createVerticalBox();
		diceContainer.setMaximumSize(new Dimension(65, 550));
		diceContainer.setAlignmentY(Component.TOP_ALIGNMENT);
		diceContainer.setMinimumSize(new Dimension(45, 550));
		uiContainer.add(diceContainer);
		
		dice = new JToggleButton[5];
		
		dice[0] = new JToggleButton("");
		dice[0].setAlignmentX(Component.CENTER_ALIGNMENT);
		dice[0].setIcon(diceImages[0]);
		dice[0].setSize(41, 41);
		diceContainer.add(dice[0]);
		
		dice[1] = new JToggleButton("");
		dice[1].setAlignmentX(Component.CENTER_ALIGNMENT);
		dice[1].setIcon(diceImages[0]);
		diceContainer.add(dice[1]);
		
		dice[2] = new JToggleButton("");
		dice[2].setAlignmentX(Component.CENTER_ALIGNMENT);
		dice[2].setIcon(diceImages[0]);
		diceContainer.add(dice[2]);
		
		dice[3] = new JToggleButton("");
		dice[3].setAlignmentX(Component.CENTER_ALIGNMENT);
		dice[3].setIcon(diceImages[0]);
		diceContainer.add(dice[3]);
		
		dice[4] = new JToggleButton("");
		dice[4].setAlignmentX(Component.CENTER_ALIGNMENT);
		dice[4].setIcon(diceImages[0]);
		diceContainer.add(dice[4]);
		
		JButton reroll = new JButton("Reroll");
		reroll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean[] mask = new boolean[5];
				for(int i = 0; i < 5; i++) {
					mask[i] = dice[i].isSelected();
				}
				client.reroll(mask);
			}
		});
		reroll.setAlignmentX(Component.CENTER_ALIGNMENT);
		diceContainer.add(reroll);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		uiContainer.add(horizontalStrut_1);
		
		Box scoresContainer = Box.createVerticalBox();
		scoresContainer.setMaximumSize(new Dimension(300, 700));
		scoresContainer.setMinimumSize(new Dimension(0, 700));
		uiContainer.add(scoresContainer);
		
		Box onesContainer = Box.createHorizontalBox();
		onesContainer.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		onesContainer.setEnabled(false);
		onesContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
		onesContainer.setSize(400, 40);
		scoresContainer.add(onesContainer);
		
		JButton onesButton = new JButton("Ones");
		onesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				client.choose(BoxChoice.ONES);
			}
		});
		onesButton.setPreferredSize(new Dimension(105, 23));
		onesContainer.add(onesButton);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		onesContainer.add(horizontalGlue_2);
		
		JLabel onesScore = new JLabel("0");
		onesScore.setHorizontalTextPosition(SwingConstants.RIGHT);
		onesScore.setHorizontalAlignment(SwingConstants.RIGHT);
		onesContainer.add(onesScore);
		
		Box twosContainer = Box.createHorizontalBox();
		twosContainer.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		twosContainer.setEnabled(false);
		twosContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(twosContainer);
		
		JButton twosButton = new JButton("Twos");
		twosButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.choose(BoxChoice.TWOS);
			}
		});
		twosButton.setPreferredSize(new Dimension(105, 23));
		twosContainer.add(twosButton);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		twosContainer.add(horizontalGlue_1);
		
		JLabel twosScore = new JLabel("0");
		twosContainer.add(twosScore);
		
		Box threesContainer = Box.createHorizontalBox();
		threesContainer.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		threesContainer.setEnabled(false);
		threesContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(threesContainer);
		
		JButton threesButton = new JButton("Threes");
		threesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.choose(BoxChoice.THREES);
			}
		});
		threesButton.setPreferredSize(new Dimension(105, 23));
		threesContainer.add(threesButton);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		threesContainer.add(horizontalGlue);
		
		JLabel threesScore = new JLabel("0");
		threesScore.setHorizontalAlignment(SwingConstants.RIGHT);
		threesScore.setHorizontalTextPosition(SwingConstants.LEFT);
		threesContainer.add(threesScore);
		
		Box foursContainer = Box.createHorizontalBox();
		foursContainer.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		foursContainer.setEnabled(false);
		foursContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(foursContainer);
		
		JButton foursButton = new JButton("Fours");
		foursButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.choose(BoxChoice.FOURS);
			}
		});
		foursButton.setPreferredSize(new Dimension(105, 23));
		foursContainer.add(foursButton);
		
		Component horizontalGlue_3 = Box.createHorizontalGlue();
		foursContainer.add(horizontalGlue_3);
		
		JLabel foursScore = new JLabel("0");
		foursContainer.add(foursScore);
		
		Box fivesContainer = Box.createHorizontalBox();
		fivesContainer.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		fivesContainer.setEnabled(false);
		fivesContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(fivesContainer);
		
		JButton fivesButton = new JButton("Fives");
		fivesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.choose(BoxChoice.FIVES);
			}
		});
		fivesButton.setPreferredSize(new Dimension(105, 23));
		fivesContainer.add(fivesButton);
		
		Component horizontalGlue_4 = Box.createHorizontalGlue();
		fivesContainer.add(horizontalGlue_4);
		
		JLabel fivesScore = new JLabel("0");
		fivesContainer.add(fivesScore);
		
		Box sixesContainer = Box.createHorizontalBox();
		sixesContainer.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		sixesContainer.setEnabled(false);
		sixesContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(sixesContainer);
		
		JButton sixesButton = new JButton("Sixes");
		sixesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.choose(BoxChoice.SIXES);
			}
		});
		sixesButton.setPreferredSize(new Dimension(105, 23));
		sixesContainer.add(sixesButton);
		
		Component horizontalGlue_5 = Box.createHorizontalGlue();
		sixesContainer.add(horizontalGlue_5);
		
		JLabel sixesScore = new JLabel("0");
		sixesContainer.add(sixesScore);
		
		Box horizontalBox_7 = Box.createHorizontalBox();
		horizontalBox_7.setEnabled(false);
		horizontalBox_7.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(horizontalBox_7);
		
		JLabel lblTotal = new JLabel("Total");
		horizontalBox_7.add(lblTotal);
		
		Component horizontalGlue_6 = Box.createHorizontalGlue();
		horizontalBox_7.add(horizontalGlue_6);
		
		JLabel upperTotal = new JLabel("0");
		upperTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		horizontalBox_7.add(upperTotal);
		
		Box horizontalBox_8 = Box.createHorizontalBox();
		horizontalBox_8.setEnabled(false);
		horizontalBox_8.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(horizontalBox_8);
		
		JLabel lblBonus = new JLabel("Bonus");
		horizontalBox_8.add(lblBonus);
		
		Component horizontalGlue_7 = Box.createHorizontalGlue();
		horizontalBox_8.add(horizontalGlue_7);
		
		JLabel upperBonus = new JLabel("0");
		horizontalBox_8.add(upperBonus);
		
		Box horizontalBox_9 = Box.createHorizontalBox();
		horizontalBox_9.setEnabled(false);
		horizontalBox_9.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(horizontalBox_9);
		
		JLabel lblUpperBoxTotal = new JLabel("Upper box total");
		horizontalBox_9.add(lblUpperBoxTotal);
		
		Component horizontalGlue_8 = Box.createHorizontalGlue();
		horizontalBox_9.add(horizontalGlue_8);
		
		JLabel upperTotalTotal = new JLabel("0");
		horizontalBox_9.add(upperTotalTotal);
		
		Box horizontalBox_10 = Box.createHorizontalBox();
		horizontalBox_10.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		horizontalBox_10.setEnabled(false);
		horizontalBox_10.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(horizontalBox_10);
		
		JButton btnThreeOfA = new JButton("Three of a kind");
		btnThreeOfA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.choose(BoxChoice.THREE_OF_A_KIND);
			}
		});
		btnThreeOfA.setPreferredSize(new Dimension(120, 23));
		horizontalBox_10.add(btnThreeOfA);
		
		Component horizontalGlue_9 = Box.createHorizontalGlue();
		horizontalBox_10.add(horizontalGlue_9);
		
		JLabel threeOfAScore = new JLabel("0");
		horizontalBox_10.add(threeOfAScore);
		
		Box horizontalBox_11 = Box.createHorizontalBox();
		horizontalBox_11.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		horizontalBox_11.setEnabled(false);
		horizontalBox_11.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(horizontalBox_11);
		
		JButton btnFourOfA = new JButton("Four of a kind");
		btnFourOfA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.choose(BoxChoice.FOUR_OF_A_KIND);
			}
		});
		btnFourOfA.setPreferredSize(new Dimension(120, 23));
		btnFourOfA.setMinimumSize(new Dimension(105, 23));
		btnFourOfA.setMaximumSize(new Dimension(105, 23));
		horizontalBox_11.add(btnFourOfA);
		
		Component horizontalGlue_10 = Box.createHorizontalGlue();
		horizontalBox_11.add(horizontalGlue_10);
		
		JLabel fourOfAScore = new JLabel("0");
		horizontalBox_11.add(fourOfAScore);
		
		Box horizontalBox_12 = Box.createHorizontalBox();
		horizontalBox_12.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		horizontalBox_12.setEnabled(false);
		horizontalBox_12.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(horizontalBox_12);
		
		JButton btnFullHouse = new JButton("Full house");
		btnFullHouse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.choose(BoxChoice.FULL_HOUSE);
			}
		});
		btnFullHouse.setPreferredSize(new Dimension(120, 23));
		horizontalBox_12.add(btnFullHouse);
		
		Component horizontalGlue_11 = Box.createHorizontalGlue();
		horizontalBox_12.add(horizontalGlue_11);
		
		JLabel fullScore = new JLabel("0");
		horizontalBox_12.add(fullScore);
		
		Box horizontalBox_13 = Box.createHorizontalBox();
		horizontalBox_13.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		horizontalBox_13.setEnabled(false);
		horizontalBox_13.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(horizontalBox_13);
		
		JButton btnSmallStraight = new JButton("Small straight");
		btnSmallStraight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.choose(BoxChoice.SMALL_STRAIGHT);
			}
		});
		btnSmallStraight.setPreferredSize(new Dimension(120, 23));
		horizontalBox_13.add(btnSmallStraight);
		
		Component horizontalGlue_12 = Box.createHorizontalGlue();
		horizontalBox_13.add(horizontalGlue_12);
		
		JLabel smallScore = new JLabel("0");
		horizontalBox_13.add(smallScore);
		
		Box horizontalBox_14 = Box.createHorizontalBox();
		horizontalBox_14.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		horizontalBox_14.setEnabled(false);
		horizontalBox_14.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(horizontalBox_14);
		
		JButton btnLargeStraight = new JButton("Large straight");
		btnLargeStraight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.choose(BoxChoice.LARGE_STRAIGHT);
			}
		});
		btnLargeStraight.setPreferredSize(new Dimension(120, 23));
		horizontalBox_14.add(btnLargeStraight);
		
		Component horizontalGlue_13 = Box.createHorizontalGlue();
		horizontalBox_14.add(horizontalGlue_13);
		
		JLabel largeScore = new JLabel("0");
		horizontalBox_14.add(largeScore);
		
		Box horizontalBox_15 = Box.createHorizontalBox();
		horizontalBox_15.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		horizontalBox_15.setEnabled(false);
		horizontalBox_15.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(horizontalBox_15);
		
		JButton btnYahtzee = new JButton("Yahtzee");
		btnYahtzee.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.choose(BoxChoice.YAHTZEE);
			}
		});
		btnYahtzee.setPreferredSize(new Dimension(120, 23));
		horizontalBox_15.add(btnYahtzee);
		
		Component horizontalGlue_14 = Box.createHorizontalGlue();
		horizontalBox_15.add(horizontalGlue_14);
		
		JLabel yahtzeeScore = new JLabel("0");
		horizontalBox_15.add(yahtzeeScore);
		
		Box horizontalBox_16 = Box.createHorizontalBox();
		horizontalBox_16.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		horizontalBox_16.setEnabled(false);
		horizontalBox_16.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(horizontalBox_16);
		
		JButton btnChance = new JButton("Chance");
		btnChance.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.choose(BoxChoice.CHANCE);
			}
		});
		btnChance.setPreferredSize(new Dimension(120, 23));
		horizontalBox_16.add(btnChance);
		
		Component horizontalGlue_15 = Box.createHorizontalGlue();
		horizontalBox_16.add(horizontalGlue_15);
		
		JLabel chanceScore = new JLabel("0");
		horizontalBox_16.add(chanceScore);
		
		Box horizontalBox_17 = Box.createHorizontalBox();
		horizontalBox_17.setEnabled(false);
		horizontalBox_17.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(horizontalBox_17);
		
		JLabel lblLowerTotal = new JLabel("Lower total");
		horizontalBox_17.add(lblLowerTotal);
		
		Component horizontalGlue_16 = Box.createHorizontalGlue();
		horizontalBox_17.add(horizontalGlue_16);
		
		JLabel lowerScore = new JLabel("0");
		horizontalBox_17.add(lowerScore);
		
		Box horizontalBox_18 = Box.createHorizontalBox();
		horizontalBox_18.setEnabled(false);
		horizontalBox_18.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoresContainer.add(horizontalBox_18);
		
		JLabel lblGrandtotal = new JLabel("Grand total");
		horizontalBox_18.add(lblGrandtotal);
		
		Component horizontalGlue_17 = Box.createHorizontalGlue();
		horizontalBox_18.add(horizontalGlue_17);
		
		JLabel totalScore = new JLabel("0");
		horizontalBox_18.add(totalScore);
		
		scoreFields.put(BoxChoice.ONES, new ChoiceUnit(onesButton, onesScore));
		scoreFields.put(BoxChoice.TWOS, new ChoiceUnit(twosButton, twosScore));
		scoreFields.put(BoxChoice.THREES, new ChoiceUnit(threesButton, threesScore));
		scoreFields.put(BoxChoice.FOURS, new ChoiceUnit(foursButton, foursScore));
		scoreFields.put(BoxChoice.FIVES, new ChoiceUnit(fivesButton, fivesScore));
		scoreFields.put(BoxChoice.SIXES, new ChoiceUnit(sixesButton, sixesScore));
		scoreFields.put(BoxChoice.THREE_OF_A_KIND, new ChoiceUnit(btnThreeOfA, threeOfAScore));
		scoreFields.put(BoxChoice.FOUR_OF_A_KIND, new ChoiceUnit(btnFourOfA, fourOfAScore));
		scoreFields.put(BoxChoice.FULL_HOUSE, new ChoiceUnit(btnFullHouse, fullScore));
		scoreFields.put(BoxChoice.SMALL_STRAIGHT, new ChoiceUnit(btnSmallStraight, smallScore));
		scoreFields.put(BoxChoice.LARGE_STRAIGHT, new ChoiceUnit(btnLargeStraight, largeScore));
		scoreFields.put(BoxChoice.YAHTZEE, new ChoiceUnit(btnYahtzee, yahtzeeScore));
		scoreFields.put(BoxChoice.CHANCE, new ChoiceUnit(btnChance, chanceScore));
		
		gameFrame.setVisible(true);
	}

	public void displayRoll(Integer[] roll) {
		if(roll == null) return;
		for(int i=0; i<5; i++) {
			dice[i].setIcon(diceImages[roll[i]]);
		}
	}
	
	public void displayScore(BoxChoice choice, Integer score) {
		scoreFields.get(choice).setScore(score);
	}
	public void updateScore(String playerName, Integer score) {
		// TODO Auto-generated method stub
		
	}
	public void launch() {
		launcherFrame = new JFrame();
		launcherFrame.setBounds(100, 100, 600, 550);
		launcherFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		launcherFrame.getContentPane().setLayout(new GridLayout(1, 0, 0, 0));
		
		Launcher launcher = new Launcher(client);
		launcherFrame.add(launcher);
		launcherFrame.setVisible(true);
		launcher.setVisible(true);
		activeFrame = launcherFrame;
	}
	public void selectGame() {
		gameSelectFrame = new JFrame();
		gameSelectFrame.setBounds(100, 100, 600, 550);
		gameSelectFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameSelectFrame.getContentPane().setLayout(new GridLayout(1, 0, 0, 0));
		
		selector = new GameSelect(client);
		gameSelectFrame.add(selector);
		gameSelectFrame.setVisible(true);
		selector.setVisible(true);
		client.getHandler().refreshChannels();
		activeFrame = gameSelectFrame;
		
	}

	public JFrame getActiveFrame() {
		return activeFrame;
	}

	public void append(String string) {
		String text = chatHistory.getText() + "\n" + string;
		chatHistory.setText(text);
		
	}

	public void displayChannelList(String[] channels) {
		selector.setHosts(channels);
	}



}
