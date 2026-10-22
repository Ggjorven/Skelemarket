package skelemarket.simulation.people;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import skelemarket.core.Renderer;
import skelemarket.core.Texture;
import skelemarket.simulation.Config;

public class PeopleRegister {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private final Map<String, Texture> mTextures = new HashMap<>();

	private final List<Customer> mCustomers = new ArrayList<>();
	// TODO: Add a list of employees once employee spawning is implemented.

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public PeopleRegister() {
		mTextures.put(Config.CUSTOMER_TEXTURE, new Texture(Config.CUSTOMER_TEXTURE));
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
