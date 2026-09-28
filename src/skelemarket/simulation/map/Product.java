package skelemarket.simulation.map;

import java.util.HashMap;
import java.util.Map;

import skelemarket.core.Texture;
import skelemarket.core.UV;

public class Product {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private String mName = "";
	private String mCategory = "";

	private Texture mTextureRef = null;
	private UV mTextureCoords = new UV();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Product(String name, String category, Texture textureRef, UV coords) {
		mTextureRef = textureRef;
		mTextureCoords = coords;
	}
}
