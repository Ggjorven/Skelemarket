package skelemarket.simulation;

import skelemarket.core.Renderer;
import skelemarket.core.Logger;
import skelemarket.simulation.map.Map;
import skelemarket.simulation.map.MapLoader;
import skelemarket.simulation.people.PeopleRegister;

public class Supermarket {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Map mMap = null;
	private PeopleRegister mPeopleRegister = null;

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

		mPeopleRegister = new PeopleRegister(mMap);
	}

	public void update() {
		mMap.update(mPeopleRegister);
		mPeopleRegister.update(mMap);
	}

	public void render(Renderer rendererRef) {
		mMap.render(rendererRef);
		mPeopleRegister.render(rendererRef);
	}
}
