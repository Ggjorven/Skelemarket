package skelemarket.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// The Renderer class will draw object (shapes and textures) on the screen
public class Renderer {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////

	// Reference to GraphicsContext of JavaFX where will be draws
	private GraphicsContext mContextRef = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////

	// The Constructor initializes the renderer with JavaFX GraphicContent
	public Renderer(GraphicsContext contextRef) {
		mContextRef = contextRef;

		// Disable image smoothing (linear filtering) for pixel art
		mContextRef.setImageSmoothing(false);
	}

	// Clears the Screen based on Height and Width with every tick.
	public void clear() {
		mContextRef.clearRect(0.0, 0.0, (double) Config.WIDTH, (double) Config.HEIGHT);
	}

	// A tile (int) will be Converted to a float
	public void drawQuad(Colour colour, Vec2i position, Vec2i size) {
		drawQuad(colour, new Vec2f((float) position.getX(), (float) position.getY()),
				new Vec2f((float) size.getX(), (float) size.getY()));
	}

	// Draws the quad with float-values
	public void drawQuad(Colour colour, Vec2f position, Vec2f size) {
		// JavaFX expects a value between 0.0 and 1.0 so the values of RGBA will be divided by 255.0
		mContextRef.setFill(new Color(colour.getR() / 255.0, colour.getG() / 255.0, colour.getB() / 255.0,
				colour.getA() / 255.0));

		// Draws the square with the x, y position
		mContextRef.fillRect((double) position.getX(), (double) position.getY(), (double) size.getX(),
				(double) size.getY());
	}

	//
	public void drawQuad(Texture texture, Vec2i position, Vec2i size) {
		drawQuad(texture, position, size, null);
	}

	public void drawQuad(Texture texture, Vec2f position, Vec2f size) {
		drawQuad(texture, position, size, null);
	}

	//Convert integer position and size to float to call Main Methods with UV-coords
	public void drawQuad(Texture texture, Vec2i position, Vec2i size, UV textureCoords) {
		drawQuad(texture, new Vec2f(position.getX(), position.getY()), new Vec2f(size.getX(), size.getY()),
				textureCoords);
	}

	// The Main methode for drawing textures (will also draw cutouts of a big image)
	public void drawQuad(Texture texture, Vec2f position, Vec2f size, UV textureCoords) {
		if (textureCoords == null) {
			// if there is no specific UV coords than it will draw the full image
			mContextRef.drawImage(texture.toUnderlying(), (double) position.getX(), (double) position.getY(),
					(double) size.getX(), (double) size.getY());
		} else {
			// First you specify the coords where the images will be cut out, then the size.
			mContextRef.drawImage(texture.toUnderlying(),
					(double) textureCoords.getX(), (double) textureCoords.getY(),
					(double) textureCoords.getWidth(), (double) textureCoords.getHeight(),
					(double) position.getX(), (double) position.getY(), (double) size.getX(),
					(double) size.getY());
		}
	}

	//Draw a line between two points for pathfinding debug renderer
	public void drawLine(Colour colour, Vec2i start, Vec2i end) {
		// set a colour
		mContextRef.setStroke(new Color(colour.getR() / 255.0, colour.getG() / 255.0, colour.getB() / 255.0,
				colour.getA() / 255.0));
		// draw the line between start and end coords
		mContextRef.strokeLine((double) start.getX(), (double) start.getY(), (double) end.getX(),
				(double) end.getY());
	}
}
