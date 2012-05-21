package mainGame;
import java.awt.event.*;
import java.util.concurrent.*;


public class SynchLock {

	private Semaphore semaphore1 = new Semaphore(1);
	private Semaphore semaphore2 = new Semaphore(0); 
	private int semaphoreCount = 0;
	private int count;
	private ActionListener al;
	
	public SynchLock(int count, ActionListener al) {
		this.count = count;
		this.al = al;
	}
	
	public void lock() {
		try {
			semaphore1.acquire();
			semaphoreCount++;
			
			if(semaphoreCount == count) {
				semaphoreCount = 0;
				if(al != null) {
					al.actionPerformed(new ActionEvent(this,0,""));
				}
				semaphore2.release(count);
			}
			semaphore1.release();
			semaphore2.acquire();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}
