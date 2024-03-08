package nachos.threads;

import nachos.machine.*;
import sun.awt.image.ImageWatched;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

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
    private int speakers;
    private int listeners;
    private LinkedList<Integer> buf;

    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        this.lock = new Lock();
        this.condition = new Condition(lock);
        this.buf = new LinkedList<>();
        this.speakers = 0;
        this.listeners = 0;
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
        this.speakers++;

        if (this.listeners > 0) {
            this.buf.add(word);
            this.condition.wake();
        } else {
            this.buf.add(word);
            this.condition.sleep();
        }
        this.speakers--;

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
        this.listeners++;

        if (this.speakers <= 0) { // no speakers
            this.condition.sleep();
        } else {
            this.condition.wake();
        }

        int ret = this.buf.poll();
        this.listeners--;
        this.lock.release();

        return ret;
    }
}
