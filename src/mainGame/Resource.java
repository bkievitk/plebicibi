package mainGame;

import java.io.Serializable;

import javax.swing.ImageIcon;

public class Resource implements Serializable {
	
	private static final long serialVersionUID = -4402014035945633306L;
	
	public int id = 0;
	public ImageIcon image = null;
	public String name = null;
	public boolean visible = false;
	
	public Resource(int id, ImageIcon image, String name, boolean visible) {
		this.id = id;
		this.image = image;
		this.name = name;
		this.visible = visible;
	}
}
