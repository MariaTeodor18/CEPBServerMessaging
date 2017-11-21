package demofinal;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientConnection implements Runnable { // ClientHandler

	private static Socket clientSocket = null;
	private static PrintStream outputStream = null;
	private static BufferedReader inputStream = null;
	private static BufferedReader inputLine = null;
	private static boolean closed = false;
	private static List<Client> clientList = Collections.synchronizedList(new ArrayList<Client>(10));
	private static BlockingQueue<PrivateMessage> msgQueue = new LinkedBlockingQueue<PrivateMessage>();
	
	public static void main(String[] args) {

		int portNumber = 2222;
		String host = "localhost";

		createClientSocket(portNumber, host);
		openIOStreams();

		if (clientSocket != null && outputStream != null && inputStream != null) {
			/* Create a thread to read from the server. */
			new Thread(new ClientConnection()).start();
			while (!closed) {
				String inputLine = getInputLine();
				outputStream.println(inputLine);
			}
		}
		closeIOStreams();
	}
	//move this class functionality into server
	public void run() {
		
		String responseLine;
		PrivateMessage msg = null;
//		while((msg = takeMsgFromQueue()) != null) {
//			System.out.println(msg);
//		}
		while (true) {
			responseLine = getInputStreamLine();
			System.out.println(responseLine);
			if (responseLine.indexOf("*** Bye") != -1)
				break;
			System.out.println(msgQueue.size()); // si aici se blocheaza cand vrei sa iei ceva din queue
												 // in while-ul asta banuiesc ca e problema 
//			if((msg = takeMsgFromQueue()) != null) {  
//				System.out.println(msg.getMessage());
//			}
		}
		closed = true;
	}
	private PrivateMessage takeMsgFromQueue() {
		PrivateMessage msg = null;
		try {
			 msg = msgQueue.take();
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}
		return msg;
	}
	
	public synchronized static void handleClient(Socket clientSocket) {
		Client client = new Client(clientSocket, clientList, msgQueue);
		client.start();

		clientList.add(client);
		System.out.println("Now size is " + clientList.size());

		if (clientList.size() == 10 + 1) {
			refuseConnection(clientSocket);
		}
	}
	
	private static void refuseConnection(Socket clientSocket) {
		try {
			PrintStream os = new PrintStream(clientSocket.getOutputStream());
			os.println("Maximum connection limit reached.");
			os.close();
			clientSocket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static void createClientSocket(int portNumber, String host) {
		try {
			clientSocket = new Socket(host, portNumber);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

	private static String getInputLine() {
		String line = "";
		try {
			line = inputLine.readLine().trim();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return line;
	}

	private String getInputStreamLine() {
		String line = "";
		try {
			line = inputStream.readLine();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return line;
	}

	private static void closeIOStreams() {
		try {
			outputStream.close();
			inputStream.close();
			clientSocket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void openIOStreams() {
		try {
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			outputStream = new PrintStream(clientSocket.getOutputStream());
			inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
