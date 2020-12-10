package uk.ac.brunel.aidan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


		HashMap<String, Thread> set = new HashMap<>();
		new Printer("Me", userIn, out, set).start();
		new Printer("Server", in, null, set).start();


	}
}

class Printer extends Thread {
	private BufferedReader in = null;
	private PrintWriter out = null;
	private HashMap<String, Thread> set = null;

	public Printer(String name, BufferedReader in, PrintWriter out, HashMap<String, Thread> set) {
		this.setName(name);
		this.in = in;
		this.out = out;
		this.set = set;
	}

	public void run() {
		this.set.put(this.getName(), currentThread());
		String message;
		boolean flag = true;
		try {
			while((message = in.readLine())!=null) {
				System.out.println(this.getName()+": "+message);
				if(this.out!=null) out.println(message); //Must be the client... lets send out that message!
				else if(flag) { //Must be the server!
					//All of this just to make it say its name instead of "Me"...but i like the look of it :)
					Pattern pattern = Pattern.compile("(ENTRANCE|EXIT)#\\d+");
					Matcher matcher = pattern.matcher(message);
					if(matcher.find()) {
						this.set.get("Me").setName(matcher.group());
						flag = false;
					}
				}
			}
		} catch(IOException e) {
			System.err.println(this.getName()+": Reading the input line failed.");
			e.printStackTrace();
		}

	}
}
