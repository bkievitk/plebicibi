package mainGame;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A cost for an action.
 * @author iami
 */

public class Cost implements Serializable {

	private static final long serialVersionUID = 4012094795172745214L;
	
	// Resource id by amount of resources.
	public Hashtable<Integer,Integer> resources = new Hashtable<Integer,Integer>();
	
	// Turns to make.
	public int turns;
	
	// Actions to pay (remove).
	public Vector<String> actions = new Vector<String>();
	
	@SuppressWarnings("unchecked")
	public Cost clone() {
		Cost newCost = new Cost();
		newCost.resources = (Hashtable<Integer,Integer>)resources.clone();
		newCost.turns = turns;
		newCost.actions = (Vector<String>)actions.clone();
		return newCost;
	}
	
	public boolean canPay(Player player) {
		
		// Check resources.
		for(Integer key : resources.keySet()) {
			if(player.getResourceCount(key) < resources.get(key)) {
				return false;
			}
		}
		
		// Check actions.
		for(String action : actions) {
			if(player.getAction(action) == null) {
				return false;
			}
		}
		
		return true;
	}
	
	public void pay(Player player) {
		
		// Remove resources.
		for(Integer key : resources.keySet()) {
			player.setResourceCount(key, player.getResourceCount(key) - resources.get(key));
		}
		
		// Remove actions.
		for(String action : actions) {
			player.removeAction(action);
		}
	}
}
