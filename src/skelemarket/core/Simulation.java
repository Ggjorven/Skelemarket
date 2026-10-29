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

	private long mLastFrameTime;
	private long mUpdateAccumulator;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Simulation(Stage stage) throws Exception {
		mStageRef = stage;

		mCanvas = new Canvas(Config.WIDTH, Config.HEIGHT);
		mContext = mCanvas.getGraphicsContext2D();
		Logger.info("Created canvas.");

		mPane = new Pane(mCanvas);
		mScene = new Scene(mPane);

		mStageRef.setScene(mScene);
		mStageRef.setTitle(Config.TITLE);
		mStageRef.setResizable(false);
		mStageRef.show();
		Logger.info("Created scene & pane.");

		mRenderer = new Renderer(mContext);
		Logger.info("Created renderer.");

		try {
			mSupermarket = new Supermarket();
			Logger.info("Created supermarket.");
		} catch (Exception ex) {
			Logger.error("Failed to create Supermarket due to error: %s.", ex.toString());
			throw new Exception(String.format("Failed to create Supermarket due to error: %s.", ex.toString()));
		}
	}

	public void run() {
		Logger.info("Started simulation");

		mLastFrameTime = System.nanoTime();

		new Timer() {
			@Override
			public void handle(long now) {
				update(now);

				mRenderer.clear();
				mSupermarket.render(mRenderer);
			}
		}.start();
	}

	private void update(long now) {
		long updateInterval = 1_000_000_000L / Config.UPDATE_TICKS_PER_SECOND;
		mUpdateAccumulator += now - mLastFrameTime;
		mLastFrameTime = now;

		while (mUpdateAccumulator >= updateInterval) {
			mSupermarket.update();
			mUpdateAccumulator -= updateInterval;
		}
	}
}
