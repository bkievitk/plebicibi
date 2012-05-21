package mainGame.run;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mainGame.Board;
import mainGame.ChatServer;
import mainGame.GameAction;
import mainGame.GameContext;
import mainGame.GameScript;

public class AIClient {

	// Players version of board.
	public Board board;
	public GameContext context;
	
	// Your playerID.
	public int playerID;
		
	// IO
	public ObjectOutputStream writer;	
	public ObjectInputStream reader;

	public JPanel chat;
	
	private final ExecutorService services = Executors.newFixedThreadPool(2);	
	
	private Random rand = new Random();
	
	public static void main(String[] args) {		
		new AIClient();
	}
	
	public void performAction(final GameAction action) {
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
			
		} catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void performActionSynchronous(final GameAction action) {

		// Run in separate thread.
		services.execute(new Runnable() {
			public void run() {
				performAction(action);
			}
		});
	}
	
	public AIClient() {	

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
		
	public AIClient(String server, Board board) {
		init(server, board);
	}
	
	private GameAction chooseAction() {
		return board.players[playerID].availableActions.get(rand.nextInt(board.players[playerID].availableActions.size()));
	}
	
	private void init(String server, Board board) {

		try {

			// Connect.
			Socket socket = new Socket(server,Server.PORT);
			writer = new ObjectOutputStream(socket.getOutputStream());
			reader = new ObjectInputStream(socket.getInputStream());
			
			// Read your Id.
			playerID = reader.readInt();

			// Build chat when you have your ID.
			chat = ChatServer.getChatClient("player " + playerID, server, Server.PORT + 1);
			
			if(board == null) {
				// Read the board and context.
				this.board = (Board)reader.readObject();
				context = (GameContext)reader.readObject();
			} else {
				this.board = board;
			}
			
			for(int turn=0;turn<20;turn++) {
				performAction(chooseAction());
			}
		} catch(ConnectException e) {			
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
