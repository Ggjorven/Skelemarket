package skelemarket.core;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import skelemarket.simulation.Supermarket;

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

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Simulation(Stage stage) {
		mStageRef = stage;

        mCanvas = new Canvas(Config.WIDTH, Config.HEIGHT);
        mContext = mCanvas.getGraphicsContext2D();
		Logger.info("Created canvas.");

        mPane = new Pane(mCanvas);
		mScene = new Scene(mPane);

        stage.setScene(mScene);
        stage.setTitle(Config.TITLE);
		stage.setResizable(false);
        stage.show();
		Logger.info("Created scene & pane.");

		mRenderer = new Renderer(mContext);
		Logger.info("Created renderer.");

		mSupermarket = new Supermarket(mRenderer);
		Logger.info("Created supermarket.");
	}

	public void run() {
		Logger.info("Started simulation");

		SimulationUpdater updater = new SimulationUpdater(mSupermarket, Config.SIMULATION_FPS);

		// Update
		updater.start();

		// Renderer
		new Timer() {
			@Override
			public void handle(long now) {
				mRenderer.clear();
				mSupermarket.render();
				// Logger.trace("Update");
			}
		}.start();;

		// Close handling
		mStageRef.setOnCloseRequest(event -> {
			Logger.info("Closing application...");

			// Interrupt and wait on updater thread
			try {
				updater.stopRunning();
				updater.join();
			}
			catch (Exception ex) {
				System.out.printf("Exception caught: %s", ex.toString());
			}
		});
	}
}



class SimulationUpdater extends Thread {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Supermarket mSupermarketRef = null;

	private int mFPS = 0;
	public long mNsPerFrame = 0;

	private volatile boolean mRunning = false;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public SimulationUpdater(Supermarket supermarketRef, int fps) {
		mSupermarketRef = supermarketRef;
		mFPS = fps;

		mNsPerFrame = (long)((1.0 / mFPS) * 1_000_000_000);
	}

	public void run() {
		mRunning = true;

		long timePassed = 0;
		long previousTime = System.nanoTime();

		while (mRunning) {
			long now = System.nanoTime();
			long deltaTime = now - previousTime;
			timePassed += deltaTime;
			previousTime = now;

			if (timePassed >= mNsPerFrame) {
				mSupermarketRef.update();

				// Logger.trace(String.format("Render - %d - %d - %d", mNsPerFrame, deltaTime, timePassed));
				timePassed = 0;
			}
		}
	}

	public void stopRunning() {
        mRunning = false;
        interrupt(); // Unblocks sleep/wait
    }
}
