package skelemarket.simulation.map;

import java.awt.List;
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

	public void addTile(Shelf shelf) {
		mTiles.add(shelf);
	}
}
