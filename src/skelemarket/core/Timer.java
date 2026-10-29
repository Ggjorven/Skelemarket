package skelemarket.core;

import javafx.animation.AnimationTimer;

// Timer Class is abstract class for adding extra functions to AnimationTimer like tracking time
public abstract class Timer extends AnimationTimer {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private volatile boolean mRunning = false;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	// Strat timer, override standard start() to set mRunning to true.
	@Override
	public void start() {
		super.start(); // Call original start-logic from JavaFX AnimationTimer
		mRunning = true;
	}

	// Stop timer, override the standers stop() from JavaFx tho set mRunning to false
	@Override
	public void stop() {
		super.stop();
		mRunning = false;
	}

	// Abstract method that runs on every tick, receiving the current time ('now')
	// so the simulation can calculate how much time has passed between ticks.
	@Override
	abstract public void handle(long now);

	public boolean isRunning() {
		return mRunning;
	}

	public void waitToFinish() {
		while (mRunning) {
			try {
				Thread.sleep(20);
			} catch (Exception ex) {
				System.out.printf("Exception caught: %s\n", ex.toString());
			}
		}
	}

}
