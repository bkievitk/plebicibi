package mainGame;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class XMLParser {

	// Line that you are reading.
	public int lineID = 0;
	
	// Hash of variable definitions.
	public Hashtable<String,String> defs = new Hashtable<String,String>();
	
	// List of events.
	public Hashtable<String,WorldEvent> events = new Hashtable<String,WorldEvent>();
	
	// List of actions.
	public Hashtable<String,GameAction> actions = new Hashtable<String,GameAction>();
		
	public int verbosity = 10;
	
	/**
	 * Parse a context file.
	 * @param r
	 * @param actions
	 * @return
	 * @throws IOException
	 */
	public GameContext parseGameContext(BufferedReader r, Hashtable<String,GameAction> actions, String location) throws IOException {
		int lineID = 0;

		if(verbosity > 8) { System.out.println("START_CONTEXT ----------------------------------------------"); };
		if(verbosity > 8) { System.out.println("  Parsing game context from [" + location + "]"); };
		
		// Build context.
		GameContext context = new GameContext();
		
		String line;
		while((line = r.readLine()) != null) {
			lineID++;
			
			if(line.trim().startsWith("//")) {
			} else if(line.contains("<def>")) {
				// A definition of a new resource.
				extractDef(r);				
			} else if(line.contains("<context>")) {
			} else if(line.contains("<time>")) {
				context.time = Integer.parseInt(extractContents(line));
			} else if(line.contains("<board>")) {	
				

				if(verbosity > 8) { System.out.println("    Defining board"); };
				
				// Land definition.
				while((line = r.readLine()) != null) {
					lineID++;					
					if(line.trim().startsWith("//")) {
					} else if(line.contains("<size>")) {
						String[] xy = extractContents(line).split(",");
						context.startingBoard = new int[Integer.parseInt(xy[0])][Integer.parseInt(xy[1])];
					} else if(line.contains("<line>")) {
						String[] parts = extractContents(line).split(",");
						int x = Integer.parseInt(parts[0]);
						for(int y=0;y<context.startingBoard[x].length;y++) {
							context.startingBoard[x][y] = Integer.parseInt(parts[y+1]);
						}
					} else if(line.contains("</board>")) {
						break;
					} else {
						baseConditions(line,lineID);
					}
				}
					
			} else if(line.contains("<include>")) {
				
				// Include.
				String file = extractContents(line);
				File f = new File(file);

				if(verbosity > 3) { System.out.println("    Including file [" + f.getAbsolutePath() + "]"); };
				
				BufferedReader includeReader = new BufferedReader(new FileReader(new File(file)));
				parseActionEvents(includeReader);
			} else if(line.contains("<landType>")) {
				

				if(verbosity > 3) { System.out.println("    Defining land type."); };
				
				// Define a land type.				
				int id = 0;
				String image = null;
				String name = null;
				boolean visible = false;
				
				while((line = r.readLine()) != null) {
					lineID++;
					
					if(line.trim().startsWith("//")) {
					} else if(line.contains("<id>")) {
						// id
						id = Integer.parseInt(extractContents(line));
					} else if(line.contains("<image>")) {
						// image
						image = extractContents(line);
					} else if(line.contains("<name>")) {
						// name
						name = extractContents(line);
					} else if(line.contains("<visible>")) {
						visible = extractContents(line).equals("true");
					} else if(line.contains("</landType>")) {						
						try {
							ImageIcon imageIcon = null;
							if(image != null) {
								imageIcon = new ImageIcon(ImageIO.read(new File(image)));
							}
							context.landTypes.put(id,new Resource(id,imageIcon,name,visible));
						} catch(IOException e) {
							System.out.println("    Unable to read image [" + image + "]");
							throw(e);
						}
						
						break;
					} else {
						baseConditions(line,lineID);
					}
				}
			} else if(line.contains("<availableActions>")) {
				

				if(verbosity > 3) { System.out.println("    Defining available actions."); };
				
				while((line = r.readLine()) != null) {
					lineID++;
					
					if(line.contains("<action>")) {
						
						String name = extractContents(line);
						int start = name.indexOf('[');
						int stop = name.indexOf(']');				
						if(start >= 0 && stop >= 0) {	
							
							String function = name.substring(0,start);
							
							if(!actions.containsKey(function)) {
								System.out.println("    Unable to find function [" + function + "] on line " + lineID);
							}
							
							GameAction action = actions.get(function).clone();
							action.performBindings(name.substring(start+1,stop).split(","));
							context.allActions.add(action);
						} else {
							context.allActions.add(actions.get(name));
						}
						
					} else if(line.contains("</availableActions>")) {
						break;
					} else {
						baseConditions(line,lineID);
					}
				}
			} else if(line.contains("<playerActions>")) {


				if(verbosity > 3) { System.out.println("    Defining player actions."); };
				
				PlayerContext player = new PlayerContext();
				while((line = r.readLine()) != null) {
					lineID++;
					if(line.contains("<playerID>")) {
						player.id = Integer.parseInt(extractContents(line));
						if(verbosity > 3) { System.out.println("      Player " + player.id); };
					} else if(line.contains("<name>")) {
						player.name = extractContents(line);
					} else if(line.contains("<description>")) {
						player.description = extractContents(line);
					} else if(line.contains("<image>")) {						
						File image = new File(extractContents(line));						
						if(!image.exists()) {
							System.out.println("Image [" + image + "] does not exist on line " + lineID);
						} else {
							BufferedImage image2 = ImageIO.read(image);
							BufferedImage newImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
							newImage.getGraphics().drawImage(image2, 0, 0, newImage.getWidth(), newImage.getHeight(), null);
							player.image = new ImageIcon(newImage);
						}
					} else if(line.contains("<action>")) {
						String name = extractContents(line);
						int start = name.indexOf('[');
						int stop = name.indexOf(']');				
						if(start >= 0 && stop >= 0) {							
							GameAction action = actions.get(name.substring(0,start)).clone();
							action.performBindings(name.substring(start+1,stop).split(","));
							context.addPlayerAction(player.id, action);
						} else {
							context.addPlayerAction(player.id, actions.get(name));
						}
						
						
						if(verbosity > 3) { System.out.println("      Action " + name); };
						
						
					} else if(line.contains("</playerActions>")) {
						context.players.put(player.id, player);
						break;
					}
				}
			} else if(line.contains("<playerCount>")) {
				context.numPlayers = Integer.parseInt(extractContents(line));
			} else if(line.contains("<turns>")) {
				context.numTurns = Integer.parseInt(extractContents(line));
			} else if(line.contains("<backstory>")) {
				context.backStory = "";
				
				while((line = r.readLine()) != null) {
					lineID++;
					
					if(line.trim().startsWith("//")) {
					} else if(line.contains("</backstory>")) {
						break;
					} else {
						context.backStory += line.trim() + "\n";
					}
				}
				
			} else if(line.contains("<event>")) {
				context.allEvents.add(events.get(extractContents(line)));
			} else if(line.contains("</context>")) {
			} else {
				baseConditions(line,lineID);
			}			
		}		

		// Sort on name.
		Collections.sort(context.allActions, new Comparator<GameAction>() {
			public int compare(GameAction arg0, GameAction arg1) {
				return arg0.name.compareTo(arg1.name);
			}
		});

		// Sort on order.
		Collections.sort(context.allActions, new Comparator<GameAction>() {
			public int compare(GameAction arg0, GameAction arg1) {
				return arg0.order - arg0.order;
			}
		});
		
		// Sort on type.
		Collections.sort(context.allActions, new Comparator<GameAction>() {
			public int compare(GameAction arg0, GameAction arg1) {
				return arg0.type.compareTo(arg1.type);
			}
		});
		
		if(verbosity > 8) { System.out.println("END_CONTEXT ----------------------------------------------"); };

		return context;
	}

	/**
	 * Check for comments, empty lines and unknown types.
	 * @param line
	 */
	private static void baseConditions(String line, int lineID) {
		line = line.trim();
		
		if(line.trim().startsWith("//")) {
		} else if(line.length() == 0) {
		} else {
			System.out.println("!!!! Unknown type [" + line + "] on line " + lineID);
		}
	}
	
	private void baseConditions(String line) {
		line = line.trim();
		
		if(line.trim().startsWith("//")) {
		} else if(line.length() == 0) {
		} else {
			System.out.println("!!!! Unknown type [" + line + "] on line " + lineID);
		}
	}
	
	/**
	 * Perform primary parse.
	 * @param r
	 * @throws IOException
	 */
	public void parseActionEvents(BufferedReader r) throws IOException {
				
		if(verbosity > 8) { System.out.println("START_EVENT ----------------------------------------------"); };
		
		String line;
		while((line = r.readLine()) != null) {
			lineID++;
			
			if(line.trim().startsWith("//")) {
			} else if(line.contains("<def>")) {
				// A definition of a new resource.
				extractDef(r);
			} else if(line.contains("<action>")) {
				// An action.
				extractAction(r);
			} else if(line.contains("<event>")) {
				// An event.
				extractEvent(r);
			} 
			
			// These are optional containers. Ignore.
			else if(line.contains("<actions>")) {
			} else if(line.contains("</actions>")) {
			} else if(line.contains("<events>")) {
			} else if(line.contains("</events>")) {
				
			} else {
				baseConditions(line);
			}
		}
		
		if(verbosity > 8) { System.out.println("END_EVENT ----------------------------------------------"); };

	}

	/**
	 * Extract a new event.
	 * @param r
	 * @throws IOException
	 */
	public void extractEvent(BufferedReader r) throws IOException {

		String name = null;
		String description = null;
		String program = null;
		
		String line;
		while((line = r.readLine()) != null) {
			lineID++;
			
			if(line.trim().startsWith("//")) {
			} else if(line.contains("<description>")) {
				// Extract description.
				description = extractContents(line);
			} else if(line.contains("<onTurn>")) {
				// Extract code.
				program = extractContents(line);
			} else if(line.contains("<name>")) {
				// Extract code.
				name = extractContents(line);
			} else if(line.contains("</event>")) {
				
				// Create the event class.
				WorldEvent event = new WorldEvent();
				event.description = description;
				event.program = program;
				event.name = name;
				events.put(event.name,event);
				return;
			} else {
				baseConditions(line);
			}
		}
	}

	/**
	 * Extract an action.
	 * @param r
	 * @throws IOException
	 */
	public void extractAction(BufferedReader r) throws IOException {
		
		GameAction action = new GameAction();

		if(verbosity > 8) { System.out.println("    Extracting action"); };
		
		String line;
		while((line = r.readLine()) != null) {
			lineID++;
			
			if(line.trim().startsWith("//")) {
			} else if(line.contains("<type>")) {
				action.type = extractContents(line);
			} else if(line.contains("<name>")) {	

				String name = extractContents(line);
				int start = name.indexOf('[');
				int stop = name.indexOf(']');				
				if(start >= 0 && stop >= 0) {
					action.bindings = name.substring(start+1,stop).split(",");
					action.name = name.substring(0,start);
				} else {
					action.name = name;
				}				
			} else if(line.contains("<selectable>")) {
				action.selectable = extractContents(line).equals("true");
			} else if(line.contains("<description>")) {
				action.description = extractContents(line);
			} else if(line.contains("<costToBuy>")) {
				action.costToBuy = extractCost(r,"</costToBuy>");
			} else if(line.contains("<costToRemove>")) {
				action.costToRemove = extractCost(r,"</costToRemove>");
			} else if(line.contains("<onTurn>")) {
				action.programs[GameAction.ON_TURN] = extractContents(line);
			} else if(line.contains("<onPerform>")) {
				action.programs[GameAction.ON_PERFORM] = extractContents(line);
			} else if(line.contains("<onPurchase>")) {
				action.programs[GameAction.ON_PURCHASE] = extractContents(line);
			} else if(line.contains("<onEndGame>")) {
				action.programs[GameAction.ON_END] = extractContents(line);
			} else if(line.contains("<order>")) {
				action.order = Integer.parseInt(extractContents(line));
			} else if(line.contains("<effects>")) {
			} else if(line.contains("</effects>")) {
			} else if(line.contains("</action>")) {
				

				if(verbosity > 8) { System.out.println("      Adding action [" + action.name + "]"); };
				
				actions.put(action.name,action);
				return;
			} else {
				baseConditions(line);
			}
		}
	}
		
	public Cost extractCost(BufferedReader r, String endTag) throws IOException {
				
		Cost cost = new Cost();
		
		String line;
		while((line = r.readLine()) != null) {
			lineID++;
			
			if(line.trim().startsWith("//")) {
			} else if(line.contains("<resources>")) {
				cost.resources = extractResources(r);
			} else if(line.contains("<actionTurns>")) {
				cost.turns = Integer.parseInt(extractContents(line));
			} else if(line.contains("<actionCard>")) {
				cost.actions.add(extractContents(line));
			} else if(line.contains(endTag)) {
				return cost;
			} else {
				baseConditions(line);
			}
		}
		
		return null;
	}
	
	public Hashtable<Integer,Integer> extractResources(BufferedReader r) throws IOException {
		
		Hashtable<Integer,Integer> resources = new Hashtable<Integer,Integer>();
		
		int type = 0;
		int count = 0;
		
		String line;
		while((line = r.readLine()) != null) {
			lineID++;
			
			if(line.trim().startsWith("//")) {
			} else if(line.contains("<resourceType>")) {
				type = Integer.parseInt(extractContents(line));
			} else if(line.contains("<resourceCount>")) {
				count = Integer.parseInt(extractContents(line));
			} else if(line.contains("<resource>")) {
			} else if(line.contains("</resource>")) {
				resources.put(type, count);
			} else if(line.contains("</resources>")) {
				return resources;
			} else {
				baseConditions(line);
			}
		}
		
		return null;
	}
	
	public void extractDef(BufferedReader r) throws IOException {
		
		String value = "";
		String name = "";
		String line;
		
		while((line = r.readLine()) != null) {
			lineID++;
			
			if(line.trim().startsWith("//")) {
			} else if(line.contains("</def>")) {
				defs.put(name, value);
				return;
			} else if(line.contains("<name>")) {
				name = extractContents(line);
			} else if(line.contains("<value>")) {
				value = extractContents(line);
			} else {
				baseConditions(line);
			}
		}
	}

	
	public String findTagLine(BufferedReader r) throws IOException {
		String line;
		while((line = r.readLine()) != null) {
			line = line.trim();
			int index = line.indexOf("//");
			if(index >= 0) {
				line = line.substring(0, index);
			}
			int start = line.indexOf('<');
			int stop = line.indexOf('>',start);
			if(start >= 0 && stop >= 0) {
				return line.substring(start+1,stop);
			}
		}
		return null;
	}
	
	public String findTag(BufferedReader r) throws IOException {
		
		while(true) {
			int c = r.read();
			if(c == -1) {
				return null;
			} else if(c == '<') {
				//String.copyValueOf(data, offset, count)
				char[] buffer = new char[1000];
				for(int i=0;true;i++) {
					c = r.read();
					if(c == -1) {
						return null;
					}
					if(c == '>') {
						return String.copyValueOf(buffer, 0, i);
					}
					if(i >= buffer.length) {
						char[] buffer2 = new char[buffer.length*2];
						for(int j=0;j<buffer.length;j++) {
							buffer2[j] = buffer[j];
						}
						buffer = buffer2;
					}
					buffer[i] = (char)c;
				}
			}
		}
	}
	
	public String extractContents(String line) {
		int start = line.indexOf('>');
		int stop = line.indexOf('<', start);
		String string = line.substring(start+1, stop);
		return replaceVariables(string);
	}
	
	public String replaceVariables(String string) {
		int index = string.indexOf('$');
		while(index >= 0) {
			int j;
			for(j=index+1;j<string.length();j++) {
				if(	(string.charAt(j) >= 'a' && string.charAt(j) <= 'z') ||
					(string.charAt(j) >= 'A' && string.charAt(j) <= 'Z')) {
				} else {
					break;
				}
			}
			
			String variable = string.substring(index+1,j);
			String value = defs.get(variable);
			string = string.substring(0,index) + value + string.substring(j);
			index = string.indexOf('$', index);
		}
		return string;
	}
}
