package mainGame.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import mainGame.GameAction;
import mainGame.run.Client;

public class CircleGUIMain extends GUIMain {

	private static final long serialVersionUID = -906313151583186263L;
	
	public JPanel actionsPanel;
	
	public Client client;
		
	public CircleGUIMain(final Client client) {

		this.client = client;
		
		setLayout(new BorderLayout());
		
		// The panel that displays the common field.
		JPanel boardPanel = new JPanel() {
			private static final long serialVersionUID = -6960195244841870888L;

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				// Background.
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, getWidth(), getHeight());				
				g.setColor(Color.BLACK);
				g.drawRect(0, 0, getWidth(), getHeight());
				
				// Board.
				if(client.board != null) {
					double scale = .4;
					Client.renderLand(client.board, g, new Rectangle((int)(getWidth() * (.5 - scale / 2)), (int)(getHeight() * (.5 - scale / 2)), (int)(getWidth() * scale), (int)(getHeight() * scale)), client.context);
				}
				
				// Players.
				for(int i =0; i<client.board.players.length;i++) {
					int playerID = (i + client.board.players.length - client.playerID) % client.board.players.length;
					int x = getWidth() / 2 + (int)(Math.cos(Math.PI * 2 * (i+1) / client.board.players.length + Math.PI / 2) * getWidth() * .35);
					int y = getHeight() / 2 + (int)(Math.sin(Math.PI * 2 * (i+1) / client.board.players.length + Math.PI / 2) * getHeight() * .35);
					
					Rectangle rect = new Rectangle(x - 60, y - 80, 120, 160);
					
					// Draw.
					g.setColor(Color.BLACK);
					g.drawRect(rect.x, rect.y, rect.width, rect.height);

					// Draw each resource.
					int count = 0;
					g.drawString("player: " + playerID, rect.x + 2, rect.y + 14 + 25 * count);
					for(Integer key : client.context.landTypes.keySet()) {
						if(client.context.landTypes.get(key).visible) {
							g.drawImage(client.context.landTypes.get(key).image.getImage(), rect.x + 2, rect.y + 14 + 25 * count, 25, 25, null);
							g.drawString(client.context.landTypes.get(key).name + ": " + client.board.players[playerID].getResourceCount(key), rect.x + 29, rect.y + 26 + 25 * count);
							count++;
						}
					}
				}
			}
		};
		add(boardPanel, BorderLayout.CENTER);

		// The panel that shows the available actions.
		actionsPanel = new JPanel(new GridLayout(0,5));
		actionsPanel.setBackground(Color.WHITE);
		JScrollPane scroll = new JScrollPane(actionsPanel);
		scroll.setPreferredSize(new Dimension(1,200));
		add(scroll, BorderLayout.SOUTH);
		
		rebuild();
	}
	
	public void rebuild() {
						
		// Remove all actions.
		actionsPanel.removeAll();
		
		final GUIMain myThis = this;
		// If there are actions defined.
		if(client.board != null && client.board.players[client.playerID].availableActions != null) {
			
			// For each action.
			int numActions = client.board.players[client.playerID].availableActions.size();
			for(int i=0;i<numActions;i++) {
				
				// Render action card.
				BufferedImage img = new BufferedImage(100,200,BufferedImage.TYPE_INT_RGB);				
				final GameAction action = client.board.players[client.playerID].availableActions.get(i);	
				if(action == null) {
					System.out.println("Unable to find action.");
				}
				action.render(img.getGraphics(), new Rectangle(0,0,img.getWidth()-1,img.getHeight()-1));
				
				// Create as button.
				JButton button = new JButton(new ImageIcon(img));
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {

						// When clicked, remove all elements.
						actionsPanel.removeAll();

						// Create a new button for this element.
						BufferedImage img = new BufferedImage(100,200,BufferedImage.TYPE_INT_RGB);			
						action.render(img.getGraphics(), new Rectangle(0,0,img.getWidth()-1,img.getHeight()-1));
						JButton button = new JButton(new ImageIcon(img));
						button.setEnabled(false);
						actionsPanel.add(button);
						actionsPanel.invalidate();
						actionsPanel.validate();
						actionsPanel.repaint();
						
						client.performAction(action, myThis);
					}
				});
				
				// Set color and size.
				button.setBackground(Color.WHITE);
				button.setPreferredSize(new Dimension(img.getWidth(),img.getHeight()));
				actionsPanel.add(button);
			}
		}

		invalidate();
		validate();
		repaint();
	}
}
