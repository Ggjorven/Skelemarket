package skelemarket.core;

import java.io.InputStream;

import javafx.scene.image.Image;

public class Texture {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Image mImage = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Texture(String path) {
		InputStream stream = getClass().getResourceAsStream(path);

		if (stream == null) {
			throw new IllegalArgumentException("Texture resource not found: " + path);
		}

		mImage = new Image(stream);
	}

	public int getWidth() {
		return (int) mImage.getWidth();
	}

	public int getHeight() {
		return (int) mImage.getHeight();
	}

	public Image toUnderlying() {
		return mImage;
	}
}
