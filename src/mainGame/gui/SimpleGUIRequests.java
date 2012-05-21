package mainGame.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import javax.swing.*;

import mainGame.*;
import mainGame.run.*;

public class SimpleGUIRequests extends GUIRequests {
	
	public static void buildTimedFrame(JPanel content, long timeMS, int width, int height) {

		JFrame frame = new JFrame();
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);
		
		long steps = timeMS / 100;
		JLabel countdown = new JLabel((steps / 10.0) + " seconds left.");
		countdown.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		panel.add(countdown,BorderLayout.NORTH);
		
		panel.add(content,BorderLayout.CENTER);
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setSize(width, height);
		frame.add(new JScrollPane(panel));
		frame.setVisible(true);
				
		try {
			for(int i=0;i<steps;i++) {
				countdown.setText(((steps - i) / 10.0) + " seconds left.");
				Thread.sleep(50);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		frame.dispose();	
	}

	/**
	 * Show string input dialog.
	 * @param message
	 * @return
	 */
	public String requestString(Board board, String message) {
		return (String)JOptionPane.showInputDialog(
                null,
                message,
                "Player Request",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");
	}

	/**
	 * Show timed string input dialog.
	 * @param message
	 * @param time
	 * @param defaultValue
	 * @return
	 */
	public String requestString(Board board, String message, String time, String defaultValue) {		
		
		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(Color.WHITE);
		
		JTextArea text = new JTextArea(message);
		text.setEditable(false);
		text.setOpaque(false);
		content.add(text,BorderLayout.CENTER);
		text.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		JTextArea options = new JTextArea(defaultValue);
		content.add(options,BorderLayout.SOUTH);
		options.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		buildTimedFrame(content, Long.parseLong(time), 200, 200);
		
		return options.getText();
	}
	
	/**
	 * Show boolean input dialog.
	 * @param message
	 * @return
	 */
	public String requestBoolean(Board board, String message) {		
		Object[] options = {"Yes","No"};		
		int n = JOptionPane.showOptionDialog(
			null,
		    message,
            "Player Request",
		    JOptionPane.YES_NO_OPTION,
		    JOptionPane.QUESTION_MESSAGE,
		    null,
		    options,
		    options[1]);
		if(n == 0) {
			return "true";
		} else {
			return "false";
		}
	}
	
	/**
	 * Show timed input boolean dialog.
	 * @param message
	 * @param time
	 * @param defaultValue
	 * @return
	 */
	public String requestBoolean(Board board, String message, String time, String defaultValue) {		
		
		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(Color.WHITE);
		
		JTextArea text = new JTextArea(message);
		text.setEditable(false);
		text.setOpaque(false);
		content.add(text,BorderLayout.CENTER);
		text.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		JPanel options = new JPanel(new GridLayout(1,2));
		content.add(options,BorderLayout.SOUTH);
		options.setOpaque(false);
		options.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		JRadioButton yesButton = new JRadioButton("yes");
		JRadioButton noButton = new JRadioButton("no");
		options.add(yesButton);
		options.add(noButton);
		yesButton.setOpaque(false);
		noButton.setOpaque(false);
		
		if(defaultValue.equals("true")) {
			yesButton.setSelected(true);
		} else {
			noButton.setSelected(true);
		}
		
		ButtonGroup group = new ButtonGroup();
		group.add(yesButton);
		group.add(noButton);
		
		buildTimedFrame(content, Long.parseLong(time), 200, 200);
		
		if(yesButton.isSelected()) {
			return "true";
		} else {
			return "false";
		}
	}
	
	/**
	 * Show window to allow player to select another player.
	 * @param message
	 * @return
	 */
	public String requestPlayer(Board board, String message) {		
		
		Player[] options = new Player[board.players.length];	
		
		for(int i=0;i<options.length;i++) {
			options[i] = board.players[i];
		}
		
		Player out = (Player)JOptionPane.showInputDialog(
			null,
		    message,
            "Player Request",
		    JOptionPane.PLAIN_MESSAGE,
		    new ImageIcon(),
		    options,
		    options[0]);
		
		return out.playerID + "";
	}
	
	/**
	 * Show window to allow player to select another player that is timed.
	 * @param message
	 * @param time
	 * @param defaultValue
	 * @return
	 */
	public String requestPlayer(Board board, String message, String time, String defaultValue) {		
		
		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(Color.WHITE);
		
		JTextArea text = new JTextArea(message);
		text.setEditable(false);
		text.setOpaque(false);
		content.add(text,BorderLayout.CENTER);
		text.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		JComboBox options = new JComboBox();
		for(int i=0;i<board.players.length;i++) {
			options.addItem(board.players[i]);
		}
		options.setSelectedItem(board.players[Integer.parseInt(defaultValue)]);
		content.add(options,BorderLayout.SOUTH);
		
		options.setOpaque(false);
		options.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		buildTimedFrame(content, Long.parseLong(time), 200, 200);
		
		return ((Player)options.getSelectedItem()).playerID + "";
	}
	
	/**
	 * Show window to allow player to select land tile.
	 * @param message
	 * @return
	 */
	public String requestLandTile(final Board board, String message, final GameContext context) {	
		
		// Request action from user.
		final Semaphore s = new Semaphore(0);
		final JFrame frame = new JFrame();
		final Point point = new Point();
		
		final JPanel panel = new JPanel() {
			private static final long serialVersionUID = -675270577145321130L;

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				if(board != null) {
					Client.renderLand(board, g, new Rectangle(0,0,getWidth(),getHeight()), context);
				}
			}
		};
		panel.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent arg0) {
				point.x = arg0.getX() * panel.getWidth() / board.land.length;
				point.y = arg0.getY() * panel.getHeight() / board.land[0].length;
				s.release();
			}

			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
		});

		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setSize(200, 200);
		frame.add(new JScrollPane(panel));
		frame.setVisible(true);
		
		try {
			s.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		frame.dispose();		
		
		return point.x + " " + point.y;
	}
	
	/**
	 * Show window to allow player to select land tile that is timed.
	 * @param message
	 * @param time
	 * @param defaultValue
	 * @return
	 */
	public String requestLandTile(final Board board, String message, String time, String defaultValue, final GameContext context) {		
		
		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(Color.WHITE);

		String[] parts = defaultValue.split(" ");

		final Point point = new Point(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));
		
		JTextArea text = new JTextArea(message);
		text.setEditable(false);
		text.setOpaque(false);
		content.add(text,BorderLayout.SOUTH);
		text.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		final JPanel options = new JPanel() {
			
			private static final long serialVersionUID = 8144951545495574079L;

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				if(board != null) {
					Client.renderLand(board, g, new Rectangle(0,0,getWidth(),getHeight()),point,context);
				}
			}
		};
		options.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent arg0) {
				point.x = arg0.getX() * board.land.length / options.getWidth();
				point.y = arg0.getY() * board.land[0].length / options.getHeight();
				options.repaint();
			}

			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
		});
		content.add(options,BorderLayout.CENTER);
		
		options.setOpaque(false);
		options.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		buildTimedFrame(content, Long.parseLong(time), 200, 200);

		return point.x + " " + point.y;
	}
	
	/**
	 * Show window to allow player to select action card.
	 * @param message
	 * @return
	 */
	public String requestCard(Board board, String message, GameContext context) {	
		return null;
	}

	/**
	 * Show window to allow player to select action card that is timed.
	 * @param message
	 * @param time
	 * @param defaultValue
	 * @return
	 */
	public String requestCard(Board board, String message, String time, String defaultValue, GameContext context) {		
		
		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(Color.WHITE);
		
		JTextArea text = new JTextArea(message);
		text.setEditable(false);
		text.setOpaque(false);
		content.add(text,BorderLayout.SOUTH);
		text.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		JPanel options = new JPanel(new GridLayout(0,5));
		JScrollPane scroll = new JScrollPane(options);
		scroll.setPreferredSize(new Dimension(10,150));
		content.add(scroll,BorderLayout.CENTER);
		
		final GameAction[] action = new GameAction[1];
		
		if(context.allActions != null) {
			int numActions = context.allActions.size();
			final Vector<JButton> buttons = new Vector<JButton>();
			for(int i=0;i<numActions;i++) {
				BufferedImage img = new BufferedImage(100,200,BufferedImage.TYPE_INT_RGB);
				
				final GameAction thisAction = context.allActions.get(i);
				
				thisAction.render(img.getGraphics(), new Rectangle(0,0,img.getWidth()-1,img.getHeight()-1));
				final JButton button = new JButton(new ImageIcon(img));
				buttons.add(button);
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						action[0] = thisAction;
						for(JButton buttonA : buttons) {
							buttonA.setEnabled(true);
						}
						button.setEnabled(false);
					}
				});
				button.setBackground(Color.WHITE);
				button.setPreferredSize(new Dimension(img.getWidth(),img.getHeight()));
				options.add(button);
			}
		}

		buildTimedFrame(content, Long.parseLong(time), 500,250);
		
		return action[0].name;
	}
	
}
