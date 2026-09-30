package skelemarket.simulation.map;

import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2i;

public class Entrance extends Tile {
    ///////////////////////////////////////////////////////////
    // Variables
    ///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////
    public Entrance(Vec2i position, Vec2i size, Texture textureRef, UV coords) {
        super(position, size, textureRef, coords);

    }
    @Override
    public void update() {

    }
}
