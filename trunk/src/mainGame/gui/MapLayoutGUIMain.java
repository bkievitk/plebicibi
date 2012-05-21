package mainGame.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

import javax.imageio.*;
import javax.swing.*;

import mainGame.*;
import mainGame.run.Client;

public class MapLayoutGUIMain extends GUIMain {

	private static final long serialVersionUID = -6600389561799168298L;
	
	// Reference to data.
	private Client client;
	
	// Images.
	private BufferedImage background;
	private BufferedImage woodBackground;
	public BufferedImage button;
	public BufferedImage cardBackground;
	
	// Available actions.
	private JPanel actionsPanel;
		
	private Hashtable<Integer,JLabel> resourceLabels = new Hashtable<Integer,JLabel>();
	
	private GameAction selectedAction = null;
	
	private Vector<JButton> actionButtons = new Vector<JButton>();
		
	// Locations to fill.
	private Point.Double[] resourceLocations = setLocations();/*{	new Point.Double(0.5007012622720898,0.3523489932885906),
			new Point.Double(0.42075736325385693,0.43791946308724833),
			new Point.Double(0.4992987377279102,0.436241610738255),
			new Point.Double(0.5007012622720898,0.5251677852348994),
			new Point.Double(0.576437587657784,0.5335570469798657),
			new Point.Double(0.34642356241234223,0.6124161073825504),
			new Point.Double(0.423562412342216,0.6124161073825504),
			new Point.Double(0.49789621318373073,0.6157718120805369),
			new Point.Double(0.5792426367461431,0.6157718120805369),
			new Point.Double(0.4992987377279102,0.697986577181208),
			new Point.Double(0.5792426367461431,0.6963087248322147),
			new Point.Double(0.6549789621318373,0.7013422818791947),};*/

	
	private Point.Double[] setLocations() {
		Point.Double[] locations = new Point.Double[12];
		boolean[][] pattern = {	{false, false, true,  false, false},
								{false, true,  true,  false, false},
								{false, false, true,  true,  false},
								{true,  true,  true,  true,  false},
								{false, false, true,  true,  true}};
		int i = 0;
		double x = .4;
		double y = .35;
		double width = .2;
		double height = .5;
		
		for(int py=0;py<pattern.length;py++) {
			for(int px=0;px<pattern[py].length;px++) {
				if(pattern[py][px]) {
					locations[i] = new Point.Double(
							x + px * width / (pattern.length - 1), 
							y + py * height / (pattern[0].length - 1));
					i++;
				}
			}	
		}
		return locations;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(background,0,0,getWidth(),getHeight(),this);
	}
	
	public MapLayoutGUIMain(final Client client) {
		this.client = client;
		
		setLayout(new BorderLayout());
		
		// Load images.
		try {
			background = ImageIO.read(new File("images/background.png"));
			cardBackground = ImageIO.read(new File("images/others/cardBackground.png"));
			woodBackground = ImageIO.read(new File("images/others/woodBackground.png"));
			button = ImageIO.read(new File("images/others/button.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Visualize the world.
		final JPanel world = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = -8285167868337288572L;
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				//g.drawImage(background,0,0,getWidth(),getHeight(),this);

				// Draw all land tiles.
				int count = 0;
				for(int x=0;x<client.board.land.length;x++) {
					for(int y=0;y<client.board.land[x].length;y++) {
						Point.Double p = resourceLocations[count];
						int px = (int)(getWidth() * p.x);
						int py = (int)(getHeight() * p.y);

						g.setColor(new Color(255,255,255,100));
						g.fillRect(px-30, py-30, 45, 60);
						g.setColor(Color.BLACK);
						g.drawRect(px-30, py-30, 45, 60);
						
						Resource resource = client.context.landTypes.get(client.board.land[x][y]);
						if(resource != null && resource.image != null) {
							g.drawImage(resource.image.getImage(), px-30, py-30, 45, 60, this);
						}
						count++;
					}	
				}
			}
		};
		world.setOpaque(false);
		add(world,BorderLayout.CENTER);
		
		this.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				double x = arg0.getX() / (double)world.getWidth();
				double y = arg0.getY() / (double)world.getHeight();
				System.out.println("new Point.Double(" + x + "," + y + "),");
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
		});

