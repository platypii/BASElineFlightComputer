package com.platypii.baseline;

/**
 * Thread to ping the mobile device for liveness
 */
class PingRunnable implements Runnable {

    private final WearSlave wear;
    private boolean running = false;
    private static final long updateInterval = 1000; // milliseconds

    PingRunnable(WearSlave wear) {
        this.wear = wear;
    }

    @Override
    public void run() {
        running = true;
        while(running) {
            wear.sendPing();
            try {
                Thread.sleep(updateInterval);
            } catch(InterruptedException e) {}
        }
    }

    void stop() {
        running = false;
        // TODO: Interrupt thread?
    }
}
