package skelemarket.core;

public class Vec2i {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private int mX = 0;
	private int mY = 0;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Vec2i() {
		mX = 0;
		mY = 0;
	}

	public Vec2i(int x, int y) {
		mX = x;
		mY = y;
	}

	public void setX(int x) {
		mX = x;
	}

	public int getX() {
		return mX;
	}

	public void setY(int y) {
		mY = y;
	}

	public int getY() {
		return mY;
	}
}
