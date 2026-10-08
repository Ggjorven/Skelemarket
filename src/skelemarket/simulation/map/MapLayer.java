package skelemarket.simulation.map;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import skelemarket.core.Renderer;

public class MapLayer {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private java.util.Map<Integer, Tile> mIDToTiles = new HashMap<>();
	private java.util.Map<String, List<Shelf>> mCategoryToShelves = new HashMap<>();

	private Entrance mEntrance = null;
	// TODO: Exit

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public MapLayer() {
	}

	public void render(Renderer rendererRef) {
		// Render all tiles
		for (java.util.Map.Entry<Integer, Tile> tile : mIDToTiles.entrySet()) {
			tile.getValue().render(rendererRef);
		}
	}

	public void addTile(Tile tile) {
		mIDToTiles.put(tile.getID(), tile);
	}

	public void addShelf(Shelf shelf) {
		mIDToTiles.put(shelf.getID(), shelf);

		if (!mCategoryToShelves.containsKey(shelf.getCategory())) {
			mCategoryToShelves.put(shelf.getCategory(), new ArrayList<>());
		}

		List<Shelf> shelves = mCategoryToShelves.get(shelf.getCategory());
		shelves.add(shelf);
	}
}
