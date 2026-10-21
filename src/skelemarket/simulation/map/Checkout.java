package skelemarket.simulation.map;

import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2i;

public class Checkout extends Tile {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private boolean mIsSelfService = false;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Checkout(int tileID, Vec2i position, Vec2i size, Texture textureRef, UV coords, boolean isSelfService) {
		super(tileID, position, size, textureRef, coords);
		mIsSelfService = isSelfService;
	}

	public boolean isSelfService() {
		return mIsSelfService;
	}
}
