package chat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

public class ChatServer extends Thread {
	private JFrame frame = new JFrame("Chat");
	private JTextField dataField = new JTextField(40);
	private JTextArea messageArea = new JTextArea(8, 30);
	private Inbound inThread;
	private static String myIdentity;
	private static PrintWriter out;
	private static String theirIdentity;
	
	public ChatServer(final Socket socket, final int clientNumber,
			final String identity) {
		myIdentity = identity;
		// Layout GUI
		messageArea.setEditable(false);
		frame.add(dataField, BorderLayout.SOUTH);
		frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);
		frame.setMinimumSize(new Dimension(384, 288));
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		DefaultCaret caret = (DefaultCaret) messageArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		// Add Listeners
		dataField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String output = dataField.getText();
				dataField.setText("");
				out.println(">" + output);
				messageArea.append(output + "\n");
			}
		});
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					socket.close();
				}
				catch(IOException e1) {
					log("Couldn't close a socket, what's going on?");
				}
				frame.setVisible(false);
			}
		});
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		out.println(myIdentity);
		out.println("Identify yourself!");
		inThread = new Inbound(socket, clientNumber, messageArea, frame);
		inThread.start();
	}
	
	protected void log(String string) {
		System.out.println(string);
	}
	
	private static class Inbound extends Thread {
		private Socket socket;
		private int clientNumber;
		private JFrame frame;
		private JTextArea textArea;
		
		public Inbound(Socket socket, int clientNumber,
				JTextArea textArea, JFrame frame) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			this.textArea = textArea;
			this.frame = frame;
			log("New connection at " + socket);
			log("Connected to client #" + clientNumber + ".");
		}
		
		public void run() {
			try {
				BufferedReader in = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				theirIdentity = in.readLine();
				out.println("You are connected to " + myIdentity + ".");
				textArea.append("You are connected to " + theirIdentity
						+ "!\n");
				frame.setTitle("Chat: " + theirIdentity);
				frame.setVisible(true);
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
				}
				catch(IOException e) {
					log("Couldn't close a socket, what's going on?");
				}
				log("Connection with client #" + clientNumber + " closed.");
				textArea.append("Connection closed.\n");
			}
		}
		
		private void log(String message) {
			System.out.println(message);
		}
	}
	
	public static void main(String[] args) throws Exception {
		javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager
				.getSystemLookAndFeelClassName());
		int clientNumber = 0;
		ServerSocket listener = null;
		try {
			listener = new ServerSocket(9898);
		}
		catch(IOException e) {
			JOptionPane.showMessageDialog(null, "Already Running.",
					"Connection Issue", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		String identity;
		if((identity = JOptionPane.showInputDialog(null,
				"What is your name?", "Identity",
				JOptionPane.QUESTION_MESSAGE)) == null)
			System.exit(0);
		System.out.println("The server is running.");
		try {
			while(true) {
				new ChatServer(listener.accept(), clientNumber++, identity)
						.start();
			}
		}
		finally {
			listener.close();
		}
	}
}
