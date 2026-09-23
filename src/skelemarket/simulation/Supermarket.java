package skelemarket.simulation;

import skelemarket.core.Renderer;
import skelemarket.core.Texture;
import skelemarket.core.Vec2;
import skelemarket.core.UV;

public class Supermarket {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private Renderer mRendererRef = null;	

	private Texture texture = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Supermarket(Renderer rendererRef) {
		mRendererRef = rendererRef;

		texture = new Texture("/textures/puzzled-skeleton.png");
	}

	public void update() {
		// TODO: ...
	}

	public void render() {
		mRendererRef.drawQuad(texture, new Vec2(0.0f, 0.0f), new Vec2(100.0f, 100.0f));
	}
}
