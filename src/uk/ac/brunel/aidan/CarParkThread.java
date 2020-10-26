package uk.ac.brunel.aidan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CarParkThread extends Thread {

	private Socket socket = null;
	private CarParkManager manager = null;
	private String state = "SETUP";
	private String substate = "READY";

	public CarParkThread(Socket socket, CarParkManager manager) {
		this.socket = socket;
		this.manager = manager;
	}

	public void run() {
		try {
			System.out.println("New thread "+this.getId());
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String inputLine, outputLine;

//			Initial setup to establish what I am...
			out.println("What gatekeeper are you?:\n1. Entrance\n2. Exit");
			while((inputLine = in.readLine())!=null) {

				if(state=="SETUP") {
					if(inputLine.equalsIgnoreCase("1")) state = "ENTRANCE";
					else if(inputLine.equalsIgnoreCase("2")) state = "EXIT";
					if(state!="SETUP") {
						this.setName(state+"#"+this.getId());
						System.out.println(this.getName()+" has connected.");
						out.println("You are now "+this.getName()+".");
						substate = "INTRO";
					} else {
						out.println("{!!} Response not accepted. Please try again.");
						continue;
					}
					//SETUP end
				}

				if(state=="ENTRANCE" && substate=="INTRO") {
					out.println("{?} You can get an updated list of spaces by entering '#list'");
					out.println("{?} Enter the car registration trying to park:");
					substate = "READY";
					continue;
				}

				if(state=="ENTRANCE" && substate=="READY") {
					if(inputLine.equalsIgnoreCase("#list")) substate = "LIST";
					else { //todo

					}
				}

				if(state=="EXIT" && substate=="INTRO") {
					out.println("{?} You can get an updated list of spaces by entering '#list'");
					substate = "LIST";
				}

				if(state=="EXIT" && substate=="READY") {
					if(inputLine.equalsIgnoreCase("#list")) substate = "LIST";
					else {
						int space = -1;
						try {
							space = Integer.parseInt(inputLine)-1;
						} catch(Exception e) {
							out.println("{!!} You didnt enter a number. Please try again.");
							continue;
						}
						manager.lock();
						String kicked = manager.kick(space);
						manager.unlock();
						if(!kicked.isEmpty()) {
							out.println("You have kicked car "+kicked+" out of space "+space+".");
						} else {
							out.println("{!} Unable to kick space "+space+". Perhaps the space is already empty?");
							substate = "LIST";
						}

					}
				}

				if(substate=="LIST") { //Can be accessed from any state.
					manager.lock();
					String[] spaces = manager.list();
					manager.unlock();
					out.println("Here is the latest view of the car park:");
					for(int i = 0; i<spaces.length; i++) {
						out.println("["+(i+1)+"] "+spaces[i]);
					}
					if(state=="EXIT")
						out.println("{?} To make the car leave a parking space, enter the space index (the number in brackets):");
					substate = "READY";
					continue;
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
