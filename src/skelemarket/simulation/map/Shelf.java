package skelemarket.simulation.map;

import skelemarket.core.Renderer;
import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2i;

public class Shelf {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Vec2i mPosition = new Vec2i();
	private Vec2i mSize = new Vec2i();

	private Texture mTextureRef = null;
	private UV mTextureCoords = new UV();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Shelf(Vec2i position, Vec2i size, Texture textureRef, UV coords) {
		mPosition = position;
		mSize = size;

		mTextureRef = textureRef;
		mTextureCoords = coords;

		// TODO: Add random amount of products for category
	}

	public void render(Renderer rendererRef) {
		rendererRef.drawQuad(mTextureRef, mPosition, mSize, mTextureCoords);
	}

	// TODO: Add and take methods
}
