package skelemarket.simulation.map;

import java.util.List;
import java.util.ArrayList;

import skelemarket.core.Renderer;

public class MapLayer {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private List<Tile> mTiles = new ArrayList<>();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public MapLayer() {
	}

	public void render(Renderer rendererRef) {
		// Render all objects
		for (Tile tile : mTiles) {
			tile.render(rendererRef);
		}
	}

	// TODO: Remove the seperate add functions if no custom functionality is
	// required

	public void addTile(Tile tile) {
		mTiles.add(tile);
	}

	public void addShelf(Shelf shelf) {
		mTiles.add(shelf);
	}
}
