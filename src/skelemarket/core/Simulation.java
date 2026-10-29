package skelemarket.core;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import skelemarket.simulation.Supermarket;

// This is the Class that will run the simulation-loop
public class Simulation {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Stage mStageRef = null;

	private Canvas mCanvas = null;
	private GraphicsContext mContext = null;

	private Pane mPane = null;
	private Scene mScene = null;

	private Renderer mRenderer = null;
	private Supermarket mSupermarket = null;

	private long mLastFrameTime;
	private long mUpdateAccumulator;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	// Create a window and initializes all subsystems
	public Simulation(Stage stage) throws Exception {
		mStageRef = stage;

		// Create canvas
		mCanvas = new Canvas(Config.WIDTH, Config.HEIGHT);
		mContext = mCanvas.getGraphicsContext2D();
		Logger.info("Created canvas.");

		// will be used to creat and render the simulation
		mPane = new Pane(mCanvas);
		mScene = new Scene(mPane);

		// Configures the Window
		stage.setScene(mScene);
		stage.setTitle(Config.TITLE);
		stage.setResizable(false);	// This will not allow window resize
		stage.show();				// Show window on screen
		Logger.info("Created scene & pane.");

		// Connect the renderer to JavaFX GraphicContext
		mRenderer = new Renderer(mContext);
		Logger.info("Created renderer.");

		// try to start supermarket simulation
		try {
			mSupermarket = new Supermarket();
			Logger.info("Created supermarket.");
		} catch (Exception ex) {
			Logger.error("Failed to create Supermarket due to error: %s.", ex.toString());
			throw new Exception(String.format("Failed to create Supermarket due to error: %s.", ex.toString()));
		}
	}

	// Start simulation and activate to in Infinite loop for update and rendering
	public void run() {
		Logger.info("Started simulation");

		// Get current time in nanoseconds as startpoint
		mLastFrameTime = System.nanoTime();

		// Start JavaFX timer
		new Timer() {
			@Override
			public void handle(long now) {
				// Calculate logic-updates (actions and movement)
				update(now);

				// Clear the screen
				mRenderer.clear();

				// Draw the supermarket on the new position
				mSupermarket.render(mRenderer);
			}
		}.start(); // activate the timer
	}

	// The update method uses a Fixed timestep, used to set a standard speed
	private void update(long now) {
		// Calculates the amount of nanoseconds in a tick
		long updateInterval = 1_000_000_000L / Config.UPDATE_TICKS_PER_SECOND;

		// Calculates how much times has been really passed since the previous frame
		mUpdateAccumulator += now - mLastFrameTime;
		mLastFrameTime = now; // Saves the time

		// as long as there is enough time passed in the buffer to update
		while (mUpdateAccumulator >= updateInterval) {
			mSupermarket.update(); // update supermarket logics (Customer walking, etc).
			mUpdateAccumulator -= updateInterval; // decrement update time from the buffer
		}
	}
}
