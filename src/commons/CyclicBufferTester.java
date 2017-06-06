package commons;

public class CyclicBufferTester {
    public static void main(String[] args) {
        CyclicBuffer<Integer> buffer = new CyclicBuffer<>(new Integer[6]);
        System.out.println("Buffer size: " + buffer.size());
        for (Integer i : buffer)
            System.out.print(i + " ");
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);
        buffer.add(4);
        buffer.add(5);
        System.out.println(buffer.poll());
        System.out.println(buffer.poll());
        buffer.add(25);
        buffer.add(11);
        System.out.println("Buffer size: " + buffer.size());
        for (Integer i : buffer)
            System.out.print(i + " ");
    }
}
