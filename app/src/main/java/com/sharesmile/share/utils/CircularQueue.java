package com.sharesmile.share.utils;

/**
 * Circular Queue- implemented using Array.
 *
 * Created by ankitmaheshwari on 3/1/17.
 */
public class CircularQueue<E> {

    private static final String TAG = "CircularQueue";

    private E[] circularQueueAr;
    private int maxSize;   //Maximum Size of Circular Queue
    private int rear;//elements will be added/queued at rear.
    private int front;   //elements will be removed/dequeued from front
    private int number; //number of elements currently in Priority Queue

    /**
     * Constructor
     */
    public CircularQueue(int maxSize){
        this.maxSize = maxSize;
        circularQueueAr = (E[])new Object[this.maxSize];
        number=0; //Initially number of elements in Circular Queue are 0.
        front=0;
        rear=0;
    }

    public synchronized E getElemAtPosition(int pos){
        if (pos >= circularQueueAr.length){
            throw new IndexOutOfBoundsException("Index value " + pos
                    + " is greater than queue max size " + circularQueueAr.length);
        }else if (pos < 0){
            throw new IndexOutOfBoundsException("Index cannot be negative");
        }
        int actualIndex = (front + pos) % circularQueueAr.length;
        return circularQueueAr[actualIndex];
    }

    public synchronized E peekLatest(){
        if (!isEmpty()){
            int latestPos = ((rear - 1) + circularQueueAr.length) % circularQueueAr.length;
            return circularQueueAr[latestPos];
        }
        return null;
    }


    public synchronized E peekOldest(){
        if (!isEmpty()){
            return circularQueueAr[front];
        }
        return null;
    }

    public int getMaxSize(){
        return maxSize;
    }

    public int getCurrentSize(){
        return number;
    }

    public synchronized void add(E item){
        if (isFull()){
            dequeue();
        }
        enqueue(item);
    }

    public synchronized void clear(){
        while (!isEmpty()){
            dequeue();
        }
    }


    /**
     * Adds element in Circular Queue(at rear)
     */
    public synchronized void enqueue(E item) throws IllegalStateException {
        if(isFull()){
            throw new IllegalStateException("Circular Queue is full");
        }else{
            circularQueueAr[rear] = item;
            rear = (rear + 1) % circularQueueAr.length;
            number++; // increase number of elements in Circular queue
        }
    }


    /**
     * Removes element from Circular Queue(from front)
     */
    public synchronized E dequeue() throws IllegalStateException {
        E deQueuedElement;
        if(isEmpty()){
            throw new IllegalStateException("Circular Queue is empty");
        }else{
            deQueuedElement = circularQueueAr[front];
            circularQueueAr[front] = null;
            front = (front + 1) % circularQueueAr.length;
            number--; // Reduce number of elements from Circular queue
        }
        return deQueuedElement;
    }

    /**
     * Return true if Circular Queue is full.
     */
    public boolean isFull() {
        return (number==circularQueueAr.length);
    }


    /**
     * Return true if Circular Queue is empty.
     */
    public boolean isEmpty() {
        return (number==0);
    }


}
