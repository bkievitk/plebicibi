package mainGame;

import java.io.Serializable;

import javax.swing.ImageIcon;

public class PlayerContext implements Serializable {
	private static final long serialVersionUID = 4341788727110696475L;
	public int id;
	public String name;
	public String description;
	public ImageIcon image;
}
