package lez02;

import java.util.ArrayList;

public class Queue<T> {
    ArrayList<T> internalQueue = new ArrayList<>();
    boolean closed = false;

    public synchronized T get() {
        while ((this.internalQueue.size() == 0) && (!this.closed)) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Interrupted. Returning null...");
                return null;
            }
        }
        T object = null;
        if (this.internalQueue.size() > 0) {
            object = this.internalQueue.get(0);
            this.internalQueue.remove(0);
        }
        return object;
    }

    public synchronized void put(T object) {
        this.internalQueue.add(object);
        notify();
    }

    public synchronized void close() {
        closed = true;
        notifyAll();
    }
}
