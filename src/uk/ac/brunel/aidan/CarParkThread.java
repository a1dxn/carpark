package uk.ac.brunel.aidan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CarParkThread extends Thread {

	public PrintWriter out = null;
	private Socket socket = null;
	private CarParkManager manager = null;
	private String state = "INITIAL", previousState = null, nextState = null, role = null;

	public CarParkThread(Socket socket, CarParkManager manager) {
		this.socket = socket;
		this.manager = manager;
	}

	public PrintWriter getPrinter() {
		if(isAlive() && out!=null) return out;
		else throw new Error("Could not get thread's printer!");
	}

	private void setState(String newState, String nextState) {
		this.previousState = this.state;
		this.state = newState;
		this.nextState = nextState;
	}

	public void run() {
		try {
			System.out.println("New thread "+this.getId());
			out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String inputLine = null;

			while(this.state!="STOP") {

				if(this.state=="INITIAL") {
					out.println("What gatekeeper are you?:\n1. Entrance\n2. Exit");
					this.setState("WAITING", "SETUP");
				}

				if(this.state=="SETUP") {
					assert inputLine!=null;
					if(inputLine.equalsIgnoreCase("1")) this.role = "ENTRANCE";
					else if(inputLine.equalsIgnoreCase("2")) this.role = "EXIT";

					if(!this.role.isBlank()) {
						this.setName(this.role+"#"+this.getId());
						System.out.println(this.getName()+" has connected.");
						out.println("You are now "+this.getName()+".");
						this.setState("LIST", "INFO");
					} else {
						out.println("{!!} Response not accepted. Please try again.");
						this.setState("INITIAL", null);
					}
				}

				if(this.role=="ENTRANCE" && this.state=="INFO") {
					out.println("{?} You can get an updated list of spaces by entering '#list'");
					out.println("{?} Enter the car registration trying to park:");
					this.setState("WAITING", "ARRIVE");
				}

				if(this.role=="EXIT" && this.state=="INFO") {
					out.println("{?} You can get an updated list of spaces by entering '#list'");
					out.println("{?} To make the car leave a parking space, enter the space index (the number in brackets):");
					this.setState("WAITING", "DEPART");
				}

				if(this.state=="ARRIVE") {
					assert inputLine!=null;
					if(inputLine.equalsIgnoreCase("#list")) {
						this.setState("LIST", "INFO");
						continue;
					}
					manager.lock();
					manager.queue(inputLine, out);
					manager.unlock();
					this.setState("INFO", null);
				}

				if(this.state=="DEPART") {
					assert inputLine!=null;
					if(inputLine.equalsIgnoreCase("#list")) {
						this.setState("LIST", "INFO");
						continue;
					}
					int space = -1;
					try {
						space = Integer.parseInt(inputLine);
					} catch(Exception e) {
						out.println("{!!} You didnt enter a number. Please try again.");
						this.setState("LIST", "INFO");
						continue;
					}
					manager.lock();
					String kicked = manager.kick(space-1);
					manager.unlock();
					if(!kicked.isEmpty()) {
						out.println("You have kicked car "+kicked+" out of space "+space+".");
					} else {
						out.println("{!} Unable to kick space "+space+". Perhaps the space is already empty?");
					}
					this.setState("LIST", "INFO");
				}

				if(this.state=="LIST") {
					manager.lock();
					String[] spaces = manager.list();
					manager.unlock();
					out.println("Here is the latest view of the car park:");
					for(int i = 0; i<spaces.length; i++) {
						out.println("["+(i+1)+"] "+spaces[i]);
					}
					this.setState(this.nextState, null);
				}

				if(state=="WAITING") {
					inputLine = in.readLine();
//				    Will only continue once a line is read from stream...
					if(inputLine==null) state = "STOP";
					else this.setState(this.nextState, null);
				}

			}

			out.close();
			in.close();

			System.out.println(this.getName()+" has disconnected.");

		} catch(IOException|InterruptedException e) {
//			e.printStackTrace();
		}
	}

}
