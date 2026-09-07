package skelemarket.core;

import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;

public class Texture {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Image mImage = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Texture(String path) {
		// NOTE: This implementation doesn't work when a resource is placed in a subfolder (sometimes)
		// I can't exactly figure out why, but claude thinks it's because of aggressive JPMS checks.
		// Using .getResourceAsStream has less checks built in so it works.

		// mImage = new Image(getClass().getResource(path).toExternalForm());

		// try (InputStream stream = getClass().getModule().getResourceAsStream(path)) {
		// 	if (stream == null)
		// 		throw new RuntimeException("Texture not found: " + path);
		// 	mImage = new Image(stream);
		// } catch (IOException e) {
		// 	throw new RuntimeException("Failed to load texture: " + path, e);
		// }

		mImage = new Image(getClass().getClassLoader().getResource(path).toExternalForm());
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
