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
		mContextRef.clearRect(0.0, 0.0, (double) Config.WIDTH, (double) Config.HEIGHT);
	}

	public void drawQuad(Colour colour, Vec2i position, Vec2i size) {
		drawQuad(colour, new Vec2f((float) position.getX(), (float) position.getY()),
				new Vec2f((float) size.getX(), (float) size.getY()));
	}

	public void drawQuad(Colour colour, Vec2f position, Vec2f size) {
		mContextRef.setFill(new Color((double) colour.getR(), (double) colour.getG(), (double) colour.getB(),
				colour.getA() / 255.0));
		mContextRef.fillRect((double) position.getX(), (double) position.getY(), (double) size.getX(),
				(double) size.getY());
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
			mContextRef.drawImage(texture.toUnderlying(),
					(double) textureCoords.getX(), (double) textureCoords.getY(),
					(double) textureCoords.getWidth(), (double) textureCoords.getHeight(),
					(double) position.getX(), (double) position.getY(), (double) size.getX(),
					(double) size.getY());
		}
	}
}
