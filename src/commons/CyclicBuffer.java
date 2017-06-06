package commons;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CyclicBuffer<T> implements Iterable<T> {
    private final T[] elements;
    private int head = 0;
    private int tail = 0;

    private int next_ind(int i) {
        return i < elements.length - 1 ? i + 1 : 0;
    }

    private class CyclicBufferIterator implements Iterator<T> {
        private int index = head;

        @Override
        public boolean hasNext() {
            return (tail > head && index >= head && index < tail)
                    || (tail < head && (index < tail || index >= head));
        }

        @Override
        public T next() {
            if (!hasNext())
                throw new NoSuchElementException();
            final T r = elements[index];
            index = next_ind(index);
            return r;
        }
    }

    public CyclicBuffer(T[] buf) { /* Passed array must have capacity for one dummy element */
        this.elements = buf;
    }

    public void add(T e) {
        if (next_ind(tail) == head)
            throw new IllegalStateException();
        elements[tail] = e;
        tail = next_ind(tail);
    }

    public T peek() {
        return tail != head ? elements[head] : null;
    }

    public T poll() {
        if (head == tail)
            return null;
        final T r = elements[head];
        elements[head] = null;
        head = next_ind(head);
        return r;
    }

    public int size() {
        if (head < tail)
            return tail - head;
        if (head > tail)
            return elements.length + tail - head;
        return 0;
    }

    public int capacity() {
        return elements.length - 1;
    }

    @Override
    public Iterator<T> iterator() {
        return new CyclicBufferIterator();
    }
}
