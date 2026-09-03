package skelemarket.core;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Simulation {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Canvas mCanvas = null;
	private GraphicsContext mContext = null;

	private Pane mPane = null;
	private Scene mScene = null;

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
        stage.show();

		// TODO: Custom renderer
	}

	public void run() {
		new AnimationTimer() {
            private long previousTime = System.nanoTime();

			@Override
			public void handle(long now) {
				double deltaTime = (now - previousTime) / 1_000_000_000.0f;
				previousTime = now;

				// TODO: Update
				// TODO: Render
			}
		}.start();
	}
}