		// Add actions.
		actionsPanel = new JPanel(new GridLayout(1,0));
		actionsPanel.setPreferredSize(new Dimension(0,150));
		actionsPanel.setOpaque(false);
		actionsPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 0, 30));
		world.add(actionsPanel,BorderLayout.NORTH);

		// Options in the bottom panel.
		JPanel bottomPanel = new JPanel(new GridLayout(1,0, 10, 10)) {
			private static final long serialVersionUID = 8612171836525865945L;
			public void paintComponent(Graphics g) {
				//g.drawImage(control, 0, 0, getWidth(), getHeight(), this);
			}
		};
		bottomPanel.setOpaque(false);
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(40,10,10,10));		
		bottomPanel.setBackground(Color.LIGHT_GRAY);
		bottomPanel.setPreferredSize(new Dimension(0,250));
		
			// Score.
			JPanel score = new JPanel() {
				private static final long serialVersionUID = 7610317081570943221L;
				public void paintComponent(Graphics g) {
					g.drawImage(woodBackground, 0, 0, getWidth(), getHeight(), null);
				}
			};
				score.setOpaque(false);
				score.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				JLabel scoreLabel = new JLabel("SCORE: 0");
				scoreLabel.setForeground(Color.WHITE);
				score.add(scoreLabel);
			bottomPanel.add(score);
			
			// Players.
			JPanel players = new JPanel(new GridLayout(2, 0, 5, 5)) {
				private static final long serialVersionUID = 7610317081570943221L;
				public void paintComponent(Graphics g) {
					g.drawImage(woodBackground, 0, 0, getWidth(), getHeight(), null);
				}
			};
				players.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				players.setOpaque(false);
				
				if(client.context.players.size() == 0) {
					players.add(new JLabel("No players found."));					
				} else {
					for(int playerID : client.context.players.keySet()) {
						PlayerContext playerContext = client.context.players.get(playerID);
						JPanel playerPanel = new JPanel(new BorderLayout());
						playerPanel.setOpaque(false);
						
						playerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
						JButton imageButton = new JButton(playerContext.image);
						imageButton.setOpaque(false);
						imageButton.setBackground(Color.WHITE);
						imageButton.setBorder(null);
						
						if(client.playerID != playerID) {
							imageButton.setEnabled(false);
						}
						
						playerPanel.add(imageButton, BorderLayout.CENTER);
						JLabel name = new JLabel(playerContext.name);
						name.setForeground(Color.WHITE);
						playerPanel.add(name,BorderLayout.SOUTH);
						players.add(playerPanel);
					}
				}
			bottomPanel.add(players);
			
			// Resources.
			JPanel resources = new JPanel() {
				private static final long serialVersionUID = 7610317081570943221L;
				public void paintComponent(Graphics g) {
					g.drawImage(woodBackground, 0, 0, getWidth(), getHeight(), null);
				}
			};
				resources.setOpaque(false);
				resources.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				
				JPanel bufferBorder = new JPanel(new GridLayout(0,1));
				bufferBorder.setOpaque(false);
				resources.add(bufferBorder);
				
				for(int landKey : client.context.landTypes.keySet()) {
				
					Resource r = client.context.landTypes.get(landKey);
					
					if(r.visible) {
						JPanel resourcePanel = new JPanel(new BorderLayout());
						resourcePanel.setOpaque(false);
						
						final BufferedImage image = new BufferedImage(30,30,BufferedImage.TYPE_INT_ARGB);
						image.getGraphics().drawImage(r.image.getImage(), 0, 0, image.getWidth(), image.getHeight(), this);
						
						JPanel imagePanel = new JPanel() {
							private static final long serialVersionUID = 1L;
							public void paintComponent(Graphics g) {
								super.paintComponent(g);
								g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
							}
						};
						imagePanel.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
						imagePanel.setOpaque(false);
						
						resourcePanel.add(imagePanel,BorderLayout.WEST);
						
						JLabel label = new JLabel("  " + client.board.players[client.playerID].getResourceCount(r.id) + " " + r.name);
						label.setForeground(Color.WHITE);
						label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
						resourceLabels.put(r.id, label);
						
						resourcePanel.add(label,BorderLayout.CENTER);
						bufferBorder.add(resourcePanel);
					}
				}
			bottomPanel.add(resources);
			
			// Chat.
			JPanel chat = new JPanel(new BorderLayout()) {
				private static final long serialVersionUID = 7610317081570943221L;
				public void paintComponent(Graphics g) {
					g.drawImage(woodBackground, 0, 0, getWidth(), getHeight(), null);
				}
			};
				chat.setOpaque(false);
				chat.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				
				if(client.chat == null) {
					JLabel chatLabel = new JLabel("Chat not available.");
					chatLabel.setForeground(Color.WHITE);
					chat.add(chatLabel);
				} else {
					chat.add(client.chat, BorderLayout.CENTER);
				}
			bottomPanel.add(chat);
			
			// Timer.
			JPanel timer = new JPanel(new FlowLayout()) {
				private static final long serialVersionUID = 7610317081570943221L;
				public void paintComponent(Graphics g) {
					g.drawImage(woodBackground, 0, 0, getWidth(), getHeight(), null);
				}
			};
				timer.setOpaque(false);
				timer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				
				final JButton confirm = prettyButton("Confirm Action");
				
				final MapLayoutGUIMain myThis = this;
				confirm.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if(selectedAction != null) {
							
							// While waiting for action, do not allow other stuff to happen.
							confirm.setEnabled(false);
							for(JButton button : actionButtons) { button.setEnabled(false); };
							
							client.performActionSynchronous(selectedAction, myThis);
							
							// Re allow selection.
							confirm.setEnabled(true);
						}
					}
				});
				timer.add(confirm);
				
			bottomPanel.add(timer);
			
			// Options.
			JPanel options = new JPanel() {
				private static final long serialVersionUID = 7610317081570943221L;
				public void paintComponent(Graphics g) {
					g.drawImage(woodBackground, 0, 0, getWidth(), getHeight(), null);
				}
			};
				options.setOpaque(false);
				options.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				JPanel innerPanel = new JPanel(new GridLayout(0,1,10,10));
					innerPanel.setOpaque(false);
					JButton menu = prettyButton("Menu");
					menu.setEnabled(false);
					innerPanel.add(menu);
					JButton instructions = prettyButton("Instructions");
					instructions.setEnabled(false);
					innerPanel.add(instructions);
				options.add(innerPanel);
			bottomPanel.add(options);
			
		add(bottomPanel, BorderLayout.SOUTH);
		
		rebuild();
	}
	
	public JButton prettyButton(String label) {
		JButton prettyButton = new JButton(label) {
			private static final long serialVersionUID = 7128044484443547037L;
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(button, 0, 0, getWidth(), getHeight(), this);
				g.setColor(Color.WHITE);
				FontMetrics fm = g.getFontMetrics();
				g.drawString(getText(), (getWidth() - fm.stringWidth(getText()))/2, (getHeight() + fm.getAscent()) / 2 - 3);
			}
		};
		prettyButton.setBackground(Color.WHITE);
		prettyButton.setOpaque(false);
		prettyButton.setBorder(BorderFactory.createEmptyBorder(5,20,5,20));
		return prettyButton;
		
	}
	
	public void rebuild() {
						
		// Remove all actions.
		actionsPanel.removeAll();
				
		for(Integer key : resourceLabels.keySet()) {
			Resource r = client.context.landTypes.get(key);
			resourceLabels.get(key).setText(client.board.players[client.playerID].getResourceCount(r.id) + " " + r.name);
		}
		
		// If there are actions defined.
		if(client.board != null && client.board.players[client.playerID].availableActions != null) {
						
			// For each action.
			int numActions = client.board.players[client.playerID].availableActions.size();
			for(int i=0;i<numActions;i++) {
				
				final GameAction action = client.board.players[client.playerID].availableActions.get(i);
				
				// Render action card.
				BufferedImage img = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB);				
				Graphics g = img.getGraphics();
				
				g.drawImage(cardBackground, 0, 0, img.getWidth()-2, img.getHeight()-2, null);
				
				//action.render(g, new Rectangle(10, 10, img.getWidth()-20, img.getHeight()-20));
				g.drawString("order: " + action.order, 12, 24);
				GameAction.renderStringWrap(g, action.description, new Point(12, 24 + 12), img.getWidth() - 20 - 4);
				
				
				BufferedImage img2 = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB);				
				Graphics g2 = img2.getGraphics();
				g2.drawImage(img, 0, 0, null);
				g2.setColor(new Color(255,0,0,50));
				g2.fillRoundRect(0, 0, img2.getWidth(), img2.getHeight(), 10, 10);
				
				// Create as button.
				final JButton button = new JButton(new ImageIcon(img));
				button.setSelectedIcon(new ImageIcon(img2));
				button.setBackground(Color.WHITE);
				button.setOpaque(false);
				button.setPreferredSize(new Dimension(img.getWidth(),img.getHeight()));
				button.setBorder(BorderFactory.createEmptyBorder());
				actionButtons.add(button);
				
				if(action.equals(selectedAction)) {
					button.setSelected(true);
				}
				
				if(!action.selectable) {
					button.setEnabled(false);
				}
				
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {

						for(int i=0;i<actionButtons.size();i++) {
							actionButtons.get(i).setSelected(false);
						}						
						button.setSelected(true);
						selectedAction = action;
					}
				});				
				actionsPanel.add(button);
			}
		}

		invalidate();
		validate();
		repaint();
	}

}
