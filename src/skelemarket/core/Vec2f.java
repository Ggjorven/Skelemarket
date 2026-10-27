package skelemarket.core;

public class Vec2f {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private float mX = 0.0f;
	private float mY = 0.0f;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Vec2f() {
		mX = 0.0f;
		mY = 0.0f;
	}

	public Vec2f(float x, float y) {
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

	public String toString() {
		return String.format("[%.1f, %.1f]", mX, mY);
	}
}
