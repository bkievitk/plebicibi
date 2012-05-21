package mainGame.run;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import javax.swing.*;

import mainGame.GameAction;
import mainGame.Board;
import mainGame.ChatServer;
import mainGame.GameContext;
import mainGame.SynchLock;
import mainGame.WorldEvent;
import mainGame.XMLParser;

public class Server {

	// Game respresentation.
	public Board board;
	
	// Connection to terminate.
	public ServerSocket serverSocket;
	public ServerSocket chatServer;
	public BufferedWriter hiddenChatClient;
	
	public GameContext context;

	public Vector<ObjectInputStream> readers = new Vector<ObjectInputStream>();
	public Vector<ObjectOutputStream> writers = new Vector<ObjectOutputStream>();
		
	// Port to talk on.
	public static final int PORT = 3457;
		
	/**
	 * A join of an action and a player ID.
	 * The compare function matches the order, lower order goes first.
	 * @author iami
	 */
	class ActionPlayer implements Comparable<ActionPlayer> {
		public GameAction action;
		public int playerID;
		public ActionPlayer(GameAction action, int playerID) {
			this.action = action;
			this.playerID = playerID;
		}

		public int compareTo(ActionPlayer arg0) {
			return action.order - arg0.action.order;
		}
	}
	

	public static void main(String[] args) {
		new Server();
	}
	
	public int playersRegistered = 0;
	
	public Server() {
		
		// Create server and visualization.
		final JFrame frame = new JFrame();

		// Allow them to close the server.
		JButton button = new JButton("closer server");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					serverSocket.close();
					chatServer.close();
					frame.dispose();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
		});

		JLabel serverIP = new JLabel();
		final JLabel playersLabel = new JLabel("No players.");
		try {
			InetAddress thisIp =InetAddress.getLocalHost();
			serverIP.setText("Server IP: " + thisIp.getHostAddress());
		} catch(Exception e) {
			serverIP.setText("Unable to determine server IP.");
		}	

		JPanel serverPanel = new JPanel(new BorderLayout());
		serverPanel.add(button, BorderLayout.CENTER);
		serverPanel.add(serverIP, BorderLayout.NORTH);
		serverPanel.add(playersLabel, BorderLayout.SOUTH);
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setSize(200,100);
		frame.add(serverPanel);
		frame.setVisible(true);		
	
		XMLParser xml = null;
		
		try {
			// Extract all actions.
			xml = new XMLParser();
			
			// Select context file.
			JFileChooser fc = new JFileChooser(".");
			fc.showOpenDialog(null);
			File selected = fc.getSelectedFile();
			
			if(selected.getName().endsWith(".xml")) {
				// Read from xml
				BufferedReader r = new BufferedReader(new FileReader(fc.getSelectedFile()));
				context = xml.parseGameContext(r, xml.actions, fc.getSelectedFile().toString());
				r.close();
			} else {
				// Read from serialization
				ObjectInputStream r = new ObjectInputStream(new FileInputStream(fc.getSelectedFile()));
				context = (GameContext)r.readObject();
				r.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// Create board.
		board = new Board(context);
		final GameAction[] actions = new GameAction[context.numPlayers];	
	
		final Server finalThis = this;

		// Locks.
		final SynchLock simpleLock = new SynchLock(context.numPlayers,null);
		final SynchLock actionLock = new SynchLock(context.numPlayers, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
													
				// Place all of the players in turn order.
				ActionPlayer[] actionPlayers = new ActionPlayer[actions.length];
				for(int i=0;i<actions.length;i++) {
					int player = (i + board.turn) % actions.length;
					actionPlayers[i] = new ActionPlayer(actions[player],player);
				}
				
				// Sort based on action order.
				// Stable sort required so players still take the right turns.
				Arrays.sort(actionPlayers);

				// Perform each action.
				for(ActionPlayer action : actionPlayers) {
					System.out.println("Running card");
					System.out.println("[" + action.action.programs[GameAction.ON_PERFORM] + "]");
					action.action.runProgram(GameAction.ON_PERFORM, finalThis, action.playerID);
				}
				
				// Perform turn actions.
				for(int playerID=0;playerID<board.players.length;playerID++) {
					for(int cardID=0;cardID<board.players[playerID].availableActions.size();cardID++) {
						board.players[playerID].availableActions.get(cardID).runProgram(GameAction.ON_TURN, finalThis, playerID);
					}
				}

				// Perform each world event.
				for(WorldEvent event : context.allEvents) {
					System.out.println("Running event " + event.description);
					event.performWorldEvent(finalThis);
				}
				
				// Next turn.
				board.turn++;	
								
				if(board.turn == context.numTurns) {
					// Perform end game actions.
					for(int playerID=0;playerID<board.players.length;playerID++) {
						for(int cardID=0;cardID<board.players[playerID].availableActions.size();cardID++) {
							board.players[playerID].availableActions.get(cardID).runProgram(GameAction.ON_END, finalThis, playerID);
						}
					}
				}
				
				try {
					hiddenChatClient.write("\nEnd of turn " + board.turn + "\n\n");
					hiddenChatClient.flush();
				} catch(Exception ex) {}
			}
		});
		
		try {
			
			// Build server.
			serverSocket = new ServerSocket(PORT);	
			chatServer = ChatServer.buildChatServer(PORT + 1);
			hiddenChatClient = ChatServer.buildHiddenChatClient("localhost", PORT + 1);

			final Semaphore semaphore = new Semaphore(1);
			
			// For each player...
			for(int i=0;i<context.numPlayers;i++) {

				// Players go sequentially.
				try {
					semaphore.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				// Visible id.
				final int playerID = i;
				new Thread() {
					public void run() {
						try {
							// Create connection.
							Socket socket = serverSocket.accept();
							ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());
							ObjectOutputStream writer = new ObjectOutputStream(socket.getOutputStream());
							
							// Next player can enter.
							semaphore.release();

							playersRegistered++;
							playersLabel.setText(playersRegistered + " players registered.");
							
							readers.add(reader);
							writers.add(writer);
							
							// Synch for everyone to connect.
							simpleLock.lock();					
							
							// Send out the player id and then the initial condition.
							writer.writeInt(playerID);
							writer.writeObject(board);
							writer.writeObject(context);
							
							// Run turns.
							for(int turn=0;turn<context.numTurns;turn++) {
								
								// Get and perform action from each player.
								GameAction action = (GameAction)reader.readObject();
								actions[playerID] = action;
																
								// Wait for all players to place actions.
								actionLock.lock();
									
								// Send the results of the actions back.
								writer.reset();
								writer.writeObject(board);
								writer.writeObject(actions);	
								writer.flush();
							}
							
						} catch (IOException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}.start();				
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
