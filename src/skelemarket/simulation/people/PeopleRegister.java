package skelemarket.simulation.people;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import skelemarket.core.Renderer;
import skelemarket.core.Texture;
import skelemarket.simulation.Config;
import skelemarket.simulation.map.Map;

public class PeopleRegister {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private final java.util.Map<String, Texture> mTextures = new HashMap<>();

	private final List<Customer> mCustomers = new ArrayList<>();
	// TODO: Add a list of employees once employee spawning is implemented.

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public PeopleRegister(Map mapRef) {
		mTextures.put(Config.CUSTOMER_TEXTURE, new Texture(Config.CUSTOMER_TEXTURE));

		// TODO: Spawn manager

		// TODO: Spawn cashier

		// TODO: Spawn 3 random stockers
	}

	public void update() {
		for (Customer customer : mCustomers) {
			customer.update();
		}

		mCustomers.removeIf(Customer::isFinished);

		// TODO: Update all employees.
	}

	public void render(Renderer rendererRef) {
		for (Customer customer : mCustomers) {
			customer.render(rendererRef);
		}

		// TODO: Render all employees.
	}

	public int getCustomerCount() {
		return mCustomers.size();
	}

	public void addCustomer(Customer customer) {
		mCustomers.add(customer);
	}

	public Texture getCustomerTexture() {
		return mTextures.get(Config.CUSTOMER_TEXTURE);
	}
}
