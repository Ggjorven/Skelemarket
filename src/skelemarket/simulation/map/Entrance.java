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
	public void update(Map mapRef, PeopleRegister peopleRegisterRef) {
		if (mCountDownTick > 0) {
			mCountDownTick--;
		}

		if (peopleRegisterRef.getCustomerCount() < Config.MAX_CUSTOMERS && isReadyToSpawn()) {
			Optional<PathNode> entranceNode = mapRef.getNodeForTile(getID());

			if (entranceNode.isPresent()) {
				Vec2i nodeLocation = entranceNode.get().getLocation();

				Vec2f spawnPosition = new Vec2f(
						nodeLocation.getX() - Config.CUSTOMER_WIDTH / 2,
						nodeLocation.getY() - Config.CUSTOMER_HEIGHT);

				Customer customer = new Customer(spawnPosition,
						new Vec2f(Config.CUSTOMER_WIDTH, Config.CUSTOMER_HEIGHT), peopleRegisterRef.getCustomerTexture(),
						new UV(Config.CUSTOMER_TEXTURE_X, Config.CUSTOMER_TEXTURE_Y, Config.CUSTOMER_TEXTURE_WIDTH,
								Config.CUSTOMER_TEXTURE_HEIGHT),
						mapRef);

				peopleRegisterRef.addCustomer(customer);
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
