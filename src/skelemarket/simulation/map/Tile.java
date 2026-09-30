package skelemarket.simulation.map;

import skelemarket.core.Renderer;
import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2f;

public abstract class Tile {
    ///////////////////////////////////////////////////////////
    // Variables
    ///////////////////////////////////////////////////////////
    protected Vec2f mPosition = new Vec2f();
    private Vec2f mSize = new Vec2f();

    private Texture mTextureRef = null;
    private UV mTextureCoords = new UV();

    ///////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////
    public Tile(Vec2f position, Vec2f size, Texture textureRef, UV coords) {
        mPosition = position;
        mSize = size;

        mTextureRef = textureRef;
        mTextureCoords = coords;
    }

    public abstract void update();

    public void render(Renderer rendererRef) {
        rendererRef.drawQuad(mTextureRef, mPosition, mSize, mTextureCoords);
    }
}
