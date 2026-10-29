package skelemarket.core;

// This Class is for saving coords from a cut out of an image
public class UV {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private int mX = 0;
	private int mY = 0;

	private int mWidth = 0;
	private int mHeight = 0;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	// Makes an empty UV
	public UV() {
		mX = 0;
		mY = 0;

		mWidth = 0;
		mHeight = 0;
	}

	// Makes a box on the self chosen coords and size
	public UV(int x, int y, int width, int height) {
		mX = x;
		mY = y;

		mWidth = width;
		mHeight = height;
	}

	// Takes automatic the full size of a texture
	public UV(Texture textureRef) {
		mX = 0; // Start on the left upper corner
		mY = 0;

		// Take the full width and height of an image
		mWidth = textureRef.getWidth();
		mHeight = textureRef.getHeight();
	}

	///////////////////////////////////////////////////////////
	// Getters en Setters
	///////////////////////////////////////////////////////////

	public void setX(int x) {
		mX = x;
	}

	public float getX() {
		return mX;
	}

	public void setY(int y) {
		mY = y;
	}

	public float getY() {
		return mY;
	}

	public void setWidth(int width) {
		mWidth = width;
	}

	public int getWidth() {
		return mWidth;
	}

	public void setHeight(int height) {
		mHeight = height;
	}

	public int getHeight() {
		return mHeight;
	}
}
