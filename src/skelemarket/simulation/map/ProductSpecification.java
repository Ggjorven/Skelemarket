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
		mName = name;
		mWeight = weight;
		mCategory = category;

		mTextureRef = textureRef;
		mTextureCoords = coords;
	}
}
