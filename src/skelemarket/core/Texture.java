package skelemarket.core;

import java.io.InputStream;

import javafx.scene.image.Image;

// Texture class for loading in textures
public class Texture {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	// Place where the images will be saved
	private Image mImage = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	//Load te image in from the saved resource path inside the project-resources
	public Texture(String path) {
		// opens a datastream to the file within the compiled app.
		InputStream stream = getClass().getResourceAsStream(path);

		// if the file has not been found it will be null
		if (stream == null) {
			// and will crash the application with an error message
			throw new IllegalArgumentException("Texture resource not found: " + path);
		}

		// load the images and safe image in JavaFX image
		mImage = new Image(stream);
	}

	// Return image width in pixels as an int
	public int getWidth() {
		return (int) mImage.getWidth();
	}

	// Return image height in pixels as an int
	public int getHeight() {
		return (int) mImage.getHeight();
	}

	// Return JavaFX image-object
	public Image toUnderlying() {
		return mImage;
	}
}
