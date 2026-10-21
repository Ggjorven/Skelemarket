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
	private Exit mExit = null;
	private final List<Checkout> mCheckouts = new ArrayList<>();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public MapLayer() {
	}

	public void update(Map mapRef) {
		for (Tile tile : mIDToTiles.values()) {
			tile.update(mapRef);
		}
	}

	public void render(Renderer rendererRef) {
		// Render all tiles
		for (java.util.Map.Entry<Integer, Tile> tile : mIDToTiles.entrySet()) {
			tile.getValue().render(rendererRef);
		}

		if (mEntrance != null)
			mEntrance.render(rendererRef);
		if (mExit != null)
			mExit.render(rendererRef);

		for (Checkout checkout : mCheckouts) {
			checkout.render(rendererRef);
		}

		for (java.util.Map.Entry<String, List<Shelf>> shelvesPerCategory : mCategoryToShelves.entrySet()) {
			for (Shelf shelf : shelvesPerCategory.getValue()) {
				shelf.render(rendererRef);
			}
		}
	}

	public void addTile(Tile tile) {
		mIDToTiles.put(tile.getID(), tile);
	}

	public void addShelf(Shelf shelf) {
		addTile(shelf);

		if (!mCategoryToShelves.containsKey(shelf.getCategory())) {
			mCategoryToShelves.put(shelf.getCategory(), new ArrayList<>());
		}

		List<Shelf> shelves = mCategoryToShelves.get(shelf.getCategory());
		shelves.add(shelf);
	}

	public void setEntrance(Entrance entrance) {
		addTile(entrance);
		mEntrance = entrance;
	}

	public void setExit(Exit exit) {
		addTile(exit);
		mExit = exit;
	}

	public void addCheckout(Checkout checkout) {
		addTile(checkout);
		mCheckouts.add(checkout);
	}

	public Tile getTile(int id) {
		return mIDToTiles.get(id);
	}

	public List<Shelf> getShelves(String category) {
		return mCategoryToShelves.getOrDefault(category, List.of());
	}

	public Entrance getEntrance() {
		return mEntrance;
	}

	public Exit getExit() {
		return mExit;
	}

	public List<Checkout> getCheckouts() {
		return List.copyOf(mCheckouts);
	}

	public List<Tile> getTiles() {
		return List.copyOf(mIDToTiles.values());
	}
}
