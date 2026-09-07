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

	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	public Supermarket(Renderer rendererRef) {
		mRendererRef = rendererRef;
	}

	public void update(float deltaTime) {
		System.out.println(String.format("deltaTime = %f, FPS = %f", deltaTime, 1 / deltaTime));
	}

	public void render() {
		Texture texture = new Texture("/puzzled-skeleton.png");
		mRendererRef.drawQuad(texture, new Vec2(0.0f, 0.0f), new Vec2(100.0f, 100.0f));
	}
}
