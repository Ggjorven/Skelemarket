package skelemarket.simulation.map;

import skelemarket.core.*;

public class Tile {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	protected Vec2i mPosition = new Vec2i();
	private Vec2i mSize = new Vec2i();

	private Texture mTextureRef = null;
	private UV mTextureCoords = new UV();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Tile(Vec2i position, Vec2i size, Texture textureRef, UV coords) {
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
}
