package skelemarket.simulation.map;

import java.util.HashMap;
import java.util.Map;

import skelemarket.core.Renderer;
import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2;

public class Shelf {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Vec2 mPosition = new Vec2();
	private Vec2 mSize = new Vec2();

	private Texture mTextureRef = null;
	private UV mTextureCoords = new UV();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Shelf(Vec2 position, Vec2 size, Texture textureRef, UV coords) {
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
