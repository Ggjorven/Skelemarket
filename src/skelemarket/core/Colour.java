package skelemarket.core;

public class Colour {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private int mR = 0;
	private int mG = 0;
	private int mB = 0;
	private int mA = 255;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Colour() {
		mR = 0;
		mG = 0;
		mB = 0;
		mA = 255;
	}

	public Colour(int red, int green, int blue) {
		mR = red;
		mG = green;
		mB = blue;
		mA = 255;
	}

	public Colour(int red, int green, int blue, int alpha) {
		mR = red;
		mG = green;
		mB = blue;
		mA = alpha;
	}

	public void setR(int r) {
		mR = Math.clamp(r, 0, 255);
	}

	public int getR() {
		return mR;
	}

	public void setG(int g) {
		mG = Math.clamp(g, 0, 255);
	}

	public int getG() {
		return mG;
	}

	public void setB(int b) {
		mB = Math.clamp(b, 0, 255);
	}

	public int getB() {
		return mB;
	}

	public void setA(int a) {
		mA = Math.clamp(a, 0, 255);
	}

	public int getA() {
		return mA;
	}

	public String toString() {
		return String.format("[%d, %d, %d, %d]", mR, mG, mB, mA);
	}
}
