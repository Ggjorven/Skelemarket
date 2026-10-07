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

		mSupermarket = new Supermarket();
		Logger.info("Created supermarket.");
	}

	public void run() {
		Logger.info("Started simulation");

		// Update & render
		new Timer() {
			@Override
			public void handle(long now) {
				mSupermarket.update();

				mRenderer.clear();
				mSupermarket.render(mRenderer);
			}
		}.start();
	}
}
