package chat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

@SuppressWarnings("serial")
class Chatlog extends JFrame {
	private JTextArea logArea = new JTextArea();
	private JButton closeButton = new JButton("Exit");
	
	public Chatlog(String identity) {
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setTitle("Chatlog for " + identity);
		add(new JScrollPane(logArea), BorderLayout.CENTER);
		add(closeButton, BorderLayout.SOUTH);
		setMinimumSize(new Dimension(384, 288));
		setLocationRelativeTo(null);
		
		//sets textArea to wrap line and not be editable
		logArea.setEditable(false);
		logArea.setLineWrap(true);
		logArea.setWrapStyleWord(true);
		logArea.setBackground(null);
		logArea.setBorder(null);
		
		//sets ScrollPane to update to bottom when it recieves input.
		DefaultCaret caret = (DefaultCaret) logArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		// Add Listeners
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				attemptClose();
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				attemptClose();
			}
		});
		setVisible(true);
	}
	
	public void append(String in) {
		logArea.append(in + "\n");
	}
	
	private void attemptClose() {
		if(JOptionPane.showConfirmDialog(this,
				"Are you sure you want to quit?", "Exit",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
			System.exit(0);
	}
}
