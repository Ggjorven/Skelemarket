package skelemarket.simulation;

import skelemarket.core.Logger;
import skelemarket.core.Renderer;
import skelemarket.simulation.map.Map;
import skelemarket.simulation.map.MapLoader;

public class Supermarket {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Map mMap = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Supermarket() {
		// Load map
		MapLoader loader = new MapLoader();
		try {
			mMap = loader.loadFromPath(Config.MAP_FILE);
		} catch (Exception ex) {
			Logger.error("Failed to load map due to error: %s.", ex.toString());
			return;
		}

		// Spawn customers at the door

		// Spawn manager

		// Spawn cashier

		// Spawn 3 random stockers
	}

	public void update() {
		// Update all customers

		// Update all employees
	}

	public void render(Renderer rendererRef) {
		// Render map
		mMap.render(rendererRef);

		// Render customers

		// Render employees
	}
}
