package uk.ac.brunel.aidan;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class CarParkServer {

	public static void main(String[] args) throws IOException {
		InetAddress computerAddr = null;
		ServerSocket serverSocket = null;
		int port = 4444;
		try {
			computerAddr = InetAddress.getLocalHost();
			System.out.println("The address of this computer is... "+computerAddr.getHostName());
			serverSocket = new ServerSocket(port);
			System.out.println("Server started on port "+port);
		} catch(UnknownHostException e) {
			System.out.println(e);
			System.exit(-1);
		} catch(IOException e) {
			System.err.println("Could not listen on port: "+port);
			System.exit(-1);
		}


		CarParkManager sharedManager = new CarParkManager(2 /* Size of Car Park */);

		//Start some threads when a client tries to start a connection
		boolean listening = true;
		while(listening) {
			new CarParkThread(serverSocket.accept(), sharedManager).start();
		}

		serverSocket.close();
	}

}
