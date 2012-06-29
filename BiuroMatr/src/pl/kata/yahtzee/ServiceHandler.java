package pl.kata.yahtzee;


import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import wtomigraj.Client;
import wtomigraj.ConnectionException;
import clientframe.ClientFrame;
import clientframe.WaitDialog;

public class ServiceHandler implements ServiceHandlerInterface {
	private GameClient gameClient;
	
	public ServiceHandler(GameClient gameClient) {
		this.gameClient = gameClient;
	}

	@Override
	public void refreshChannels() {
		final JDialog waitd = new WaitDialog(gameClient.gui.getActiveFrame());
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
            {
                try {
                    gameClient.serviceClient.refreshChannels();
                } catch (ConnectionException ex) {
                    JOptionPane.showMessageDialog(gameClient.gui.getActiveFrame(),
                            "Could not contact server.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done()
            {
                waitd.dispose();
            }
        }.execute();
        waitd.setVisible(true);
	}

	@Override
	public void join(final String hostname) {
		final JDialog waitd = new WaitDialog(gameClient.gui.getActiveFrame());
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
            {
                try {
                	gameClient.serviceClient.join(hostname);
                } catch (ConnectionException ex) {
                    JOptionPane.showMessageDialog(gameClient.gui.getActiveFrame(),
                            "Could not contact server.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done()
            {
                waitd.dispose();
            }
        }.execute();
        waitd.setVisible(true);
        gameClient.prepareGame();
	}

	@Override
	public void startChannel() {
		final JDialog waitd = new WaitDialog(gameClient.gui.getActiveFrame());
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
            {
                try {
                	gameClient.serviceClient.startChannel(gameClient.serviceClient.getMyNick(), 32);
                } catch (ConnectionException ex) {
                    JOptionPane.showMessageDialog(gameClient.gui.getActiveFrame(),
                            "Could not contact server.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done()
            {
                waitd.dispose();
            }
        }.execute();
        waitd.setVisible(true);
        gameClient.prepareGame();
	}

	@Override
	public void disconnect() {
		gameClient.serviceClient.close();
		gameClient.gui.getActiveFrame().dispose();
	}

	@Override
	public void connect(InetAddress ia, int port,final  String nick) {
		gameClient.serviceClient = new Client(ia, port, "Yahtzee");
		try {
			gameClient.serviceClient.init();
		} catch (SocketException ex) {
			Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
		}
		gameClient.serviceClient.addPropertyChangeListener(gameClient);

		final JDialog waitd = new WaitDialog(gameClient.gui.getActiveFrame());
		new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground()
			{
				try {
					gameClient.serviceClient.start(nick);
				} catch (ConnectionException ex) {
					JOptionPane.showMessageDialog(gameClient.gui.getActiveFrame(),
							"Could not contact server.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				return null;
			}

			@Override
			protected void done()
			{
				waitd.dispose();
			}
		}.execute();
		waitd.setVisible(true);
		gameClient.gui.getActiveFrame().dispose();
		gameClient.gui.selectGame();
	}
}
