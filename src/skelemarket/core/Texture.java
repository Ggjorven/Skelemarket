package skelemarket.core;

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
		mImage = new Image(getClass().getResource(path).toExternalForm());
	}

	public int getWidth() {
		return (int)mImage.getWidth();
	}

	public int getHeight() {
		return (int)mImage.getHeight();
	}

	public Image toUnderlying() {
		return mImage;
	}
}
