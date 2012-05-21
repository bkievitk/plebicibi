package mainGame.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import mainGame.Board;
import mainGame.GameContext;
import mainGame.XMLParser;

public class ShowInterface {

	public static void main(String[] args) {
		
		try {
			// Extract all actions.
			XMLParser xml = new XMLParser();
			String location = "XML/Contexts/contextPantheon.xml";
			BufferedReader r = new BufferedReader(new FileReader(new File(location)));
			GameContext context = xml.parseGameContext(r, xml.actions,location);
			r.close();			
			Board board = new Board(context);
			new Client(null, board, context);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}
}
