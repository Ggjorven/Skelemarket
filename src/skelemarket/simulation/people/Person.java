package skelemarket.simulation.people;
import skelemarket.core.Renderer;
import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2;

public abstract class Person {
    ///////////////////////////////////////////////////////////
    // Variables
    ///////////////////////////////////////////////////////////
    protected Vec2 mPosition = new Vec2();
    private Vec2 mSize = new Vec2();

    private Texture mTextureRef = null;
    private UV mTextureCoords = new UV();
    protected int mRemainingTicks;

    ///////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////
    public Person(Vec2 position, Vec2 size, Texture textureRef, UV coords) {
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
