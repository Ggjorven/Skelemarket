package skelemarket.simulation.map;

import java.util.HashMap;
import java.util.Map;

import skelemarket.core.Renderer;
import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2;
import skelemarket.simulation.map.Product.ProductCategory;
import skelemarket.simulation.map.Product.ProductType;

public class Shelf {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Vec2 mPosition = new Vec2();
	private Vec2 mSize = new Vec2();

	private Texture mTextureRef = null;
	private UV mTextureCoords = new UV();

	private ProductCategory mCategory = ProductCategory.Fruit;
	private Map<ProductType, Integer> mProducts = new HashMap<>(); // TOOD: Fix

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Shelf(Vec2 position, Vec2 size, ProductCategory category, Texture textureRef, UV coords) {
		mPosition = position;
		mSize = size;

		mTextureRef = textureRef;
		mTextureCoords = coords;

		mCategory = category;

		// TODO: Add random amount of products for category
	}

	public void render(Renderer rendererRef) {
		rendererRef.drawQuad(mTextureRef, mPosition, mSize, mTextureCoords);
		
	}

	// TODO: Add and take methods
}
