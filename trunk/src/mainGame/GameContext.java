package mainGame;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ImageIcon;

/**
 * This game can be played in many contexts.
 * This class specifies the names and images to use for a given context.
 * The set of available actions is also specified here.
 * @author iami
 */

public class GameContext implements Serializable {

	private static final long serialVersionUID = -3767724201559887196L;
	
	// Land.
	public Hashtable<Integer,Resource> landTypes = new Hashtable<Integer,Resource>();
	
	// Actions.
	public Vector<GameAction> allActions = new Vector<GameAction>();
	public Hashtable<Integer,Vector<GameAction>> playerActions = new Hashtable<Integer,Vector<GameAction>>();
	public Hashtable<GameAction,ImageIcon> actionImages = new Hashtable<GameAction,ImageIcon>();
	
	// Events.
	public Vector<WorldEvent> allEvents = new Vector<WorldEvent>();
	
	// Other.
	public String backStory;	
	public int numTurns;
	public int numPlayers;	
	public int[][] startingBoard;	
	public int time = -1;
	
	public Hashtable<Integer,PlayerContext> players = new Hashtable<Integer,PlayerContext>();
	
	public void addPlayerAction(int id, GameAction action) {
		Vector<GameAction> actions = playerActions.get(id);
		if(actions == null) {
			actions = new Vector<GameAction>();
			playerActions.put(id, actions);
		}
		actions.add(action);
	}
	
	public GameAction getAction(String actionName) {
		for(GameAction action : allActions) {
			if(action.name.equals(actionName)) {
				return action;
			}
		}
		return null;
	}
}
