import java.io.*;
import java.net.*;
import java.text.SimpleDataFormat;
import java.util.*;

public class Server
{
	private static int uniqueID;
	//to keep the list of the client
	private ArrayList<ClientThread> al;
	//GUI
	private ServerGUI sg;
	//time
	private SimpleDateFormat sdf;
	//port
	private int port;
	//to stop the server
	private boolean keepGoing;

	public Server(int port)
	{
		this(port, null);
	}

	public Server(int port, ServerGUI sg)
	{
		this.sg = sg;
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");
		al = new ArrayList<ClientThread>();
	}

	public void start()
	{
		keepGoing = true;

		try
		{
			ServerSocket serverSocket = new ServerSocket(port);

			while(keepGoing)
			{
				display("Server waiting for clients on port " + port + ".");
				Socket socket = serverSocket.accept();

				//if asked to stop
				if(!keepGoing)
					break;

				//make a thread
				ClientThread t = new ClientThread(socket);
				//saved in the list
				al.add(t);
				t.start();
			}
			try
			{
				serverSocket.close();

				for(int i = 0; i < al.size(); ++i)
				{
					ClientThread tc = al.get(i);

					try
					{
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					}

					catch(Exception e)
					{
						display("Exception closing the server and the clients: " + e);
					}
				}

				catch(IOException e)
				{
					String msg = sdf.format(new Date()) + "Exception on new ServerSocket: " + e + "\n";
					display(msg);
				}
			}	
		}
	}

	protected void stop()
    {
    	keepGoing = false;
    	try
    	{
    		new Socket("localhost", port);
    	}

    	catch(Exception e)
    	{
    		//nothing
    	}
    }

    private void display(String msg)
    {
    	String time  = sdf.format(new Date()) + " " + msg;

    	if(sg == null)
    		System.out.println(time);
    	else
    		sg.appendEvent(time + "\n");
    }

    private synchronized void broadcast(String message)
    {
    	String time = sdf.format(new Date());
    	String messageLf = time + " " + message + "\n";

    	//display
    	if(sg == null)
    		System.out.print(messageLf);
    	else
    		sg.appendRoom(messageLf);

    	//to remove a client, when it gets disconnected
    	for(int i = al.size(); --i >= 0; )
    	{
    		ClientThread ct = al.get(i);

    		//remove from list
    		if(!ct.writeMsg(messageLf))
    		{
    			al.remove(i);
    			display("Disconnected Client " + ct.username + " remove from list.");
    		}
    	}
    }

    //for those who logout using LOGOUT message
    synchronized void remove(int id)
    {
    	//scan array until we find the id
    	for(int i = 0; i < al.size(); ++i)
    	{
    		ClientThread ct = al.get(i);

    		//when found
    		if(ct.id == id)
    		{
    			al.remove(i);
    			return;
    		}
    	}
    }

    public static void main(String[] args)
    {
    	int portNumber = 1500;

    	switch(args.length)
    	{
    		case 1:
    			try
    			{
    				portNumber = Integer.parseInt(args[0]);
    			}

    			catch(Exception e)
    			{
    				System.out.println("Invalid port number.");
    				System.out.println("Usage is: > java Server [portNumber]");
    				return;
    			}

    		case 0: 
    			break;

    		default:
    			System.out.println("Usage is: > java Server [portNumber]");
    			return;
    	}

    	Server server = new Server(portNumber);
    	server.start();
    }

    class ClientThread extends Thread
    {
    	Socket socket;
    	ObjectInputStream sInput;
    	ObjectOutputStream sOutput;
    	int id;
    	String username;
    	ChatMessage cm;
    	String date;

    	ClientThread(Socket socket)
    	{
    		id = ++uniqueID;
    		this.socket = socket;

    		System.out.println("Thread trying to create Object Input/Output Streams");

    		try
    		{
    			sOutput = new ObjectOutputStream(socket.getOutputStream());
    			sInput = new ObjectInputStream(socket.getInputStream());
    			
    			//read username
    			username = (String) sInput.readObject();
    			display(username + " just connected.");
    		}

    		catch(IOException e)
    		{
    			display("Exception creating new Input/Output Streams: " + e);
    			return;
    		}

    		catch(ClassNotFoundException e)
    		{

    		}

    		date = new Date().toString() + "\n";
    	}

    	public void run()
    	{
    		boolean keepGoing = true;

    		while(keepGoing)
    		{
    			try
    			{
    				cm = (ChatMessage) sInput.readObject();
    			}

    			catch(IOException e)
    			{
    				display(username + " Exception reading Streams: " + e);
    				break;
    			}

    			catch(ClassNotFoundException e2)
    			{
    				break;
    			}

    			String message = cm.getMessage();

    			switch(cm.getType())
    			{
    				case ChatMessage.MESSAGE:
    					broadcast(username + ": " + message);
    					break;

    				case ChatMessage.LOGOUT:
    					display(username + " disconnected with a LOGOUT message.");
    					keepGoing = false;
    					break;

    				case ChatMessage.WHOISIN:
    					writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");

    					for(int i = 0; i < al.size(); ++i)
    					{
    						ClientThread ct = al.get(i);
    						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
    					}

    					break;
    			}
    		}

    		remove(id);
    		close();
    	}

    	private void close()
    	{
    		try
    		{
    			if(sOutput != null)
    				sOutput.close();
    		}

    		catch(Exception e) {}

    		try
    		{
    			if(sInput != null)
    				sInput.close();
    		}

    		catch(Exception e) {}

    		try
    		{
    			if(socket != null)
    				socket.close();
    		}

    		catch(Exception e) {}
    	}

    	private boolean writeMsg(String msg)
    	{
    		//if client connected, send message to client
    		if(!socket.isConnected())
    		{
    			close();
    			return false;
    		}

    		try
    		{
    			sOutput.writeObject(msg);
    		}

    		catch(IOException e)
    		{
    			display("Error sending message to " + username);
    			display(e.toString());
    		}

    		return true;
    	}
    } 
}
