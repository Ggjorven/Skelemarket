package skelemarket.core;

import javafx.scene.canvas.GraphicsContext;

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
		mContextRef.clearRect(0.0, 0.0, (double) Config.WIDTH, (double) Config.HEIGHT);
	}

	public void drawQuad(Texture texture, Vec2i position, Vec2i size) {
		drawQuad(texture, position, size, null);
	}

	public void drawQuad(Texture texture, Vec2f position, Vec2f size) {
		drawQuad(texture, position, size, null);
	}

	public void drawQuad(Texture texture, Vec2i position, Vec2i size, UV textureCoords) {
		drawQuad(texture, new Vec2f(position.getX(), position.getY()), new Vec2f(size.getX(), size.getY()),
				textureCoords);
	}

	public void drawQuad(Texture texture, Vec2f position, Vec2f size, UV textureCoords) {
		if (textureCoords == null) {
			mContextRef.drawImage(texture.toUnderlying(), (double) position.getX(), (double) position.getY(),
					(double) size.getX(), (double) size.getY());
		} else {
			mContextRef.drawImage(texture.toUnderlying(), (double) position.getX(), (double) position.getY(),
					(double) size.getX(), (double) size.getY(), (double) textureCoords.getX(),
					(double) textureCoords.getY(), (double) textureCoords.getWidth(),
					(double) textureCoords.getHeight());
		}
	}
}
