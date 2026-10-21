package skelemarket.simulation.map;

import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2f;
import skelemarket.core.Vec2i;

import skelemarket.simulation.Config;
import skelemarket.simulation.Supermarket;
import skelemarket.simulation.people.Customer;

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
	public void update(Map mapRef) {
		countDown();


		// TODO: Use some kind of UpdateRegister or Employee/CustomerRegister which
		// is passed down through the update function instead of having to call a
		// singleton function (Supermarket::get()) and retrieve information through that
		Supermarket supermarket = Supermarket.get();

		if (supermarket.getCustomerCount() < Config.MAX_CUSTOMERS && isReadyToSpawn()) {
			Optional<PathNode> entranceNode = mapRef.getNodeForTile(getID());

			if (entranceNode.isPresent()) {
				Vec2i nodeLocation = entranceNode.get().getLocation();

				Vec2f spawnPosition = new Vec2f(
						nodeLocation.getX() - Config.CUSTOMER_WIDTH / 2,
						nodeLocation.getY() - Config.CUSTOMER_HEIGHT);

				Customer customer = new Customer(spawnPosition,
						new Vec2f(Config.CUSTOMER_WIDTH, Config.CUSTOMER_HEIGHT), supermarket.getCustomerTexture(),
						new UV(0, 0, 7, 16));

				supermarket.addCustomer(customer);
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

	void countDown() {
		if (mCountDownTick > 0) {
			mCountDownTick--;
		}
	}
}
