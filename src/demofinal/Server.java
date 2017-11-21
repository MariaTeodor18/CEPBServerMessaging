package demofinal;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {

	private static ServerSocket serverSocket = null;
	private static Socket clientSocket = null;
	private static final int maxClients = 10;
	private static List<Client> clientList = Collections.synchronizedList(new ArrayList<Client>(maxClients));
	private static BlockingQueue<PrivateMessage> msgQueue = new LinkedBlockingQueue<PrivateMessage>();

	public static void main(String args[]) {

		int portNumber = 2222;

		createServerSocket(portNumber);

		while (true) {
			acceptConnection(); // listens for connection requests
			ClientConnection.handleClient(clientSocket);
//			Client client = new Client(clientSocket, clientList, msgQueue);
//			client.start();
//
//			clientList.add(client);
//			System.out.println("Now size is " + clientList.size());
//
//			if (clientList.size() == maxClients + 1) {
//				refuseConnection();
//			}
		}
	}

	private synchronized static void acceptConnection() {
		try {
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void refuseConnection() {
		try {
			PrintStream os = new PrintStream(clientSocket.getOutputStream());
			os.println("Maximum connection limit reached.");
			os.close();
			clientSocket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void createServerSocket(int portNumber) {
		try {
			serverSocket = new ServerSocket(portNumber);
			System.out.println("Server started..");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
