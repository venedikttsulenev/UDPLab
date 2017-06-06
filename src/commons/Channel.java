package commons;

import java.util.LinkedList;

public class Channel<T> {
    private final int maxSize;
    private final LinkedList<T> queue = new LinkedList<>();
    private final Object lock = new Object();

    public Channel(int maxSize) {
        this.maxSize = maxSize;
    }

    public void put(T x) throws InterruptedException {
        synchronized (lock) {
            while (queue.size() == maxSize)
                lock.wait();
            queue.addLast(x);
            lock.notifyAll();
        }
    }

    public T take() throws InterruptedException {
        synchronized (lock) {
            while (queue.isEmpty())
                lock.wait();
            lock.notifyAll();
            return queue.removeFirst();
        }
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return queue.isEmpty();
        }
    }

    public int size() {
        synchronized (lock) {
            return queue.size();
        }
    }

    public int maxSize() {
        return maxSize;
    }
}