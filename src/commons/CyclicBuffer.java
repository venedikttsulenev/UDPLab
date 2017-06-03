package commons;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CyclicBuffer<T> implements Iterable<T> {
    private final Object lock = new Object();

    private final T[] elements;
    private int head = 0;
    private int tail = 0;

    private int next_ind(int i) {
        return i < elements.length - 1 ? i + 1 : 0;
    }

    private int prev_ind(int i) {
        return i > 0 ? i - 1 : elements.length - 1;
    }

    private class CyclicBufferIterator implements Iterator<T> {
        private int index = head;
        private boolean has_next() {
            return (tail > head && index >= head && index < tail)
                    || (tail < head && (index < tail || index >= head));
        }

        @Override
        public boolean hasNext() {
            synchronized (lock) {
                return has_next();
            }
        }

        @Override
        public T next() {
            synchronized (lock) {
                if (!has_next())
                    throw new NoSuchElementException();
                final T r = elements[index];
                index = next_ind(index);
                return r;
            }
        }
    }

    public CyclicBuffer(T[] buf) { /* Passed array must have capacity for one dummy element */
        this.elements = buf;
    }

    public void put(T e) throws InterruptedException {
        synchronized (lock) {
            while (tail == prev_ind(head))
                lock.wait();
            elements[tail] = e;
            tail = next_ind(tail);
            lock.notifyAll();
        }
    }

    public T first() throws InterruptedException {
        synchronized (lock) {
            while (head == tail)
                lock.wait();
            return elements[head];
        }
    }

    public T take() throws InterruptedException {
        synchronized (lock) {
            while (head == tail)
                lock.wait();
            final T r = elements[head];
            head = next_ind(head);
            lock.notifyAll();
            return r;
        }
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return head == tail;
        }
    }

    public int size() {
        synchronized (lock) {
            if (head < tail)
                return tail - head;
            if (head > tail)
                return elements.length + tail - head;
            return 0;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new CyclicBufferIterator();
    }
}
