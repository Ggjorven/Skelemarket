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

	// TODO: Remove temporary
	private float time = 0.0f;
	private int frames = 0;
	private float fps = 0;

	private Texture texture = null;

	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	public Supermarket(Renderer rendererRef) {
		mRendererRef = rendererRef;

		texture = new Texture("/textures/puzzled-skeleton.png");
	}

	public void update(float deltaTime) {
		frames++;
		time += deltaTime;

		if (time >= 1.0f)
		{
			fps = frames / time;
			frames = 0;
			time = 0.0f;
		}

		System.out.println(String.format("deltaTime = %f, FPS = %f", deltaTime, fps));
	}

	public void render() {
		mRendererRef.drawQuad(texture, new Vec2(0.0f, 0.0f), new Vec2(100.0f, 100.0f));
	}
}
