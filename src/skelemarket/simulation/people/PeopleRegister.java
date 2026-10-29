package skelemarket.simulation.people;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import skelemarket.core.Renderer;
import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2f;
import skelemarket.core.Vec2i;
import skelemarket.simulation.Config;
import skelemarket.simulation.map.Map;
import skelemarket.simulation.map.PathNode;
import skelemarket.simulation.map.Shelf;
import skelemarket.simulation.map.Tile;

//This class will track all persons in the supermarket and arranges the creation, update of all persons
public class PeopleRegister {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	// Place to store images of customers and employees
	private final java.util.Map<String, Texture> mTextures = new HashMap<>();

	// List with all activities for customers and Stockers
	private final List<Customer> mCustomers = new ArrayList<>();
	private final List<Stocker> mStockers = new ArrayList<>();
	// For one manager
	private final Manager mManager = new Manager();
	// Random for example the start where a stocker will start filling
	private final Random mRandom = new Random();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	// Load the appearance and put stockers on there position
	public PeopleRegister(Map mapRef) {
		// Load customer and stocker image
		mTextures.put(Config.CUSTOMER_TEXTURE, new Texture(Config.CUSTOMER_TEXTURE));
		mTextures.put(Config.STOCKER_TEXTURE, new Texture(Config.STOCKER_TEXTURE));

		// find all shelves with Stockroom property
		List<Shelf> storageShelves = new ArrayList<>();
		for (skelemarket.simulation.map.MapLayer layer : mapRef.getLayers()) {
			for (Tile tile : layer.getTiles()) {
				// if the tile is a shelf and with the property Stockroom and persons can walk to it
				if (tile instanceof Shelf shelf && shelf.isMagazine() && mapRef.getNodeForShelf(shelf).isPresent())
					storageShelves.add(shelf); // add to list with Stockroom shelves
			}
		}

		// load the amount of stockers that is set in config
		for (int i = 0; i < Config.STOCKER_COUNT; i++) {
			Vec2f spawnPosition = new Vec2f(); // start position is 0,0, for background
			// if stockroom shelf has been found put stocker there
			if (!storageShelves.isEmpty()) {
				// Choose a random shelve in the Stockroom
				Shelf storageShelf = storageShelves.get(mRandom.nextInt(storageShelves.size()));
				PathNode storageNode = mapRef.getNodeForShelf(storageShelf).orElse(null);
				if (storageNode != null) {
					Vec2i location = storageNode.getLocation();
					// Calculates the position so the stocker will spawn next to the shelf
					spawnPosition = new Vec2f(location.getX() - Config.STOCKER_WIDTH / 2.0f,
							location.getY() - Config.STOCKER_HEIGHT);
				}
			}

			mStockers.add(new Stocker(spawnPosition,
					new Vec2f(Config.STOCKER_WIDTH, Config.STOCKER_HEIGHT), mTextures.get(Config.STOCKER_TEXTURE),
					new UV(Config.STOCKER_TEXTURE_X, Config.STOCKER_TEXTURE_Y, Config.STOCKER_TEXTURE_WIDTH,
							Config.STOCKER_TEXTURE_HEIGHT)));
		}
	}

	// add stocker to the list with position size and texture
	public void update(Map mapRef) {
		// Every customer get there action
		for (Customer customer : mCustomers) {
			customer.update(mapRef, this);
		}

		// is used to clean up old customers
		mCustomers.removeIf(Customer::isFinished);

		// Every stocker get there action
		for (Stocker stocker : mStockers)
			stocker.update(mapRef, this);
	}

	// draws all known customers and stockers on the window with renderer
	public void render(Renderer rendererRef) {
		// draw all customers
		for (Customer customer : mCustomers) {
			customer.render(rendererRef);
		}

		// draw all stockers
		for (Stocker stocker : mStockers)
			stocker.render(rendererRef);
	}

	//
	public int getCustomerCount() {
		return mCustomers.size();
	}

	public void addCustomer(Customer customer) {
		mCustomers.add(customer);
	}

	public Texture getCustomerTexture() {
		return mTextures.get(Config.CUSTOMER_TEXTURE);
	}

	public void notifyLowStock(Shelf shelf) {
		mManager.notifyLowStock(shelf);
	}

	public Manager getManager() {
		return mManager;
	}
}
