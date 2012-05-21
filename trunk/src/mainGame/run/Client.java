package mainGame.run;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import mainGame.*;
import mainGame.gui.*;

/**
 * Interface for user to interact with game.
 * @author iami
 */

public class Client {

	// Players version of board.
	public Board board;
	public GameContext context;
	
	// Your playerID.
	public int playerID;
		
	// IO
	public ObjectOutputStream writer;	
	public ObjectInputStream reader;

	public JPanel actionsPanel;
	public JPanel resourcesPanel;
	public JPanel chat;
	
	private final ExecutorService services = Executors.newFixedThreadPool(2);	
	
	private JFrame frame = new JFrame();
	
	public static void main(String[] args) {		
		new Client();
	}
	
	public void performAction(final GameAction action, final GUIMain main) {
		try {
			// Send action.
			writer.writeObject(action);
			writer.flush();
			
			// Object holder.
			Object object;
			
			do {
				// Read objects from the server.
				object = reader.readObject();
				
				// If it is a string, then it is a command.
				if(object instanceof String) {
					
					// Run the program here.
					String program = (String)object;
					new GameScript(program,playerID,board,context,reader,writer,null);
				}
				
				// If it was a board, then keep it.
			} while(!(object instanceof Board));
												
			// Receive results.
			board = (Board)object;
			reader.readObject();
			
			main.rebuild();
		} catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void performActionSynchronous(final GameAction action, final GUIMain main) {

		// Run in separate thread.
		services.execute(new Runnable() {
			public void run() {
				performAction(action, main);
			}
		});
	}
	
	public Client() {	

		// Ask where the server is to connect to.
		String server = (String)JOptionPane.showInputDialog(
		                    null,
		                    "Select Server Location",
		                    "Server Location",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null,
		                    null,
		                    "localhost");
		if(server != null) {
			init(server, null);
		}
	}
		
	public Client(String server, Board board, GameContext context) {
		this.context = context;
		init(server, board);
	}
	
	private void init(String server, Board board) {

		JLabel message = new JLabel();
		
		try {
			final BufferedImage backgroundImage = ImageIO.read(new File("images/background.png"));
			Container c = frame.getContentPane();
			JPanel background = new JPanel(new BorderLayout()) {
				private static final long serialVersionUID = 8079171830392201654L;
				public void paintComponent(Graphics g) {
					g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
				}
			};
			
			c.add(background);
			message.setFont(new Font("Arial Black",Font.BOLD,20));
			message.setVerticalAlignment(JLabel.CENTER);
			message.setHorizontalAlignment(JLabel.CENTER);	
			
			background.add(message, BorderLayout.CENTER);
			message.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
			// Build GUI.
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800,550);
			frame.setVisible(true);			
			
			if(server != null) {

				message.setText("Waiting for connection.");
				
				// Connect.
				Socket socket = new Socket(server,Server.PORT);
				writer = new ObjectOutputStream(socket.getOutputStream());
				reader = new ObjectInputStream(socket.getInputStream());
				
				message.setText("Waiting for other players.");
				
				// Read your Id.
				playerID = reader.readInt();

				// Build chat when you have your ID.
				chat = ChatServer.getChatClient("player " + playerID, server, Server.PORT + 1);
			}
			
			if(board == null) {
				// Read the board and context.
				this.board = (Board)reader.readObject();
				context = (GameContext)reader.readObject();
			} else {
				this.board = board;
			}
			
			showInstructions();
		} catch(ConnectException e) {			
			message.setText("Unable to connect to host [" + server + "]");
		} catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void showInstructions() {

		// Remove old.
		Container c = frame.getContentPane();
		c.removeAll();

		c.setLayout(new BorderLayout());
		
		try {
			final JEditorPane display = new JEditorPane();
			display.setPage(new URL("file:tutorial.html"));			
			display.setEditable(false);
			display.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent arg0) {
					if(arg0.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						if(arg0.getDescription().equals("begin")) {
							showGame();
							
							
						} else {
							try {
								//System.out.println(arg0.getDescription());
								display.setPage(new URL("file:tutorial/" + arg0.getDescription()));
							} catch (MalformedURLException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}				
			});			
			
			c.add(new JScrollPane(display),BorderLayout.CENTER);
		} catch(IOException e) {
			e.printStackTrace();
		}

		c.validate();		
	}
	
	public void showBackstory() {

		// Remove old.
		Container c = frame.getContentPane();
		c.removeAll();
		
		if(context.backStory.endsWith(".html")) {
			
			// If html file, then load.
			try {
				JEditorPane display = new JEditorPane();
				
				//"file:name if as a file"
				display.setPage(new URL(context.backStory.trim()));			
				display.setEditable(false);
				display.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent arg0) {
						if(arg0.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
							
							// On the begin link, start the game.
							if(arg0.getDescription().equals("begin")) {
								showGame();
							}
						}
					}				
				});			
				
				c.add(display,BorderLayout.CENTER);
			} catch(IOException e) {
				e.printStackTrace();
			}
			
		} else {
			
			// Show story.
			JTextArea textArea = new JTextArea(context.backStory);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setEditable(false);		
	
			// Continue button.
			JButton continueButton = new JButton("continue");
			continueButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					showGame();
				}
			});
	
			// Add elements.
			c.setLayout(new BorderLayout());
			c.add(textArea,BorderLayout.CENTER);
			c.add(continueButton,BorderLayout.SOUTH);
		}
		
		c.validate();
	}
	
	public void showGame() {
		
		// Remove old.
		Container c = frame.getContentPane();
		c.removeAll();
		c.add(new MapLayoutGUIMain(this));
		c.validate();
	}
	
	/**
	 * Render the land area.
	 * @param g
	 * @param rect
	 */
	public static void renderLand(Board board, Graphics g, Rectangle rect, GameContext context) {
		for(int x=0;x<board.land.length;x++) {
			for(int y=0;y<board.land[x].length;y++) {
				int px = rect.x + rect.width * x / board.land.length;
				int py = rect.y + rect.height * y / board.land[x].length;
				int width = rect.width / board.land.length;
				int height = rect.height / board.land[x].length;
				
				g.setColor(Color.BLACK);
				g.drawRect(px, py, width, height);
				g.drawImage(context.landTypes.get(board.land[x][y]).image.getImage(), px+1, py+1, width-2, height-2, null);
			}
		}
	}
	
	public static void renderLand(Board board, Graphics g, Rectangle rect, Point selected, GameContext context) {
		for(int x=0;x<board.land.length;x++) {
			for(int y=0;y<board.land[x].length;y++) {
				int px = rect.x + rect.width * x / board.land.length;
				int py = rect.y + rect.height * y / board.land[x].length;
				int width = rect.width / board.land.length;
				int height = rect.height / board.land[x].length;
				
				g.setColor(Color.BLACK);
				g.drawRect(px, py, width, height);
				g.drawImage(context.landTypes.get(board.land[x][y]).image.getImage(), px+1, py+1, width-2, height-2, null);
				
				if(selected != null && selected.x == x && selected.y == y) {
					g.setColor(new Color(0,0,0,100));
					g.fillRect(px, py, width, height);
				}
			}
		}
	}

}
