package mainGame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Semaphore;

import javax.swing.*;

import mainGame.gui.*;

public class GameScript {
	
	// Variables stored in this action.
	private Hashtable<String,String> variables = new Hashtable<String,String>();
	private static Hashtable<String,String> variablesGlobal = new Hashtable<String,String>();
	public String result;
	
	public int playerID;
	public Board board;
	public GameContext context;
	public ObjectInputStream input;
	public ObjectOutputStream output;
	public BufferedWriter messages;
	public GUIRequests gui;

	public static final Random rand = new Random();
	
	public static void main(String[] args) {
		/*HashSet<String> standardFunctionHash = new HashSet<String>();
		standardFunctionHash.add("main");
		standardFunctionHash.add("runProgram");
		standardFunctionHash.add("runLine");
		standardFunctionHash.add("breakParameters");
		standardFunctionHash.add("notify");
		standardFunctionHash.add("notifyAll");
		standardFunctionHash.add("wait");
		standardFunctionHash.add("equals");
		standardFunctionHash.add("toString");
		standardFunctionHash.add("hashCode");
		standardFunctionHash.add("getClass");

		// Additional methods.
		// goto(lineNumber,booleanFunction);
		// if(booleanFunction,functionToRunIfTrue,functionToRunIfFalse);
		// for(startFunction, conditionFunction, updateFunction, function);
		// foreach(function); automatically sets local variable x and y for each field location.
				
		for(Method method : GameScript.class.getMethods()) {
			if(!standardFunctionHash.contains(method.getName())) {
				System.out.println(method.getName() + "()");
			}
		}
		*/
		
		XMLParser xml = null;
		GameContext context = null;
		try {
			// Extract all actions.
			xml = new XMLParser();
			String location = "XML/Contexts/context_gold.xml";
			BufferedReader r = new BufferedReader(new FileReader(new File(location)));
			context = xml.parseGameContext(r, xml.actions,location);
			r.close();
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// Create board.
		Board board = new Board(context);

		//System.out.println(board.context.getAction("testFunction").programs[Action.ON_PERFORM]);
		//System.out.println(board.context.getAction("simpleTrain").programs[Action.ON_PERFORM]);
		
		//System.out.println(board.context.getAction("testFunction").programs[Action.ON_PERFORM]);
		//new GameScript(board.context.getAction("testFunction").programs[Action.ON_PERFORM],0,board,null,null);

		//setValue(overshoot,minusInt(getCommunalResourceCount($Warriors),plusInt(getCommunalResourceCount($Priests),getCommunalResourceCount($Farmers))));for(setValue(i,0),lessThanInt(getValue(i),getValue(overshoot)),plusPlus(i),if(equalTo(getRandom(2),0),if(equalTo(swapNotType($Warriors,$Wilderness,1),1),nullOp(),swapType($Warriors,$Wilderness,1))))
		//new GameScript(xml.replaceVariables("setValue(overshoot,minusInt(plusInt(getCommunalResourceCount($Priests),getCommunalResourceCount($Warriors)),timesInt(2,getCommunalResourceCount($Farmers)));print(getValue(overshoot));"),0,board,null,null);
		
		//setResourceCount(0,5)
		new GameScript(xml.replaceVariables("sendMessage(test);"),0,board,context,null,null,null);
		
		/*for(int playerID=0;playerID<board.players.length;playerID++) {
			System.out.println(playerID);
			for(Action action : context.playerActions.get(playerID)) {
				System.out.println("   [" + action + "]");
			}
		}*/

		board.showBoard(context);
		
	}
	
	/**
	 * Create a new game script and run it.
	 * @param program
	 * @param playerID
	 * @param board
	 * @param input
	 * @param output
	 */
	public GameScript(String program, int playerID, Board board, GameContext context, ObjectInputStream input, ObjectOutputStream output, BufferedWriter messages) {
		this.playerID = playerID;
		this.context = context;
		this.board = board;
		this.input = input;
		this.output = output;
		this.messages = messages;
		gui = new SimpleGUIRequests();
		runProgram(program);
	}
	
	/**
	 * Run this program code as a set of lines.
	 * @param program
	 * @param board
	 * @param playerID
	 */
	private void runProgram(String program) {

		try {
			//System.out.println("Running program [" + program + "]");
			
			// No program.
			if(program == null) {
				return;
			}
			
			result = "";
			
			// Split into lines.
			String[] lines = program.split(";");
			for(int lineID=0;lineID<lines.length;lineID++) {
				String line = lines[lineID];
				
				if(line.startsWith("goto(")) {
					int start = line.indexOf(',');
					
					// Line id.
					int gotoLineID = Integer.parseInt(line.substring(5,start));
					
					// Line function.
					String gotoLineFunction = line.substring(start+1,line.length());
					
					System.out.println(gotoLineFunction);
					
					// Result.
					String lineResult = runLine(gotoLineFunction);
					
					// If true, then jump to line.
					if(lineResult.equals("true")) {
						lineID = gotoLineID-1;
					}				
				} else {
					String lineResult = runLine(line);
					if(lineResult != null) {
						result += lineResult;
					}
				}
			}
		} catch(Exception e) {
		}
	}

	/**
	 * Run this line of program code.
	 * @param line
	 * @param board
	 * @param playerID
	 * @return
	 */
	public String runLine(String line) {
		
		// Find the beginning and end of the arguments part.
		int start = line.indexOf('(');
		int stop = line.lastIndexOf(')');
		
		if(start < 0 || stop < 0) {
			return null;
		}
		
		String function = line.substring(0,start).trim();
		Vector<String> parameters = breakParameters(line.substring(start+1,stop));
		
		//System.out.println("Running function [" + function + "]");
		
		// The 'if' conditional.
		if(function.equals("if")) {
			
			// Check the result of the first argument.
			String conditional = runLine(parameters.get(0));
			if(conditional.equals("true")) {
				// If "true", run the second argument.
				return runLine(parameters.get(1));
			} else {
				// Otherwise run the third.
				return runLine(parameters.get(2));
			}
		} else if(function.equals("noCompile")) {
			// Do not compile the contents of this function.
			return line.substring(start+1,stop);
		} else if(function.equals("for")) {
			// for(start, condition, update, function);
			String result = "";
			
			String forStart = parameters.get(0);
			String forCondition = parameters.get(1);
			String forUpdate = parameters.get(2);
			String forFunction = parameters.get(3);
			for(runLine(forStart); runLine(forCondition).equals("true"); runLine(forUpdate)) {
				result += runLine(forFunction);
			}
			
			return result;
		} else if(function.equals("foreach")) {
			String result = "";
			for(int x=0;x<board.land.length;x++) {
				setValue("x",x + "");
				for(int y=0;y<board.land[x].length;y++) {
					setValue("y",y + "");
					result += runLine(parameters.get(0));
				}	
			}
			return result;
		} else {
			// Calculate all arguments.
			for(int i=0;i<parameters.size();i++) {
				if(parameters.get(i).contains("(")) {
					parameters.set(i, runLine(parameters.get(i)));
				}
			}
			
			Object[] parameterArray = parameters.toArray();
			@SuppressWarnings("rawtypes")
			Class[] parameterArrayClass = new Class[parameterArray.length];
			for(int i=0;i<parameterArray.length;i++) {
				parameterArrayClass[i] = parameterArray[i].getClass();
			}
			
			// Run the function.
			Method method = null;
			try {				
				method = GameScript.class.getMethod(function, parameterArrayClass);
				String result = (String)method.invoke(this, parameterArray);				
				return result;				
			} catch (Exception e) {
				System.out.println("full line: [" + line + "][" + function + "]");
				System.out.println("error invoking [" + method + "] with paramters ");
				for(Object parameter : parameterArray) {
					System.out.println("  " + parameter + " [" + parameter.getClass() + "]");
				}
				e.printStackTrace();
			}
			
			// On error, return null;
			return null;
		}
	}
	
	/**
	 * Break a set of parameters into sub strings.
	 * @param string
	 * @return
	 */
	private Vector<String> breakParameters(String string) {
		int open = 0;
		int lastPoint = 0;
		Vector<String> parameters = new Vector<String>();
		
		for(int i=0;i<string.length();i++) {
			if(string.charAt(i) == '(') {
				open ++;
			} else if(string.charAt(i) == ')') {
				open --;
			} else if(open == 0 && string.charAt(i) == ',') {
				String parameter = string.substring(lastPoint,i).trim();
				if(parameter.length() > 0) {
					parameters.add(parameter);
				}
				lastPoint = i+1;
			}
		}
		
		String parameter = string.substring(lastPoint,string.length()).trim();
		if(parameter.length() > 0) {
			parameters.add(parameter);
		}
		
		return parameters;
	}
	
	/***********************************************************************************************************************************************************/
	/*                                                                                                                                                         */
	/*                                                                                                                                                         */
	/*                                                             Available Functions                                                                         */
	/*                                                                                                                                                         */
	/*                                                                                                                                                         */
	/***********************************************************************************************************************************************************/
	
	public String sendMessage(String message) {
		try {
			messages.write(message + "\n");
			messages.flush();
		} catch(Exception e) {}
		return message;
	}
	
	/**
	 * Returns the number of players playing.
	 * @return
	 */
	public String numPlayers() {
		return board.players.length + "";
	}

	/**
	 * Attack in some form.
	 * @return			null.
	 */
	public String attack() {
		return null;
	}

	public String swapNotType(String notToLandTypeS, String toLandTypeS, String amountS) {

		int notToLandType = Integer.parseInt(notToLandTypeS);
		int toLandType = Integer.parseInt(toLandTypeS);
		int amount = Integer.parseInt(amountS);
		
		Vector<Point> points = new Vector<Point>();
		for(int x=0;x<board.land.length;x++) {
			for(int y=0;y<board.land[x].length;y++) {
				if(board.land[x][y] != notToLandType && board.land[x][y] != toLandType) {
					points.add(new Point(x,y));
				}
			}	
		}

		int count = 0;
		
		for(int i=0;i<amount;i++) {
			if(points.size() > 0) {
				Point selected = points.get(rand.nextInt(points.size()));
				board.land[selected.x][selected.y] = toLandType;
				count ++;
			} else {
				break;
			}
		}		
		
		return count + "";
	}
	
	/**
	 * Replace all from land types with to land types. Add each to land type to store.
	 * @param fromLandType	Type of land replaced.
	 * @param toLandType	Type of land to replace with.
	 * @param amount		Amount of land.
	 * @return				null.
	 */
	public String swapTypeTake(String fromLandTypeS, String toLandTypeS, String amountS) {
		
		int fromLandType = Integer.parseInt(fromLandTypeS);
		int toLandType = Integer.parseInt(toLandTypeS);
		int amount = Integer.parseInt(amountS);
		
		// Perform swap.
		int amountValid = board.swap(fromLandType, toLandType, amount);
		
		// Add to players resources.
		board.players[playerID].addResourceCount(fromLandType,amountValid);
		
		return amountValid + "";
	}

	/**
	 * Replace all from land types with to land types.
	 * @param fromLandTypeS	Type of land replaced.
	 * @param toLandTypeS	Type of land to replace with.
	 * @param amountS		Amount of land.
	 * @return				null.
	 */
	public String swapType(String fromLandTypeS, String toLandTypeS, String amountS) {
		
		int fromLandType = Integer.parseInt(fromLandTypeS);
		int toLandType = Integer.parseInt(toLandTypeS);
		int amount = Integer.parseInt(amountS);
		
		// Perform swap.
		int amountValid = board.swap(fromLandType, toLandType, amount);
		
		return amountValid + "";
	}
	
	/**
	 * Set a local variable value.
	 * @param variable	Variable name
	 * @param value		Value
	 * @return
	 */
	public String setValue(String variable, String value) {
		variables.put(variable, value);
		return value;
	}
	
	/**
	 * Get the value of a local variable.
	 * @param variable
	 * @return
	 */
	public String getValue(String variable) {
		return variables.get(variable);
	}
	
	/**
	 * Set a global variable value.
	 * @param variable	Variable name
	 * @param value		Value
	 * @return
	 */
	public String setValueGlobal(String variable, String value) {
		variablesGlobal.put(variable, value);
		return value;
	}
	
	/**
	 * Get the value of a global variable.
	 * @param variable
	 * @return
	 */
	public String getValueGlobal(String variable) {
		return variablesGlobal.get(variable);
	}
	
	/**
	 * Print to standard out.
	 * @param text
	 * @return
	 */
	public String print(String text) {
		System.out.println("[" + text + "]");
		JOptionPane.showMessageDialog(null, text, "Message", JOptionPane.INFORMATION_MESSAGE);
		return text;
	}
	
	/**
	 * Ask the server to request an action card from the client.
	 * @param vals
	 * @param server
	 * @param playerID
	 * @return
	 */
	public String takeActionServer() {

		// Ask the player to take a client action.
		try {
			output.writeObject("takeActionClient();");
			GameAction action = (GameAction)input.readObject();
			board.players[playerID].availableActions.add(action);
			
			// Perform purchase action.
			GameScript script = new GameScript(action.programs[GameAction.ON_PURCHASE],playerID,board,context,input,output,messages);
			return script.result;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Select an action card and send it back.
	 * @return
	 */
	public String takeActionClient() {
		
		// Request action from user.
		final Semaphore s = new Semaphore(1);
		final JFrame takeFrame = new JFrame();
		
		JPanel takePanel = new JPanel(new GridLayout(0,5));
		takePanel.setBackground(Color.WHITE);
		
		if(context.allActions != null) {
			int numActions = context.allActions.size();
			for(int i=0;i<numActions;i++) {
				BufferedImage img = new BufferedImage(100,200,BufferedImage.TYPE_INT_RGB);
				
				final GameAction action = context.allActions.get(i);
				
				action.render(img.getGraphics(), new Rectangle(0,0,img.getWidth()-1,img.getHeight()-1));
				JButton button = new JButton(new ImageIcon(img));
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						try {
							output.writeObject(action);
						} catch (IOException e) {
							e.printStackTrace();
						}
						takeFrame.dispose();
						s.release();
					}
				});
				button.setBackground(Color.WHITE);
				button.setPreferredSize(new Dimension(img.getWidth(),img.getHeight()));
				takePanel.add(button);
			}
		}
				
		takeFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		takeFrame.setSize(400, 100);
		takeFrame.add(new JScrollPane(takePanel));
		takeFrame.setVisible(true);
		
		try {
			s.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
		// Wait till close.
		return null;
	}
	
	/**
	 * Return number of resources of this type owned by this player.
	 * @param resourceIDS
	 * @return
	 */
	public String getResourceCount(String resourceIDS) {
		int resourceID = Integer.parseInt(resourceIDS);
		return board.players[playerID].getResourceCount(resourceID) + "";
	}

	/**
	 * Set the number of resources of this type owned by this player.
	 * @param resourceIDS
	 * @param amountS
	 * @return
	 */
	public String setResourceCount(String resourceIDS, String amountS) {
		int resourceID = Integer.parseInt(resourceIDS);
		int amount = Integer.parseInt(amountS);		
		board.players[playerID].setResourceCount(resourceID, amount);
		return amountS;
	}	

	/**
	 * Return number of resources of this type owned by this player.
	 * @param resourceIDS
	 * @return
	 */
	public String getResourceCount(String resourceIDS, String userIDS) {
		int resourceID = Integer.parseInt(resourceIDS);
		int userID = Integer.parseInt(userIDS);
		return board.players[userID].getResourceCount(resourceID) + "";
	}

	/**
	 * Set the number of resources of this type owned by this player.
	 * @param resourceIDS
	 * @param amountS
	 * @return
	 */
	public String setResourceCount(String resourceIDS, String amountS, String userIDS) {
		int resourceID = Integer.parseInt(resourceIDS);
		int amount = Integer.parseInt(amountS);		
		int userID = Integer.parseInt(userIDS);
		board.players[userID].setResourceCount(resourceID, amount);
		return amountS;
	}

	/**
	 * Get number of communal resources of this type.
	 * @param resourceIDS
	 * @return
	 */
	public String getCommunalResourceCount(String resourceIDS) {
		int resourceID = Integer.parseInt(resourceIDS);
		return board.countLandType(resourceID) + "";
	}
	
	/**
	 * Get a random integer.
	 * @param nS
	 * @return
	 */
	public String getRandom(String nS) {
		int n = Integer.parseInt(nS);
		return rand.nextInt(n) + "";
	}
	
	/**
	 * Set the land plot at x,y to be of a given type.
	 * @param xS
	 * @param yS
	 * @param typeS
	 * @return
	 */
	public String setLand(String xS, String yS, String typeS) {
		int x = Integer.parseInt(xS);
		int y = Integer.parseInt(yS);
		int type = Integer.parseInt(typeS);
		board.land[x][y] = type;
		return "";
	}

	/**
	 * Nothing happens.
	 */
	public void nullOp() {}	

	/**
	 * Add one to a local value.
	 * @param variable
	 * @return
	 */
	public String plusPlus(String variable) { 
		int val = Integer.parseInt(getValue(variable));
		setValue(variable,(val+1) + "");
		return null;
	}
	
	/************************************/
	/*    User input request functions  */
	/************************************/
	
	/**
	 * Write this data out to the output as a string.
	 * @param string
	 * @return
	 */
	public String write(String string) {
		try {
			output.writeObject(string);
			output.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Read a string from the stream.
	 * @param string
	 * @return
	 */
	public String read() {
		try {
			return (String)input.readObject();
		} catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Write this data out to the output as a string and then read the results back.
	 * @param function
	 * @return
	 */
	public String writeRead(String function) {
		try {
			output.writeObject(function);
			output.flush();
			return (String)input.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Show string input dialog.
	 * @param message
	 * @return
	 */
	public String requestString(String message) {
		return gui.requestString(board, message);
	}

	/**
	 * Show timed string input dialog.
	 * @param message
	 * @param time
	 * @param defaultValue
	 * @return
	 */
	public String requestString(String message, String time, String defaultValue) {		
		return gui.requestString(board, message,time,defaultValue);
	}
	
	/**
	 * Show boolean input dialog.
	 * @param message
	 * @return
	 */
	public String requestBoolean(String message) {		
		return gui.requestBoolean(board, message);
	}
	
	/**
	 * Show timed input boolean dialog.
	 * @param message
	 * @param time
	 * @param defaultValue
	 * @return
	 */
	public String requestBoolean(String message, String time, String defaultValue) {		
		return gui.requestBoolean(board, message, time, defaultValue);
	}
	
	/**
	 * Show window to allow player to select another player.
	 * @param message
	 * @return
	 */
	public String requestPlayer(String message) {		
		return gui.requestPlayer(board, message);
	}
	
	/**
	 * Show window to allow player to select another player that is timed.
	 * @param message
	 * @param time
	 * @param defaultValue
	 * @return
	 */
	public String requestPlayer(String message, String time, String defaultValue) {		
		return gui.requestPlayer(board, message, time, defaultValue);
	}
	
	/**
	 * Show window to allow player to select land tile.
	 * @param message
	 * @return
	 */
	public String requestLandTile(String message) {	
		return gui.requestLandTile(board, message,context);
	}
	
	/**
	 * Show window to allow player to select land tile that is timed.
	 * @param message
	 * @param time
	 * @param defaultValue
	 * @return
	 */
	public String requestLandTile(String message, String time, String defaultValue) {		
		return gui.requestLandTile(board, message, time, defaultValue, context);
	}
	
	/**
	 * Show window to allow player to select action card.
	 * @param message
	 * @return
	 */
	public String requestCard(String message) {	
		return gui.requestCard(board, message, context);
	}

	/**
	 * Show window to allow player to select action card that is timed.
	 * @param message
	 * @param time
	 * @param defaultValue
	 * @return
	 */
	public String requestCard(String message, String time, String defaultValue) {		
		return gui.requestCard(board, message, time, defaultValue, context);
	}

	/******************************/
	/*    Conditional Logic       */
	/******************************/
	public String equalTo(String v1, String v2) { return v1.equals(v2) + ""; }	
	public String equalToInt(String v1, String v2) { return (Integer.parseInt(v1) == Integer.parseInt(v2)) + ""; }	
	public String lessThanInt(String v1, String v2) { return (Integer.parseInt(v1) < Integer.parseInt(v2)) + ""; }	
	public String greaterThanInt(String v1, String v2) { return (Integer.parseInt(v1) > Integer.parseInt(v2))  + ""; }
	public String equalToDouble(String v1, String v2) { return (Double.parseDouble(v1) == Double.parseDouble(v2)) + ""; }	
	public String lessThanDouble(String v1, String v2) { return (Double.parseDouble(v1) < Double.parseDouble(v2)) + ""; }	
	public String greaterThanDouble(String v1, String v2) { return (Double.parseDouble(v1) > Double.parseDouble(v2))  + ""; }

	public String and(String v1, String v2) { if(v1.equals("true") && v2.equals("true")) { return "true"; } else { return "false"; } }
	public String or(String v1, String v2) { if(v1.equals("true") || v2.equals("true")) { return "true"; } else { return "false"; } }
	public String not(String v1) { if(v1.equals("true")) { return "false"; } else { return "true"; } }

	/*****************/
	/*    Math       */
	/*****************/
	public String timesInt(String v1, String v2) { return (Integer.parseInt(v1) * Integer.parseInt(v2))  + ""; }
	public String divideInt(String v1, String v2) { return (Integer.parseInt(v1) / Integer.parseInt(v2))  + ""; }
	public String plusInt(String v1, String v2) { return (Integer.parseInt(v1) + Integer.parseInt(v2))  + ""; }
	public String minusInt(String v1, String v2) { return (Integer.parseInt(v1) - Integer.parseInt(v2))  + ""; }
	
}
