package skelemarket.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Renderer {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private GraphicsContext mContextRef = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Renderer(GraphicsContext contextRef) {
		mContextRef = contextRef;
	}

	public void clear() {
		mContextRef.setFill(Color.BLACK);
		mContextRef.fillRect(0.0, 0.0, (double)Config.WIDTH, (double)Config.HEIGHT);
	}
}
