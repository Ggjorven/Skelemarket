package skelemarket.simulation.map;

import skelemarket.core.*;

public class Tile {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private int mID = 0;

	private Vec2i mPosition = new Vec2i();
	private Vec2i mSize = new Vec2i();

	private Texture mTextureRef = null;
	private UV mTextureCoords = new UV();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Tile(int tileID, Vec2i position, Vec2i size, Texture textureRef, UV coords) {
		mID = tileID;

		mPosition = position;
		mSize = size;

		mTextureRef = textureRef;
		mTextureCoords = coords;
	}

	// NOTE: Can be overwritten by the deriving class
	public void update() {
	}

	public void render(Renderer rendererRef) {
		rendererRef.drawQuad(mTextureRef, mPosition, mSize, mTextureCoords);
	}

	public int getID() {
		return mID;
	}

	public Vec2i getPosition() {
		return mPosition;
	}

	public Vec2i getSize() {
		return mSize;
	}
}
