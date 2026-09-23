package skelemarket.core;

import javafx.animation.AnimationTimer;

public abstract class Timer extends AnimationTimer {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
    private volatile boolean mRunning = false;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
    @Override
    public void start() {
         super.start();
         mRunning = true;
    }

    @Override
    public void stop() {
        super.stop();
        mRunning = false;
    }

	@Override
	abstract public void handle(long now);


    public boolean isRunning() {
        return mRunning;
    }

	public void waitToFinish() {
		while (mRunning) {
			try {
				Thread.sleep(20);
			}
			catch (Exception ex) {
				System.out.printf("Exception caught: %s\n", ex.toString());
			}
		}
	}

}
