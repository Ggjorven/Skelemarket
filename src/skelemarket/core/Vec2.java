package skelemarket.core;

public class Vec2 {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private float mX = 0.0f;
	private float mY = 0.0f;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Vec2() {
		mX = 0.0f;
		mY = 0.0f;
	}

	public Vec2(float x, float y) {
		mX = x;
		mY = y;
	}

	public void setX(float x) {
		mX = x;
	}

	public float getX() {
		return mX;
	}

	public void setY(float y) {
		mY = y;
	}

	public float getY() {
		return mY;
	}
}
