package mainGame.gui;

import mainGame.Board;
import mainGame.GameContext;

public abstract class GUIRequests {
	public abstract String requestString(Board board, String message);
	public abstract String requestString(Board board, String message, String time, String defaultValue);
	public abstract String requestBoolean(Board board, String message);
	public abstract String requestBoolean(Board board, String message, String time, String defaultValue);
	public abstract String requestPlayer(Board board, String message);
	public abstract String requestPlayer(Board board, String message, String time, String defaultValue);
	public abstract String requestLandTile(Board board, String message, GameContext context);
	public abstract String requestLandTile(Board board, String message, String time, String defaultValue, GameContext context);
	public abstract String requestCard(Board board, String message, GameContext context);
	public abstract String requestCard(Board board, String message, String time, String defaultValue, GameContext context);
}
