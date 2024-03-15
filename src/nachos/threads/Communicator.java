package nachos.threads;


import java.util.LinkedList;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    private static class WaitingThread {
        private final Lock lock;
        private final Condition condition;
        private Integer word;

        public WaitingThread(Integer word) {
            this.word = word;
            this.lock = new Lock();
            this.condition = new Condition(this.lock);
        }
    }

    private final LinkedList<WaitingThread> speakingQueue;
    private final LinkedList<WaitingThread> listeningQueue;

    private final Lock lock;

    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        this.speakingQueue = new LinkedList<>();
        this.listeningQueue = new LinkedList<>();
        this.lock = new Lock();
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
        if (!listeningQueue.isEmpty()) { // listener available
            WaitingThread listener = listeningQueue.pop();
            listener.word = word;
            listener.lock.acquire();
            listener.condition.wake();
            listener.lock.release();
        } else {
            WaitingThread speaker = new WaitingThread(word);
            speaker.lock.acquire();
            speakingQueue.add(speaker);
            this.lock.release();
            speaker.condition.sleep();
            this.lock.acquire();
            speaker.lock.release();
        }

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
        int word;
        if (!speakingQueue.isEmpty()) {
            WaitingThread speaker = speakingQueue.pop();
            word = speaker.word;

            speaker.lock.acquire();
            speaker.condition.wake();
            speaker.lock.release();
        } else {
            WaitingThread listener = new WaitingThread(null);
            listener.lock.acquire();
            listeningQueue.add(listener);
            this.lock.release();
            listener.condition.sleep();
            this.lock.acquire();
            word = listener.word;
            listener.lock.release();
        }
        this.lock.release();
        return word;
    }
}
