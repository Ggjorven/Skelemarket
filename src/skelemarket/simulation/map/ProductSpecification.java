package skelemarket.simulation.map;

import skelemarket.core.Texture;
import skelemarket.core.UV;

public class ProductSpecification {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private String mName = "";
	private int mWeight = 1;
	private String mCategory = "";

	private Texture mTextureRef = null;
	private UV mTextureCoords = new UV();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public ProductSpecification(String name, int weight, String category, Texture textureRef, UV coords) {
		// Asserts
		if (name.isBlank())
			throw new IllegalArgumentException("Product name is required.");
		if (category.isBlank())
			throw new IllegalArgumentException("Product category is required.");
		if (weight <= 0)
			throw new IllegalArgumentException("Product weight must be positive.");

		mName = name;
		mWeight = weight;
		mCategory = category;

		mTextureRef = textureRef;
		mTextureCoords = coords;
	}

	public String getName() {
		return mName;
	}

	public int getWeight() {
		return mWeight;
	}

	public String getCategory() {
		return mCategory;
	}

	public Texture getTexture() {
		return mTextureRef;
	}

	public UV getTextureCoords() {
		return mTextureCoords;
	}

	public ProductSpecification clone() {
		return new ProductSpecification(mName, mWeight, mCategory, mTextureRef, mTextureCoords);
	}
}
