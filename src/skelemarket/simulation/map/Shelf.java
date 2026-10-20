package skelemarket.simulation.map;

import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2i;

import java.util.HashMap;

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

		// Asserts
		if (category.isBlank())
			throw new IllegalArgumentException("Shelf must have a category...");
		if (productCapacities.isEmpty())
			throw new IllegalArgumentException("Shelf must hold some products...");

		mCategory = category;
		mProductToCapacity = new HashMap<>(productCapacities);

		for (java.util.Map.Entry<String, Integer> entry : mProductToCapacity.entrySet()) {
			if (category.isBlank() || !categoryToProducts.get(category).containsKey(entry.getKey())
					|| entry.getValue() < 0) {
				throw new IllegalArgumentException("Invalid shelf inventory entry: " + entry.getKey());
			}
			mProductToCount.put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void update() {
		// TODO: Update map
	}

	public int addProduct(String productName, int amount) {
		if (amount < 0 || !mProductToCapacity.containsKey(productName))
			return 0;

		int accepted = Math.min(amount, mProductToCapacity.get(productName) - getProductCount(productName));
		mProductToCount.put(productName, getProductCount(productName) + accepted);
		return accepted;
	}

	public boolean takeProduct(String productName) {
		int count = getProductCount(productName);

		if (count == 0)
			return false;

		mProductToCount.put(productName, count - 1);
		return true;
	}

	public int getProductCount(String productName) {
		return mProductToCount.getOrDefault(productName, 0);
	}

	public int getProductCapacity(String productName) {
		return mProductToCapacity.getOrDefault(productName, 0);
	}

	public java.util.Map<String, Integer> getProductCapacities() {
		return java.util.Map.copyOf(mProductToCapacity);
	}

	public String getCategory() {
		return mCategory;
	}
}
