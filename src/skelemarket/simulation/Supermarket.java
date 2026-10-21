package skelemarket.simulation;

import skelemarket.core.*;
import skelemarket.simulation.map.Entrance;
import skelemarket.simulation.map.Map;
import skelemarket.simulation.map.MapLoader;
import skelemarket.simulation.people.Customer;

import java.util.ArrayList;
import java.util.List;

public class Supermarket {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Map mMap = null;
	private List<Customer> mCustomers = new ArrayList<>();
	private Texture mCustomerTexture = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Supermarket() throws Exception {
		MapLoader loader = new MapLoader();
		try {
			mMap = loader.loadFromPath(Config.MAP_FILE);
		} catch (Exception ex) {
			Logger.error("Failed to load map due to error: %s.", ex.toString());
			throw new Exception(String.format("Failed to load map due to error: %s.", ex.toString()));
		}

		mCustomerTexture = new Texture(Config.CUSTOMER_TEXTURE);

		// TODO: Spawn manager

		// TODO: Spawn cashier

		// TODO: Spawn 3 random stockers
	}

	public void update() {
		mMap.update();
		Entrance entrance = mMap.getEntrance();
		if(entrance != null && mCustomers.size() < Config.MAX_CUSTOMERS && entrance.isReadyToSpawn()) {
			Vec2i entrancePosition = entrance.getPosition();
			Vec2f spawnPosition = new Vec2f(entrancePosition.getX(), entrancePosition.getY());
			Customer customer = new Customer(spawnPosition, new Vec2f(Config.CUSTOMER_WIDTH, Config.CUSTOMER_HEIGHT), mCustomerTexture, new UV(0,0,7,16));

			mCustomers.add(customer);

		}

		for (Customer customer : mCustomers){
			customer.update();
		}
		// TODO: Update all employees
		// TODO: delete customer when isFinished() using removeIf
	}

	public void render(Renderer rendererRef) {
		mMap.render(rendererRef);

		for (Customer customer : mCustomers){
			customer.render(rendererRef);
		}

		// TODO: Render employees
	}
}
