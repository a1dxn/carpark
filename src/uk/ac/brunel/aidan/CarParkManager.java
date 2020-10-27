package uk.ac.brunel.aidan;

import java.io.PrintWriter;
import java.util.ArrayList;

public class CarParkManager {

	private final ArrayList<Car> q = new ArrayList<>();
	private final String[] carPark;
	private boolean locked = false;

	public CarParkManager(int max) {
		if(max<1) throw new Error("Car Park size should be more than 0.");
		this.carPark = new String[max];
	}

	public synchronized void lock() throws InterruptedException {
		Thread me = Thread.currentThread(); //Ref to current thread.
		while(locked) {
			System.out.println(me.getName()+" is waiting to speak to manager.");
			wait();
		}
		this.locked = true;
		System.out.println(me.getName()+" has locked the manager.");
	}

	public synchronized void unlock() {
		this.locked = false;
		notifyAll();
		Thread me = Thread.currentThread(); //Ref to current thread.
		System.out.println(me.getName()+" has unlocked the manager.");
	}

//--------------------

	public synchronized String[] list() {
		return this.carPark;
	}

	public synchronized int findFreeSpace() {
		for(int i = 0; i<this.carPark.length; i++) {
			if(this.carPark[i]==null) return i;
		}
		return -1; //No spaces!! We're full
	}

	public synchronized int park(String carReg) {
		int space = this.findFreeSpace();
		if(space>=0) {
			this.carPark[space] = carReg;
			return space;
		} else return -1; //AKA no spaces available!
	}

	public synchronized int queue(String carReg, PrintWriter callerPrinter) {
//		Try to park initially...
		int spaceAllocated = this.park(carReg);
		if(spaceAllocated>=0) {
			callerPrinter.println("Car "+carReg+" has been parked in space "+(spaceAllocated+1));
			return spaceAllocated;
		}
//		If theres no space, we can start queuing cars
		this.q.add(new Car(carReg, Thread.currentThread(), callerPrinter));
		if(Thread.currentThread().isAlive()) callerPrinter.println("Car "+carReg+" is being held in a queue as "+
				"there are no spaces available.");
		return -1;
	}

	public synchronized void checkQueue() {
		if(this.q.size()==0) return;
		Car firstCar = this.q.get(0);
		if(firstCar.getOwnerThread().isAlive()) {
			int result = this.park(firstCar.getCarReg());
			if(result>=0) {
				firstCar.getPrinter().println("{*} A space has become available for "
						+firstCar.getCarReg()+" in space "+(result+1)+"! Now that's some nice parallel parking!");
				this.q.remove(0);
				this.checkQueue();
			}
		} else {
			this.q.remove(0); //Thread is dead, can remove the car.
			this.checkQueue(); //Try the next one in the queue instead!
		}
	}

	public synchronized String kick(int space) {
		if(space<0 || space>this.carPark.length) return "";
		if(this.carPark[space]==null) return "";
		String car = this.carPark[space];
		this.carPark[space] = null;

		this.checkQueue();
		return car;
	}

}

class Car {
	private final String carReg;
	private final Thread ownerThread;
	private final PrintWriter printer;

	public Car(String carReg, Thread ownerThread, PrintWriter printer) {
		this.carReg = carReg;
		this.ownerThread = ownerThread;
		this.printer = printer;
	}

	public PrintWriter getPrinter() {
		return printer;
	}

	public String getCarReg() {
		return carReg;
	}

	public Thread getOwnerThread() {
		return ownerThread;
	}

}
