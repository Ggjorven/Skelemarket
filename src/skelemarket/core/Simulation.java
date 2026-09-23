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
	private Canvas mCanvas = null;
	private GraphicsContext mContext = null;

	private Pane mPane = null;
	private Scene mScene = null;

	private Renderer mRenderer = null;
	private long mNsPerFrame = 0;

	private Supermarket mSupermarket = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Simulation(Stage stage) {
        mCanvas = new Canvas(Config.WIDTH, Config.HEIGHT);
        mContext = mCanvas.getGraphicsContext2D();

        mPane = new Pane(mCanvas);
		mScene = new Scene(mPane);

        stage.setScene(mScene);
        stage.setTitle(Config.TITLE);
		stage.setResizable(false);
        stage.show();

		mRenderer = new Renderer(mContext);
		mNsPerFrame = (long)((1.0 / Config.RENDERER_FPS) * 1_000_000_000);

		mSupermarket = new Supermarket(mRenderer);
	}

	public void run() {
		Thread updater = new SimulationUpdater(mSupermarket, Config.SIMULATION_FPS);

		// Update
		updater.start();

		// Renderer
		{
			long previousTime = System.nanoTime();
			long timePassed = 0;
			
			while (true) {
				long now = System.nanoTime();
				long deltaTime = now - previousTime;
				timePassed += deltaTime;
				previousTime = now;

				if (timePassed >= mNsPerFrame)
				{
					mRenderer.clear();
					mSupermarket.render();
					System.out.printf("Render - %d - %d - %d\n", mNsPerFrame, deltaTime, timePassed);
					timePassed = 0;
				}
			}
		}

		// Join
		// try {
		// 	updater.join();
		// }
		// catch (Exception ex) {
		// 	System.out.println(String.format("Exception caught: %s", ex.toString()));
		// }
	}

	public Supermarket getSupermarket() { return mSupermarket; }
}



class SimulationUpdater extends Thread {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Supermarket mSupermarketRef = null;
	private int mFPS = 0;
	public long mNsPerFrame = 0;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public SimulationUpdater(Supermarket supermarketRef, int fps) {
		mSupermarketRef = supermarketRef;
		mFPS = fps;

		mNsPerFrame = (long)((1.0 / mFPS) * 1_000_000_000);
	}

	public void run() {
		long timePassed = 0;
		long previousTime = System.nanoTime();

		while (true) {
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
}
