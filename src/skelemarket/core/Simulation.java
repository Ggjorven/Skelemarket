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

		mSupermarket = new Supermarket(mRenderer);
	}

	public void run() {
		new AnimationTimer() {
            private long previousTime = System.nanoTime();

			@Override
			public void handle(long now) {
				double deltaTime = (now - previousTime) / 1_000_000_000.0;
				previousTime = now;

				// Clear screen
				mRenderer.clear();

				// Update & Render
				mSupermarket.update((float)deltaTime);
				mSupermarket.render();

				mRenderer.drawQuad(new Texture("/puzzled-skeleton.png"), new Vec2(0.0f, 0.0f), new Vec2(100.0f, 100.0f));
			}
		}.start();
	}
}
