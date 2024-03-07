package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    private final Condition condition;
    private final Lock lock;
    private int balance;
    private Integer buf;

    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        this.lock = new Lock();
        this.condition = new Condition(lock);
        this.buf = null;
        this.balance = 0;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
        this.lock.acquire();

        if (this.buf != null) { // another speaker is waiting to be paired
            this.condition.sleep();
        }

        this.buf = word;

        if (this.balance > 0) { // a listener was waiting for us
            this.condition.wake();
        }

        this.condition.sleep(); // wait for listener, finished with a listener once we return
        this.lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
        this.lock.acquire();

        if (this.buf == null) {
            if (this.balance < 0) {
                this.condition.wake();
            }
            this.balance++;
            this.condition.sleep();
        }

        int ret = this.buf;
        this.buf = null;
        this.condition.wake();
        this.lock.release();

        return ret;
    }
}
