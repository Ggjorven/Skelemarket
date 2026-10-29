package skelemarket.simulation.map;

import skelemarket.core.*;
import skelemarket.simulation.people.PeopleRegister;

public class Tile {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	//The unike number used to separate tiles
	private int mID = 0;

	private Vec2i mPosition = new Vec2i();
	private Vec2i mSize = new Vec2i();

	private Texture mTextureRef = null;
	private UV mTextureCoords = new UV();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	// Create tile with place, size and texture
	public Tile(int tileID, Vec2i position, Vec2i size, Texture textureRef, UV coords) {
		mID = tileID;

		mPosition = position;
		mSize = size;

		mTextureRef = textureRef;
		mTextureCoords = coords;
	}

	// NOTE: Can be overwritten by the deriving class (for tiles with logics)
	public void update(Map mapRef, PeopleRegister peopleRegisterRef) {
		// empty cuz normal tile doesn't have logics
	}

	//Draws a specific tile on the window and send all necessary input to renderer
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
