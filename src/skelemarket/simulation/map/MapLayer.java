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

    public void update() {
        for (Tile tile : mTiles) {
            tile.update();
        }
    }

    public void addTile(Tile tile) {
        mTiles.add(tile);
    }
}
