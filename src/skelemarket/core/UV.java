package skelemarket.core;

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
	public UV() {
		mX = 0;
		mY = 0;

		mWidth = 0;
		mHeight = 0;
	}

	public UV(int x, int y, int width, int height) {
		mX = x;
		mY = y;

		mWidth = width;
		mHeight = height;
	}

	public UV(Texture textureRef) {
		mX = 0;
		mY = 0;

		mWidth = textureRef.getWidth();
		mHeight = textureRef.getHeight();
	}

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
