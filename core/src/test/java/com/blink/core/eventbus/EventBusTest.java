package com.blink.core.eventbus;

import xyz.justblink.eventbus.AsyncEventBus;
import xyz.justblink.eventbus.EventBus;
import xyz.justblink.eventbus.Subscribe;

import java.util.concurrent.Executors;

public class EventBusTest {
    public static EventBus bus = new AsyncEventBus(Executors.newCachedThreadPool());

    public static void main(String[] args) {
        EventBusTest t = new EventBusTest();
        bus.register(t);
        bus.post(new InitiateProcessing());
        System.out.println("started");
    }

    @Subscribe
    public void receiveStartRequest(InitiateProcessing evt) {
        System.out.println("Got processing request - starting processing " + Thread.currentThread().getName());
        bus.post(new ProcessingStarted());

        System.out.println("Generating results " + Thread.currentThread().getName());
        bus.post(new ProcessingResults());
        System.out.println("Generating more results " + Thread.currentThread().getName());
        bus.post(new ProcessingResults());

        bus.post(new ProcessingFinished());
    }

    @Subscribe
    public void processingStarted(ProcessingStarted evt) {
        System.out.println("Processing has started " + Thread.currentThread().getName());
    }

    @Subscribe
    public void resultsReceived(ProcessingResults evt) {
        System.out.println("got results " + Thread.currentThread().getName());
    }
    //public static EventBus bus = new EventBus();

    @Subscribe
    public void processingComplete(ProcessingFinished evt) {
        System.out.println("Processing has completed " + Thread.currentThread().getName());
    }

    public static class InitiateProcessing {
    }

    public static class ProcessingStarted {
    }

    public static class ProcessingResults {
    }

    public static class ProcessingFinished {
    }
}
