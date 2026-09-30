package skelemarket.simulation.map;

import skelemarket.core.*;

public abstract class Tile {
    ///////////////////////////////////////////////////////////
    // Variables
    ///////////////////////////////////////////////////////////
    protected Vec2i mPosition = new Vec2i();
    private Vec2i mSize = new Vec2i();

    private Texture mTextureRef = null;
    private UV mTextureCoords = new UV();

    ///////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////
    public Tile(Vec2i position, Vec2i size, Texture textureRef, UV coords) {
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
