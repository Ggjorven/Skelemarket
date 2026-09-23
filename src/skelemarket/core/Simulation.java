package skelemarket.core;

import javafx.animation.AnimationTimer;
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

        mPane = new Pane(mCanvas);
		mScene = new Scene(mPane);

        stage.setScene(mScene);
        stage.setTitle(Config.TITLE);
		stage.setResizable(false);
        stage.show();

		mRenderer = new Renderer(mContext);

		mSupermarket = new Supermarket(mRenderer);
	}

	public void run() {
		SimulationUpdater updater = new SimulationUpdater(mSupermarket, Config.SIMULATION_FPS);

		// Update
		updater.start();

		// Renderer
		new Timer() {
			@Override
			public void handle(long now) {
				mRenderer.clear();
				mSupermarket.render();
				System.out.printf("Update\n");
			}
		}.start();;

		// Close handling
		mStageRef.setOnCloseRequest(event -> {
			// Interrupt and wait on updater thread
			try {
				updater.stopRunning();
				updater.join();
			}
			catch (Exception ex) {
				System.out.printf("Exception caught: %s\n", ex.toString());
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

				System.out.printf("Update - %d - %d - %d\n", mNsPerFrame, deltaTime, timePassed);
				timePassed = 0;
			}
		}
	}

	public void stopRunning() {
        mRunning = false;
        interrupt(); // Unblocks sleep/wait
    }
}
