package uk.ac.brunel.aidan;

public class CarParkManager {

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

	public synchronized void unlock() throws InterruptedException {
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
			return space+1;
		} else return -1; //AKA no spaces available!
	}

	public synchronized void queue(String carReg, Thread owner) {

	}

	public synchronized int getQueueSize() {
		return 0;
	}

	public synchronized String kick(int space) {
		if(space<0 || space>this.carPark.length) return "";
		if(this.carPark[space]==null) return "";
		String car = this.carPark[space];
		this.carPark[space] = null;
		return car;
	}

}
