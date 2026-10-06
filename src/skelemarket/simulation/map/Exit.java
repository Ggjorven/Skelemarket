package skelemarket.simulation.map;

import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2i;

public class Exit extends Tile{
    ///////////////////////////////////////////////////////////
    // Variables
    ///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////
    public Exit(Vec2i position, Vec2i size, Texture textureRef, UV coords) {
        super(position, size, textureRef, coords);

    }

    @Override
    public void update() {
        // Intentionally empty: Exit has no behaviour of its own
    }
}
