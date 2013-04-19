package ica.exam;

public class StopWatch {
    private long startTime = 0;
    private long stopTime = 0;
    private long elapsed = 0;
    private boolean running = false;

    
    public void start() {
        this.startTime = System.nanoTime();
        this.running = true;
    }
    
    public void stop() {
        this.stopTime = System.nanoTime();
        this.running = false;
    }

    public void reset() {
        this.startTime = 0;
        this.stopTime = 0;
        this.running = false;
    }    
    
    public long getElapsedTimeMicro() {
        if (running) {
            elapsed = ((System.nanoTime() - startTime) / 1000);
        }
        else {
            elapsed = ((stopTime - startTime) / 1000);
        }
        return elapsed;
    }
      
    public long getElapsedTimeMilli() {
        if (running) {
            elapsed = ((System.nanoTime() - startTime) / 1000000);
        }
        else {
            elapsed = ((stopTime - startTime) / 1000000);
        }
        return elapsed;
    }
    
    public static int[] splitToComponentTimes(long biggy)
    {
        //long longVal = biggy.longValue();
    	long longVal = biggy;
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return ints;
    }    
}
