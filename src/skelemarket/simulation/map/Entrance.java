package skelemarket.simulation.map;

import skelemarket.simulation.Config;
import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2i;

public class Entrance extends Tile {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private int mCountDownTick; // First customer instant spawn

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Entrance(Vec2i position, Vec2i size, Texture textureRef, UV coords) {
		super(position, size, textureRef, coords);

	}

	@Override
	public void update() {
		if (mCountDownTick > 0) {
			mCountDownTick--;
		}
	}

	public boolean isReadyToSpawn() {
		if (mCountDownTick == 0) {
			mCountDownTick = Config.CUSTOMER_SPAWN_INTERVAL_TICKS;
			return true;
		} else {
			return false;
		}
	}
}
