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

public class SimpleGUIMain extends GUIMain {

	private static final long serialVersionUID = -906313151583186263L;
	
	public JPanel boardPanel;
	public JPanel resourcesPanel;
	public JPanel actionsPanel;
	
	public Client client;
		
	public SimpleGUIMain(final Client client) {

		this.client = client;
		
		setLayout(new BorderLayout());
		
		// The panel that displays the common field.
		boardPanel = new JPanel() {
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
					Client.renderLand(client.board, g, new Rectangle(10,10,getWidth()-20,getHeight()-20), client.context);
				}
			}
		};
		add(boardPanel, BorderLayout.CENTER);

		// The panel that shows the resources collected.
		resourcesPanel = new JPanel() {
			private static final long serialVersionUID = 4938194707421815516L;

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				// Background.
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.BLACK);
				g.drawRect(0, 0, getWidth(), getHeight());
				
				// Resources.
				for(int i=0;i<client.board.players.length;i++) {
					
					g.setColor(Color.BLACK);

					// Define square for player.
					int x = i * 120;
					int y = 0;
					int width = 120;
					int height = getHeight();

					// Draw.
					g.setColor(Color.BLACK);
					g.drawRect(x, y, width, height);

					// Draw each resource.
					int count = 0;
					for(Integer key : client.context.landTypes.keySet()) {
						if(client.context.landTypes.get(key).visible) {
							g.drawImage(client.context.landTypes.get(key).image.getImage(), x + 2, y + 2 + 25 * count, 25, 25, null);
							g.drawString(client.context.landTypes.get(key).name + ": " + client.board.players[i].getResourceCount(key), x + 29, y + 14 + 25 * count);
							count++;
						}
					}	
				}
			}
		};
		resourcesPanel.setPreferredSize(new Dimension(1,150));
		add(new JScrollPane(resourcesPanel), BorderLayout.NORTH);

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