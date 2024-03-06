package nachos.threads;

import nachos.machine.*;

import java.util.PriorityQueue;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    private class WaitingThread implements Comparable {
        private WaitingThread(KThread thread, long at) {
            this.thread = thread;
            this.wakeUpTime = at;
            this.sleepLock = new Lock();
            this.sleepCondition = new Condition(this.sleepLock);
        }

        public KThread thread;
        public long wakeUpTime;
        public Lock sleepLock;
        public Condition sleepCondition;

        @Override
        public int compareTo(Object o) {
            WaitingThread t = (WaitingThread) o;
            return Long.compare(this.wakeUpTime, t.wakeUpTime);
        }
    }
    public Alarm() {
        this.queue = new PriorityQueue<WaitingThread>();
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
        long now = Machine.timer().getTime();
    while (queue.peek() != null && now >= queue.peek().wakeUpTime) {
            WaitingThread p = queue.poll();
            Lock l = p.sleepLock;
            Condition c = p.sleepCondition;
            l.acquire();
            c.wakeAll();
            l.release();
        }
        KThread.currentThread()._yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
        // for now, cheat just to get something working (busy waiting is bad)
        long wakeTime = Machine.timer().getTime() + x;

        // my work
        WaitingThread sleeper = new WaitingThread(KThread.currentThread(), wakeTime);

        sleeper.sleepLock.acquire();
        queue.add(sleeper);
        sleeper.sleepCondition.sleep();
        sleeper.sleepLock.release();

        // built in
//        while (wakeTime > Machine.timer().getTime())
//            KThread._yield();
    }

    private final PriorityQueue<WaitingThread> queue;
}
