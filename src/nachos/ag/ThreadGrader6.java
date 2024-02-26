package nachos.ag;

import java.util.HashSet;
import java.util.Set;
import nachos.machine.Machine;
import nachos.threads.KThread;
import nachos.threads.Lock;
import nachos.threads.PriorityScheduler;
import nachos.threads.ThreadedKernel;

/**
 * <li>ThreadGrader6: <b>More Priority Scheduling</b><br>
 * <ol type=a>
 * <li>Test ThreadGrader6.a: Tests priority donation
 * <li>Test ThreadGrader6.b: Tests priority donation with more locks and more
 * complicated resource allocation
 * </ol>
 * </li>
 * 
 * @author 
 * 
 */
public class ThreadGrader6 extends BasicTestGrader
{
  static int total = 0;
  static int count = 0;
  static int error_cnt = 0;
  static boolean high_run = false;
  Set<ThreadHandler> set = new HashSet<ThreadHandler>();
  Lock[] lock = null;
  static final int lockCount = 10;
  
  public void run ()
  {
    assertTrue(ThreadedKernel.scheduler instanceof PriorityScheduler,
      "this test requires priority scheduler");
    
    lock = new Lock[lockCount];
    for (int i = 0; i < lockCount; ++i)
      lock[i] = new Lock();
    
    /* Test ThreadGrader6.a: Tests priority donation: Tests priority donation by running 4 threads, 2 with equal priority and the other with higher and lower priority. */
    KThread low = forkNewThread(new Low(), 1).thread;
    KThread._yield();
    KThread med1 = forkNewThread(new Med(), 3).thread;
    KThread med2 = forkNewThread(new Med(), 3).thread;
    KThread high = forkNewThread(new High(), 4).thread;

    low.setName("low");
    med1.setName("med1");
    med2.setName("med2");
    high.setName("high");
    
    low.join();
    med2.join();
    med1.join();
    high.join();
    
    if (error_cnt != 0)
    {
    	System.out.print("\n------------------------------------------");
        System.out.print("\nTestGrader6.a failed: medium priority threads take over "+error_cnt+" times");
    	System.out.print("\n------------------------------------------\n");
    	privilege.exit(162);
    } 
    else {
    	System.out.print("\n------------------------------------------");
        System.out.print("\nTestGrader6.a succeeded");
    	System.out.print("\n------------------------------------------\n");
    }
    
    /*
     * Test ThreadGrader6.b: Tests priority donation with multiple locks
     */
    error_cnt = 0;
    KThread t1 = forkNewThread(new a(lock[0]),1).thread;
    KThread t2 = forkNewThread(new a(lock[1], lock[0]),1).thread;
    KThread t3 = forkNewThread(new a(lock[2], lock[1]),1).thread;
    KThread t4 = forkNewThread(new a(lock[2]),1).thread;
    t1.setName("t1");
    t2.setName("t2");
    t3.setName("t3");
    t4.setName("t4");

    KThread._yield();

    boolean intStatus = Machine.interrupt().disable();
    ThreadedKernel.scheduler.setPriority(t4, 3);
    
    if (ThreadedKernel.scheduler.getEffectivePriority(t1) != 3) {
    	System.out.print("\n------------------------------------------");
        System.out.println("\nPriority not correctly donated.");
    	System.out.print("\n------------------------------------------\n");
    	error_cnt++;
    }

    Machine.interrupt().restore(intStatus);

    KThread._yield();

    intStatus = Machine.interrupt().disable();
    if (ThreadedKernel.scheduler.getEffectivePriority(t1) != 1) {
    	System.out.print("\n------------------------------------------");
        System.out.println("\nPriority donation not revoked.");
    	System.out.print("\n------------------------------------------");
    	error_cnt++;
    }
    Machine.interrupt().restore(intStatus);


    /* Make sure its all finished before quitting */
    t1.join();
    t2.join();
    t3.join();
    t4.join();
    assertTrue(Machine.timer().getTime() < 2000,
    		"Too many ticks wasted on \nTest ThreadGrader6.b");

    if (error_cnt != 0)
    {
    	System.out.print("\n------------------------------------------");
    	System.out.print("\nTestGrader6.b failed "+error_cnt+" times");
    	System.out.print("\n------------------------------------------");
    	privilege.exit(162);
    }
    else {
       	System.out.print("\n------------------------------------------");
        System.out.print("\nTestGrader6.b succeeded");
    	System.out.print("\n------------------------------------------");
    	done();
    }
  }
  
  private class a implements Runnable
  {    

	  private Lock lock;
	  private Lock altLock;
	  private int num;

	  public a(Lock l1) {
		  lock = l1;
		  altLock = null;
	  }

	  public a(Lock l1, Lock l2) {
		  lock = l1;
		  altLock = l2;
	  }

	  public void run() {
		  lock.acquire();
		  if (altLock != null)
			  altLock.acquire();

		  KThread._yield();

		  if (altLock != null)
			  altLock.release();
		  lock.release();
	  }

  }


    private  class High implements Runnable {
        public High() {
        }

        public void run() {
            lock[0].acquire();
            high_run = true;
            lock[0].release();
        }
    }

    private  class Med implements Runnable {
        public Med() {
        }
        public void run() {
            for (int i = 1; i < 3; i++)
                KThread._yield();

            if (!high_run)
                error_cnt++;
        }
    }

    private  class Low implements Runnable {
        public void run() {
            lock[0].acquire();
            KThread._yield();
            lock[0].release();
        }
    }
}
