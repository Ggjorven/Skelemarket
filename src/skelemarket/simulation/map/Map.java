package skelemarket.simulation.map;

import java.util.List;

import skelemarket.core.Renderer;

public class Map {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private List<MapLayer> mLayers = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Map(List<MapLayer> layers) {
		mLayers = layers;
	}

	public void render(Renderer rendererRef) {
		for (MapLayer layerRef : mLayers) {
			layerRef.render(rendererRef);
		}
	}
}
