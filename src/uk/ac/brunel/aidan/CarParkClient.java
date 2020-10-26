package uk.ac.brunel.aidan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class CarParkClient {

	public static void main(String[] args) throws IOException {

		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String host = "localhost";
		int port = 4444;

		try {
			socket = new Socket(host, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch(UnknownHostException e) {
			System.err.println("Don't know about host: "+host);
			System.exit(-1);
		} catch(IOException e) {
			System.err.println("Couldn't get I/O for the connection to: "+port);
			System.exit(-1);
		}

		BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Initialised client and IO connections");

		new Printer("Server", in, null).start();
		new Printer("Me", userIn, out).start();

	}
}

class Printer extends Thread {
	private BufferedReader in = null;
	private PrintWriter out = null;

	public Printer(String name, BufferedReader in, PrintWriter out) {
		this.setName(name);
		this.in = in;
		this.out = out;
	}

	public void run() {
		String message;
		try {
			while((message = in.readLine())!=null) {
				System.out.println(this.getName()+": "+message);
				if(this.out!=null) out.println(message);
			}
		} catch(IOException e) {
			System.err.println(this.getName()+": Reading the input line failed.");
			e.printStackTrace();
		}

	}
}
