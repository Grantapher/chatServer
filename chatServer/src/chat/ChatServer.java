package chat;

/**
 * 	@author Grant Toepfer
 *  @class CSE 223
 *  @assignment Programming Assignment 5
 *  @date 5/30/2014
 * 
 * 	@description 
 * 	Chat server runs in the background and grabs all incoming connections,
 * 	creating a GUI to chat with every client simultaneously.
 * 
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

public class ChatServer extends Thread {
	private static final int PORT = 9898;
	private static Chatlog log;
	private JFrame frame = new JFrame("Chat");
	private JTextField dataField = new JTextField(40);
	private JTextArea messageArea = new JTextArea(8, 30);
	private Inbound inThread;
	private static String myIdentity;
	private PrintWriter out;
	private String theirIdentity;
	
	public ChatServer(final Socket socket, final int clientNumber,
			final String identity) {
		
		// Layout GUI
		frame.add(dataField, BorderLayout.SOUTH);
		frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);
		frame.setMinimumSize(new Dimension(384, 288));
		frame.setLocationRelativeTo(null);
		
		//sets textArea to wrap line and not be editable
		messageArea.setEditable(false);
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		messageArea.setBackground(null);
		messageArea.setBorder(null);
		
		//sets ScrollPane to update to bottom when it recieves input.
		DefaultCaret caret = (DefaultCaret) messageArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		// Add Listeners
		dataField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//on enter, sends the contents of the textField
				String output = dataField.getText();
				dataField.setText("");
				out.println(">" + output);
				messageArea.append(output + "\n");
			}
		});
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				//closes socket on close of GUI
				try {
					socket.close();
				}
				catch(IOException e1) {}
				frame.setVisible(false);
			}
		});
		
		//sets up output stream, which is event driven
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		//prints identity for client to see
		out.println(myIdentity);
		out.println("Identify yourself!");
		inThread = new Inbound(socket, clientNumber, messageArea, frame);
		inThread.start();
	}
	
	private static void log(String in) {
		log.append(in);
	}
	
	private static void getIP() {
		URL myIP = null;
		try {
			myIP = new URL("http://www.trackip.net/ip");
		}
		catch(Exception e1) {}
		
		String ipAddress = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					myIP.openStream()));
			ipAddress = in.readLine();
			in.close();
		}
		catch(Exception e) {}
		try {
			if(InetAddress.getLocalHost().getHostAddress().trim()
					.equals("127.0.0.1")) {
				String[] array = {
						"\"" + InetAddress.getLocalHost().getHostAddress()
								+ "\"", "Don't Copy", "Exit" };
				switch(JOptionPane
						.showOptionDialog(
								null,
								"Running internally at:\n"
										+ InetAddress.getLocalHost()
												.getHostAddress().trim()
										+ "\nThis application will seem closed after this,"
										+ " it will run in the background until someone connects.\n"
										+ "\nWould you like to copy any of the following text?",
								"New Game", JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, array,
								"Host")) {
					case 0:
						setClipboard(InetAddress.getLocalHost()
								.getHostAddress());
					case 1:
						break;
					default:
						System.exit(0);
				}
			} else if(ipAddress != null) {
				String[] array = {
						"\"" + InetAddress.getLocalHost().getHostAddress()
								+ "\"", "\"" + ipAddress + "\"",
						"Don't Copy", "Exit" };
				switch(JOptionPane
						.showOptionDialog(
								null,
								"Running locally at:\n"
										+ InetAddress.getLocalHost()
												.getHostAddress().trim()
										+ "\nand on the internet at:\n"
										+ ipAddress
										+ "\nTo recieve connections from the internet"
										+ " IP, you must forward Port 9898 to redirect"
										+ " connections from the internet here.\n"
										+ "\nThis application will seem closed after this,"
										+ " it will run in the background until someone connects.\n"
										+ "\nWould you like to copy any of the following text?",
								"New Game", JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, array,
								"Host")) {
					case 0:
						setClipboard(InetAddress.getLocalHost()
								.getHostAddress());
						break;
					case 1:
						setClipboard(ipAddress);
						break;
					case 2:
						break;
					default:
						System.exit(0);
				}
			} else {
				String[] array = {
						"\"" + InetAddress.getLocalHost().getHostAddress()
								+ "\"", "Don't Copy", "Exit" };
				switch(JOptionPane
						.showOptionDialog(
								null,
								"Running locally at\n"
										+ InetAddress.getLocalHost()
												.getHostAddress()
										+ "\nThis application will seem closed after this,"
										+ " but it will run in the background until someone connects.\n"
										+ "\nWould you like to copy any of the following text?",
								"New Game", JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, array,
								"Host")) {
					case 0:
						setClipboard(InetAddress.getLocalHost()
								.getHostAddress());
						break;
					case 1:
						break;
					default:
						System.exit(0);
				}
			}
		}
		catch(UnknownHostException e) {
			JOptionPane.showMessageDialog(null,
					"Host could not be determined.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	private static void setClipboard(String copy) {
		StringSelection ss = new StringSelection(copy);
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		cb.setContents(ss, null);
	}
	
	//handles incoming packets
	private class Inbound extends Thread {
		private Socket socket;
		private JFrame frame;
		private JTextArea textArea;
		
		public Inbound(Socket socket, int clientNumber,
				JTextArea textArea, JFrame frame) {
			this.socket = socket;
			this.textArea = textArea;
			this.frame = frame;
		}
		
		public void run() {
			try {
				BufferedReader in = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				theirIdentity = in.readLine();
				log("Connected to \"" + theirIdentity + "\" at port \""
						+ socket.getPort() + "\".");
				//client will send their identity when the outbound connection is established
				out.println("You are connected to " + myIdentity + "!");
				textArea.append("You are connected to " + theirIdentity
						+ "!\n");
				frame.setTitle("Chat: " + theirIdentity);
				frame.setVisible(true);
				//reads the input forever and appends the textArea
				while(true) {
					String input = in.readLine();
					if(input == null) {
						break;
					}
					textArea.append(">" + input + "\n");
				}
			}
			catch(IOException e) {}
			finally {
				try {
					socket.close();
					textArea.append("Connection closed.\n");
					log("Closed connection with " + theirIdentity + ".");
				}
				catch(IOException e) {}
			}
			
		}
	}
	
	//Main initializes the ServerSocket and grabs incoming connections
	public static void main(String[] args) throws Exception {
		javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager
				.getSystemLookAndFeelClassName());
		//
		int clientNumber = 0;
		ServerSocket listener = null;
		try {
			listener = new ServerSocket(PORT);
		}
		catch(IOException e) {
			JOptionPane.showMessageDialog(null,
					"Server is already running.", "Connection Issue",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		if((myIdentity = JOptionPane.showInputDialog(null,
				"What is your name?", "Identity",
				JOptionPane.QUESTION_MESSAGE)) == null)
			System.exit(0);
		getIP();
		log = new Chatlog(myIdentity);
		log("The server is running.");
		try {
			while(true) {
				new ChatServer(listener.accept(), ++clientNumber,
						myIdentity).start();
				//grabs all incoming connections and 
				//relays them to new GUI and connection
			}
		}
		finally {
			listener.close();
		}
	}
}
