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

	public void drawQuad(Texture texture, Vec2 position, Vec2 size) {
		drawQuad(texture, position, size, null);
	}

	public void drawQuad(Texture texture, Vec2 position, Vec2 size, UV textureCoords) {
		if (textureCoords == null) {
			mContextRef.drawImage(texture.toUnderlying(), (double)position.getX(), (double)position.getY(), (double)size.getX(), (double)size.getY());
		}
		else {
			mContextRef.drawImage(texture.toUnderlying(), (double)position.getX(), (double)position.getY(), (double)size.getX(), (double)size.getY(), (double)textureCoords.getX(), (double)textureCoords.getY(), (double)textureCoords.getWidth(), (double)textureCoords.getHeight());
		}
	}
}
