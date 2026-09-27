package skelemarket.simulation.people;

import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2;

public class Stocker extends Employee{
    ///////////////////////////////////////////////////////////
    // Variables
    ///////////////////////////////////////////////////////////
    private State mCurrentState = State.IDLE;
    private Vec2 mWalkTarget = new Vec2();

    ///////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////
    public Stocker(Vec2 position, Vec2 size, Texture textureRef, UV coords) {
        super(position, size, textureRef, coords);

    }

    @Override
    public void update() {
        // TODO: If no task: wait until the supermarket assigns a refill task
        // TODO: Walk to the storage
        // TODO: Take the products for the task out of the storage
        // TODO: Walk to the shelf of the first product
        // TODO: Restock the shelf with the right products
        // TODO: If the shelf is full and products are left: walk back to the storage and put them back
        // TODO: Walk to the next shelf, or back to the storage for more products
        // TODO: If the task is done: become idle again
    }

    private enum State{
        IDLE,
        WALK,
        TAKE,
        RESTOCK,
        PUT_BACK
    }
}
