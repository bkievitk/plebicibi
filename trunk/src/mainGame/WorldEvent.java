package mainGame;

import java.io.Serializable;

import mainGame.run.Server;

public class WorldEvent implements Serializable {

	private static final long serialVersionUID = -5829277251504569079L;
	
	public String description;
	public String name;
	public String program;
	
	public String performWorldEvent(Server server) {
		return (new GameScript(program, -1, server.board, server.context,null, null,server.hiddenChatClient)).result;
	}
}
