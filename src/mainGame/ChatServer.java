package mainGame;
import java.io.*;
import java.net.*;
import java.util.Vector;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The chat server, listens to messages and forwards them to all listeners.
 * @author iami
 *
 */

public class ChatServer {

	/**
	 * Start the server.
	 * @return
	 */
	public static ServerSocket buildChatServer(int port) {	
		try {
			
			// Start ServerSocket.
			final ServerSocket serverSocket = new ServerSocket(port);
			
			// Put the chat server in its own thread.
			new Thread() {
				public void run() {
					
					// List of all listeners to send to.
					final Vector<BufferedWriter> writers = new Vector<BufferedWriter>();
					try {
						
						// Infinitely listen for new people.
						while(true) {
							
							// When you have a new socket.
							final Socket socket = serverSocket.accept();
							
							// Place them in their own sending thread.
							new Thread() {
								public void run() {
									try {
										
										// Listen to this particular client.
										BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
										
										// Add to the writer list.
										writers.add(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));

										// Continuously read and write results to everyone.
										while(true) {
											
											// Read and write results.
											String str = reader.readLine();
											for(BufferedWriter writer : writers) {
												writer.write(str + "\n");
												writer.flush();
											}
										}
									} catch(IOException e) {
										e.printStackTrace();
									}
								}
							}.start();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}	
				}
			}.start();
			
			return serverSocket;
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static BufferedWriter buildHiddenChatClient(final String host, final int port) {
		try {
			Socket socket = new Socket(host, port);
			return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	/**
	 * Start the client with a name.
	 * @param name
	 */
	public static JPanel getChatClient(final String name, final String host, final int port) {
		
		final JPanel panel = new JPanel(new BorderLayout());
		
		// Client in its own thread.
		new Thread() {
			public void run() {
				try {
					
					// Connect to server.
					Socket socket = new Socket(host, port);
					final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					
					// Build GUI.
					final JTextArea messages = new JTextArea();
					final JTextField newMessage = new JTextField();
					messages.setEditable(false);
					panel.add(new JScrollPane(messages),BorderLayout.CENTER);
					panel.add(newMessage,BorderLayout.NORTH);
					
					// When new message recieved, add to visualization.
					new Thread() {
						public void run() {
							while(true) {
								try {
									messages.setText(reader.readLine() + "\n" + messages.getText());
									//messages.append(reader.readLine() + "\n");
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}.start();
					
					// When you hit enter, send your message.
					newMessage.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							try {
								writer.write(name + ") " + newMessage.getText() + "\n");
								writer.flush();						
								newMessage.setText("");
							} catch(IOException e) {
								e.printStackTrace();
							}
						}
					});		
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
		
		return panel;
	}
	
	
	/**
	 * Start the client with a name.
	 * @param name
	 */
	public static void buildChatClient(final String name, final String host, final int port) {
		
		// Client in its own thread.
		new Thread() {
			public void run() {
				try {
					
					// Connect to server.
					Socket socket = new Socket(host, port);
					final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					
					// Build GUI.
					JPanel panel = new JPanel(new BorderLayout());
					final JTextArea messages = new JTextArea();
					final JTextField newMessage = new JTextField();
					messages.setEditable(false);
					panel.add(new JScrollPane(messages),BorderLayout.CENTER);
					panel.add(newMessage,BorderLayout.NORTH);
					
					// When new message recieved, add to visualization.
					new Thread() {
						public void run() {
							while(true) {
								try {
									messages.append(reader.readLine() + "\n");
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}.start();
					
					// When you hit enter, send your message.
					newMessage.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							try {
								writer.write(name + ") " + newMessage.getText() + "\n");
								writer.flush();						
								newMessage.setText("");
							} catch(IOException e) {
								e.printStackTrace();
							}
						}
					});
					
					JFrame frame = new JFrame(name);
					frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					frame.setSize(200,300);
					frame.add(panel);
					frame.setVisible(true);			
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
}
