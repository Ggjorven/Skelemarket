package skelemarket.simulation.map;

import java.util.HashMap;

import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2i;

public class Shelf extends Tile {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private String mCategory = "";
	private java.util.Map<String, Integer> mProductToCapacity = null;

	private java.util.Map<String, Integer> mProductToCount = new HashMap<>();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Shelf(int tileID, Vec2i position, Vec2i size, Texture textureRef, UV coords, String category,
			java.util.Map<String, Integer> productCapacities,
			java.util.Map<String, java.util.Map<String, ProductSpecification>> categoryToProducts) {
		super(tileID, position, size, textureRef, coords);

		mCategory = category;

		// TODO: Add random amount of products for category
	}

	@Override
	public void update() {

	}

	// TODO: Add and take methods

	public String getCategory() {
		return mCategory;
	}
}
