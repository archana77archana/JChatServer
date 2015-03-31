import java.net.*;
import java.io.*;
import java.util.*;

public class Client
{
	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;
	private Socket socket;
	private ClientGUI cg;
	//server = server address
	private String server, username;
	private int port;

	//without GUI
	Client(String server, int port, String username)
	{
		this(server, port, username, null);
	}

	//through GUI
	Client(String server, int port, String username, ClientGUI cg)
	{
		this.server = server;
		this.port = port;
		this.username = username;
		this.cg = cg;
	}

	//start
	public boolean start()
	{
		try
		{
			socket = new Socket(server, port);
		}

		catch(Exception ec)
		{
			display("Error connecting to server: " + ec);
			return false;
		}

		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);

		try
		{
			sInput = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}

		catch(IOException eIO)
		{
			display("Exception creating new Input/Output Streams: " + eIO);
			return false;
		}

		//created thread to listen from server
		new ListenFromServer().start();

		try
		{
			sOutput.writeObject(username);
		}

		catch(IOException eIO)
		{
			display("Exception doing login: " + eIO);
			disconnect();
			return false;
		}

		return true;
	}

	private void display(String msg)
	{
		if(cg == null)
			System.out.println(msg);
		else
			cg.append(msg + "\n");
	}

	void sendMessage(ChatMessage msg)
	{
		try
		{
			sOutput.writeObject(msg);
		}

		catch(IOException e)
		{
			display("Exception writing to server: " + e);
		}
	}

	private void disconnect()
	{
		try
		{
			if(sInput != null)
				sInput.close();
		}

		catch(Exception e) {}

		try
		{
			if(sOutput != null)
				sOutput.close();
		}

		catch(Exception e) {}

		try
		{
			if(socket != null)
				socket.close();
		}

		catch(Exception e) {}

		if(cg != null)
			cg.connectionFailed();
	}

	public static void main(String[] args)
	{
		int portNumber = 1500;
		String serverAddress = "localhost";
		String username = "Anonymous";

		switch(args.length)
		{
			case 3: 
				serverAddress = args[2];

			case 2: 
				try
				{
					portNumber = Integer.parseInt(args[1]);
				}

				catch(Exception e)
				{
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
					return;
				}

			case 1:
				username = args[0];

			case 0:
				break;

			default:
				System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
				return;
		}

		Client client = new Client(serverAddress, portNumber, username);

		if(!client.start())
			return;

		Scanner scan = new Scanner(System.in);

		while(true)
		{
			System.out.print("> ");
			String msg = scan.nextLine();

			//Logout when you see the message as LOGOUT
			if(msg.equalsIgnoreCase("LOGOUT"))
			{
				client.sendMessage(new, ChatMessage(ChatMessage.LOGOUT, ""));
				break;
			}

			else if(msg.equalsIgnoreCase("WHOISIN"))
			{
				client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
			}

			else
			{
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}

		client.disconnect();
	}

	class ListenFromServer extends thread
	{
		public void run()
		{
			while(true)
			{
				try
				{
					String msg = (String) sInput.readObject();

					if(cg == null)
					{
						System.out.println(msg);
						System.out.print("> ");
					}

					else
					{
						cg.append(msg);
					}
				}

				catch(IOException e)
				{
					display("Server has closed the connection :" + e);

					if(cg != null)
						cg.connectionFailed();

					break;
				}

				catch(ClassNotFoundException e2)
				{

				}
			}
		}
	} 
}
