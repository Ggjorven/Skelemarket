package skelemarket.simulation.carrier;

public abstract class Carrier {
    ///////////////////////////////////////////////////////////
    // Variables
    ///////////////////////////////////////////////////////////
    //TODO: list with the products in the carrier
    private final int mMaxCapacity; // in units (e.g. apple = 1, bread = 2)

    ///////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////
    public Carrier(int maxCapacity) {
        mMaxCapacity = maxCapacity;
    }

    //TODO: add a product
    //TODO: check if a product still fits
    //TODO: remove a product
    //TODO: remove everything (at the checkout)
}
