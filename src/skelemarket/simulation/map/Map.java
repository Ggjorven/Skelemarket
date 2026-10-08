package skelemarket.simulation.map;

import java.util.List;

import skelemarket.core.Renderer;

public class Map {
    ///////////////////////////////////////////////////////////
    // Variables
    ///////////////////////////////////////////////////////////
    private List<MapLayer> mLayers = null;
    private Entrance mEntrance = null;

    ///////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////
    public Map(List<MapLayer> layers, Entrance entrance) {

        mLayers = layers;
        mEntrance = entrance;
    }

    public void render(Renderer rendererRef) {
        for (MapLayer layerRef : mLayers) {
            layerRef.render(rendererRef);
        }
    }

    public void update() {
        for (MapLayer layerRef : mLayers) {
            layerRef.update();
        }
    }

    public Entrance getEntrance(){
        return mEntrance;
    }
}
