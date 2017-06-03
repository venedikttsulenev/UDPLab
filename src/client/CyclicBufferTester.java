package client;

import commons.CyclicBuffer;

public class CyclicBufferTester {
    public static void main(String[] args) {
        CyclicBuffer<Integer> buffer = new CyclicBuffer<>(new Integer[6]);
        System.out.println("Buffer size: " + buffer.size());
        for (Integer i : buffer)
            System.out.print(i + " ");
        try {
            buffer.put(1);
            buffer.put(2);
            buffer.put(3);
            buffer.put(4);
            buffer.put(5);
            buffer.take();
            buffer.take();
            buffer.put(25);
            buffer.put(11);
            System.out.println("Buffer size: " + buffer.size());
            for (Integer i : buffer)
                System.out.print(i + " ");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
