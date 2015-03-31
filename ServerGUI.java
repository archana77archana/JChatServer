import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ServerGUI extends JFrame implements ActionListener, WindowListener
{
	private static final long serialVersionUID = 1L;
	private JButton stopStart;
	private JTextArea chat, event;
	private JTextField tPortNumber;
	private Server server;

	ServerGUI(int port)
	{
		super("Chat Server");
		server = null;

		JPanel north = new JPanel();
		north.add(new JLabel("Port number: "));
		tPortNumber = new JTextField(" " + port);
		north.add(tPortNumber);

		stopStart = new JButton("Start");
		stopStart.addActionListener(this);
		north.add(stopStart);
		add(north, BorderLayout.NORTH);

		//Chat room
		JPanel center = new JPanel(new GridLayout(2, 1));
		chat = new JTextArea(80, 80);
		chat.setEditable(false);
		appendRoom("Chat Room.\n");
		center.add(new JScrollPane(chat));
		event = new JTextArea(80, 80);
		event.setEditable("Events log.\n");
		center.add(new JScrollPane(event));
		add(center);

		addWindowListener(this);
		setSize(400, 600);
		setVisible(true);
	}

	void appendRoom(String str)
	{
		chat.append(str);
		chat.setCaretPosition(chat.getText().length() - 1);
	}
}
