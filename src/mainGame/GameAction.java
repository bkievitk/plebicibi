package mainGame;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;

import mainGame.run.Server;

/**
 * This is an action owned by a player.
 * It has costs and effects on the board.
 * @author iami
 */

public class GameAction implements Serializable {

	private static final long serialVersionUID = 7109538194954366789L;
	
	// Costs to buy and remove.
	public Cost costToBuy;
	public Cost costToRemove;

	// Action type IDs.
	public static final int ON_PURCHASE = 0;
	public static final int ON_TURN = 1;
	public static final int ON_PERFORM = 2;
	public static final int ON_END = 3;

	// Programs to run based on action id.
	public String[] programs = new String[4];

	// Order that cards are played.
	public int order;
	
	// Type.
	public String type;
	
	// Called name.
	public String name;
	
	// Description for user.
	public String description;
	
	// String names to be replaced in code and description.
	public String[] bindings;
	
	public boolean selectable = true;
	
	public String toString() {
		return name;
	}
	
	/**
	 * Check that descriptions match.
	 */
	public boolean equals(Object o) {
		if(o instanceof GameAction) {
			return ((GameAction) o).description.equals(description);
		} else {
			return false;
		}
	}
	
	/**
	 * Create a copy.
	 */
	public GameAction clone() {
		GameAction newAction = new GameAction();
		if(costToBuy != null) { newAction.costToBuy = costToBuy.clone(); }
		if(costToRemove != null) { newAction.costToRemove = costToRemove.clone(); }
		newAction.programs = programs.clone();
		newAction.order = order;
		newAction.type = type;
		newAction.name = name;
		newAction.description = description;
		newAction.bindings = bindings.clone();
		newAction.selectable = selectable;
		return newAction;
	}

	/**
	 * Bind all of the bindings in the code and description with these values.
	 * @param bind
	 */
	public void performBindings(String[] bind) {
		if(bind.length != bindings.length) {
			System.out.println("Bind length mismatch.");
			for(int i=0;i<bind.length;i++) {
				System.out.print("[" + bind[i] + "]");
			}
			System.out.println();
			for(int i=0;i<bindings.length;i++) {
				System.out.print("[" + bindings[i] + "]");
			}
			System.out.println();
			System.exit(0);
		} else {
			if(bindings != null) {
				for(int i=0;i<bind.length;i++) {
					for(int j=0;j<programs.length;j++) {
						if(programs[j] != null) {
							programs[j] = programs[j].replaceAll(bindings[i], bind[i]);
						}
					}
					if(description != null) {
						description = description.replaceAll(bindings[i], bind[i]);
					}
				}
			}
		}
	}
	
	/**
	 * Run this program code as a set of lines.
	 * @param programID
	 * @param board
	 * @param playerID
	 */
	public String runProgram(int programID, Server server, int playerID) {
		return runProgram(programs[programID], server, playerID);
	}
	
	/**
	 * Run this program code as a set of lines.
	 * @param program
	 * @param board
	 * @param playerID
	 */
	public String runProgram(String program, Server server, int playerID) {
		return (new GameScript(program,playerID,server.board,server.context,server.readers.get(playerID),server.writers.get(playerID),server.hiddenChatClient)).result;
	}

	/**
	 * Draw.
	 * @param g
	 * @param rect
	 */
	public void render(Graphics g, Rectangle rect) {
				
		if(type.equals("sanction")) {
			g.setColor(new Color(205,255,255));
		} else if(type.equals("build")) {
			g.setColor(new Color(255,205,255));
		} else if(type.equals("improve")) {
			g.setColor(new Color(255,255,205));
		} else if(type.equals("plant")) {
			g.setColor(new Color(205,205,255));
		} else if(type.equals("harvest")) {
			g.setColor(new Color(205,255,205));
		} else if(type.equals("ambition")) {
			g.setColor(new Color(255,205,205));
		} else if(type.equals("info")) {
			g.setColor(new Color(205,205,205));
		} else {
			g.setColor(new Color(255,255,255));
		}
		
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
		
		g.setColor(Color.BLACK);
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
		
		g.drawString("order: " + order, rect.x + 2, rect.y + 14);
		renderStringWrap(g, description, new Point(rect.x + 2, rect.y + 14 + 12), rect.width - 4);
    }
	
	/**
	 * Render a long string by breaking it on the word wrap length.
	 * @param g
	 * @param sentence
	 * @param start
	 * @param width
	 */
	public static void renderStringWrap(Graphics g, String sentence, Point start, int width) {
		String[] words = sentence.split(" +");
		FontMetrics fm = g.getFontMetrics();
		int x = start.x;
		int y = start.y;
		
		for(int i=0;i<words.length;i++) {
			int newX = x + fm.stringWidth(words[i]);
			if(newX > start.x + width) {
				if(start.x == x) {
					g.drawString(words[i], x, y);
					y += fm.getHeight();
				} else {
					x = start.x;
					y += fm.getHeight();
					g.drawString(words[i], x, y);
					x += fm.stringWidth(words[i]) + 4;
				}
			} else {
				g.drawString(words[i], x, y);
				x += fm.stringWidth(words[i] + 4);
			}
		}
	}	
}
