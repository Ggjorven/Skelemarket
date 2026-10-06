package skelemarket.simulation.map;

import skelemarket.core.Renderer;
import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2i;

public class Shelf extends Tile {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Shelf(Vec2i position, Vec2i size, Texture textureRef, UV coords) {
		super(position, size, textureRef, coords);

		// TODO: Add random amount of products for category
	}


	@Override
	public void update() {

	}

	// TODO: Add and take methods
}
