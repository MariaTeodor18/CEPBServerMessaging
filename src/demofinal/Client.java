package demofinal;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client extends Thread {

	private String clientName = null;
	private BufferedReader inputStream = null;
	private PrintStream outputStream = null;

	private Socket clientSocket = null;
	private List<Client> clientList;
	private BlockingQueue<PrivateMessage> msgQueue = new LinkedBlockingQueue<PrivateMessage>();

	public Client(Socket clientSocket, List<Client> clientList, BlockingQueue<PrivateMessage> msg) {
		this.clientSocket = clientSocket;
		this.clientList = clientList;
		this.msgQueue = msg;
	}

	public synchronized void sendMessage(PrivateMessage msg, List<Client> clientList) {
		System.out.println("putting message in queue in Client class..");
		putMessageInQueue(msg);
//		for (Client client : clientList) {
//			if (client != this && client.clientName != null && client.clientName.equals(receiver)) {
//				client.outputStream.println("From: " + this.clientName + "> " + message);
//				this.outputStream.println("To: " + receiver + "> " + message);
//				break;
//			}
//		}

	}

	private void putMessageInQueue(PrivateMessage msg) {
		try {
			msgQueue.put(msg);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}
	}

	public void run() {
		System.out.println("beginning of run in Client class..");
		openIOStreams();
		boolean identifiedClient = false;

		while (true) {
			identifiedClient = setClientName();
			if (identifiedClient)
				break;
		}
		synchronized (this) {
			for (Client client : clientList) {
				if (client != this) {
					client.outputStream.println("New user in town " + clientName); //check clientName not with @
				}
			}
		}
		/* Start the conversation. */
		while (true) {
			String line = getInputStreamLine();
			if (line != null && line.startsWith("/quit")) { //disconnect
				break;
			}
			if (line != null && line.startsWith("@")) { //message is private if it starts with "@"
				PrivateMessage msg = extractMessage(line);
				sendMessage(msg, clientList); //this should enque message somehow
			} else {
				/* The message is public, broadcast it to all other clients. */
				synchronized (this) {
					for (Client client : clientList) {
						if (client.clientName != null) {
							client.outputStream.println("<" + clientName + "> " + line);
						}
					}
				}
			}
		}
		synchronized (this) {
			for (Client client : clientList) {
				if (client != this && client.clientName != null) {
					client.outputStream.println("*** The user " + clientName + " is leaving the chat room !!! ***");

				}
			}
		}
		outputStream.println("*** Bye " + clientName + " ***");

		/*
		 * Clean up. Set the current thread variable to null so that a new client could
		 * be accepted by the server.
		 */
		synchronized (this) {
			for (Client client : clientList) {
				if (client == this) {
					clientList.remove(this);
				}
			}
		}
		/*
		 * Close the output stream, close the input stream, close the socket.
		 */
		closeIOStreams();

	}

	private PrivateMessage extractMessage(String line) {
		String[] msg = line.split("\\s", 2);
		String receiver = "";
		
		if (msg[0]!= null && !msg[0].isEmpty())
			receiver = msg[0];
		if (msg.length > 1 && msg[1] != null && !msg[1].isEmpty()) 
			msg[1] = msg[1].trim();
		
		return new PrivateMessage(receiver, MessageType.NOTIFICATION, msg[1]);
	}

	private  boolean setClientName() {
		outputStream.println("Name: ");
		
		String name = getInputStreamLine().trim();
		if (name != null && !name.isEmpty() && !name.contains("@")) {
			clientName = "@" + name;
			outputStream.println("Welcome " + name + "! " + "To leave enter /quit in a new line.");
			return true;
		} else {
			outputStream.println("The name should not contain '@' character and not be empty.");
			return false;
		}
	}

	private synchronized String getClientName() {
		return this.clientName;
	}

	private String getInputStreamLine() {
		String line = null;
		try {
			line = inputStream.readLine().trim();
		} catch (IOException e) {
			e.getMessage();
		}
		return line;
	}

	private void closeIOStreams() {
		try {
			inputStream.close();
			outputStream.close();
			clientSocket.close();
		} catch (IOException e) {
			e.getMessage();
		}
	}

	private void openIOStreams() {
		try {
			inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outputStream = new PrintStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
