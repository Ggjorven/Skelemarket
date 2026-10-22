package skelemarket.simulation.map;

import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2f;
import skelemarket.core.Vec2i;

import skelemarket.simulation.Config;
import skelemarket.simulation.people.Customer;
import skelemarket.simulation.people.PeopleRegister;

import java.util.Optional;

public class Entrance extends Tile {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private int mCountDownTick; // First customer instant spawn

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Entrance(int tileID, Vec2i position, Vec2i size, Texture textureRef, UV coords) {
		super(tileID, position, size, textureRef, coords);
	}

	@Override
	public void update(Map mapRef, PeopleRegister peopleRegister) {
		if (mCountDownTick > 0) {
			mCountDownTick--;
		}

		if (peopleRegister.getCustomerCount() < Config.MAX_CUSTOMERS && isReadyToSpawn()) {
			Optional<PathNode> entranceNode = mapRef.getNodeForTile(getID());

			if (entranceNode.isPresent()) {
				Vec2i nodeLocation = entranceNode.get().getLocation();

				Vec2f spawnPosition = new Vec2f(
						nodeLocation.getX() - Config.CUSTOMER_WIDTH / 2,
						nodeLocation.getY() - Config.CUSTOMER_HEIGHT);

				Customer customer = new Customer(spawnPosition,
						new Vec2f(Config.CUSTOMER_WIDTH, Config.CUSTOMER_HEIGHT), peopleRegister.getCustomerTexture(),
						new UV(0, 0, 7, 16));

				peopleRegister.addCustomer(customer);
			}
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
