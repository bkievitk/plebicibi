package mainGame;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Represents a player with resources and abilities.
 * @author iami
 */

public class Player implements Serializable {
	
	private static final long serialVersionUID = 2418008808063577839L;
	
	// List of resources.
	private Hashtable<Integer,Integer> resources = new Hashtable<Integer,Integer>();
	
	// List of actions.
	public Vector<GameAction> availableActions = new Vector<GameAction>();
	
	public int playerID;
			
	public String toString() {
		return "player: " + playerID;
	}
	
	public Player(GameContext context, int playerID) {
		this.playerID = playerID;
		resetResources();
	}
	
	public void resetResources() {
		resources.clear();
	}
	
	public int getResourceCount(int id) {
		Integer i = resources.get(id);
		if(i == null) {
			return 0;
		} else {
			return i;
		}
	}
		
	public void setResourceCount(int id, int val) {
		resources.put(id, val);
	}
	
	public void addResourceCount(int id, int val) {
		Integer i = resources.remove(id);
		if(i == null) {
			resources.put(id, val);
		} else {
			resources.put(id, val + i);
		}
	}
	
	public GameAction getAction(String actionName) {
		for(GameAction action : availableActions) {
			if(action.name.equals(actionName)) {
				return action;
			}
		}
		return null;
	}
	
	public GameAction removeAction(String actionName) {
		for(int i=0;i<availableActions.size();i++) {
			GameAction action = availableActions.get(i);
			if(action.name.equals(actionName)) {
				availableActions.remove(i);
				return action;
			}
		}
		return null;
	}
}
