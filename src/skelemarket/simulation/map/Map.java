package skelemarket.simulation.map;

import java.util.List;

import skelemarket.core.Colour;
import skelemarket.core.Renderer;
import skelemarket.core.Vec2i;

public class Map {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private List<MapLayer> mLayers = null;
	private Colour mBackgroundColour = new Colour();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Map(List<MapLayer> layers, Colour backgroundColour) {
		mLayers = layers;
		mBackgroundColour = backgroundColour;
	}

	public void render(Renderer rendererRef) {
		rendererRef.drawQuad(mBackgroundColour, new Vec2i(0, 0),
				new Vec2i(skelemarket.core.Config.WIDTH, skelemarket.core.Config.HEIGHT));

		for (MapLayer layerRef : mLayers) {
			layerRef.render(rendererRef);
		}
	}
}
