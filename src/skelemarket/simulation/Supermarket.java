package skelemarket.simulation;

import skelemarket.core.Renderer;
import skelemarket.core.Texture;
import skelemarket.core.Logger;
import skelemarket.simulation.map.Map;
import skelemarket.simulation.map.MapLoader;
import skelemarket.simulation.people.Customer;

import java.util.ArrayList;
import java.util.List;

public class Supermarket {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	// TODO: Remove singleton instance
	private static Supermarket mThis = null;

	private Map mMap = null;

	// TODO: Remove customer texture and customers and replace
	// with some kind of storage class which handles employees/customers
	private Texture mCustomerTexture = null;
	private List<Customer> mCustomers = new ArrayList<>();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Supermarket() throws Exception {
		mThis = this;

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

		for (Customer customer : mCustomers) {
			customer.update();
		}

		// TODO: Update all employees
		mCustomers.removeIf(Customer::isFinished);
	}

	public void render(Renderer rendererRef) {
		mMap.render(rendererRef);

		for (Customer customer : mCustomers) {
			customer.render(rendererRef);
		}

		// TODO: Render employees
	}

	// TODO: Remove singleton and pass through necessary constructs through update
	public static Supermarket get() {
		return mThis;
	}

	// TODO: Remove once singleton is gone
	public int getCustomerCount() {
		return mCustomers.size();
	}

	// TODO: Remove once singleton is gone
	public void addCustomer(Customer customer) {
		mCustomers.add(customer);
	}

	// TODO: Remove once singleton is gone
	public Texture getCustomerTexture() {
		return mCustomerTexture;
	}
}
