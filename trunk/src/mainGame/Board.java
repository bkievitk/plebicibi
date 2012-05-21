package mainGame;

import java.io.Serializable;

/**
 * Contains communal land and player information.
 * @author iami
 */

public class Board implements Serializable {
	
	private static final long serialVersionUID = 8487065659506336079L;
	
	// Public domain.
	public int[][] land;
	
	// List of players.
	public Player[] players;
	
	// Game turn.
	public int turn = 0;
	
	public Board(Player[] players) {
		this.players = players;
		fillLand(0);
	}
		
	public Board(GameContext context) {
		
		// Build players.
		players = new Player[context.numPlayers];
		for(int playerID=0;playerID<players.length;playerID++) {
			players[playerID] = new Player(context, playerID);
		}

		// Create board.
		land = new int[context.startingBoard.length][context.startingBoard[0].length];
		for(int x=0;x<land.length;x++) {
			for(int y=0;y<land[0].length;y++) {
				land[x][y] = context.startingBoard[x][y];
			}	
		}		
		
		// Set player actions.
		for(int playerID=0;playerID<players.length;playerID++) {
			if(context.playerActions.get(playerID) != null) {
				players[playerID].availableActions.addAll(context.playerActions.get(playerID));
			}
		}	
	}
	
	/**
	 * Fill all land with this type.
	 * Create land first.
	 * @param type
	 */
	public void fillLand(int type) {
		land = new int[4][players.length];
		for(int x=0;x<land.length;x++) {
			for(int y=0;y<land[x].length;y++) {
				land[x][y] = type;
			}
		}
	}
	
	/**
	 * Swap land types from one type to the other. Return how many were swapped.
	 * @param fromType
	 * @param toType
	 * @param amount
	 * @return
	 */
	public int swap(int fromType, int toType, int amount) {
		int count = 0;
		
		if(amount == 0) {
			return 0;
		}
		
		for(int x=0;x<land.length;x++) {
			for(int y=0;y<land[x].length;y++) {
				if(land[x][y] == fromType) {
					land[x][y] = toType;
					count++;
					if(count >= amount) {
						return count;
					}
				}
			}	
		}
		return count;
	}
	
	/**
	 * See how many of a given land type are available.
	 * @return
	 */
	public int countLandType(int landType) {
		int count = 0;
		for(int x=0;x<land.length;x++) {
			for(int y=0;y<land[x].length;y++) {
				if(land[x][y] == landType) {
					count++;
				}
			}	
		}
		return count;
	}
		
	/**
	 * This is for debugging. Show the board.
	 */
	public void showBoard(GameContext context) {
		for(int x=0;x<land.length;x++) {
			for(int y=0;y<land[x].length;y++) {
				System.out.print("[" + context.landTypes.get(land[x][y]).name + "]");
			}
			System.out.println();
		}
		
		for(int playerID=0;playerID<players.length;playerID++) {
			System.out.println("Player " + playerID);
			for(Resource resource : context.landTypes.values()) {
				int count = players[playerID].getResourceCount(resource.id);
				String name = resource.name;
				System.out.println("  [" + name + "] " + count);
			}
		}
	}
}
