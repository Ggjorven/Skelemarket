package skelemarket.simulation.map;

import java.util.HashMap;
import java.util.Map;

import skelemarket.core.Texture;
import skelemarket.core.UV;

public class Product {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Texture mTextureRef = null;
	private UV mTextureCoords = new UV();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Product(Texture textureRef, UV coords) {
		mTextureRef = textureRef;
		mTextureCoords = coords;
	}
}
