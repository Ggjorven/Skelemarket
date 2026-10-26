package skelemarket.simulation.carrier;

import skelemarket.simulation.map.ProductSpecification;

import java.util.ArrayList;
import java.util.List;

public abstract class Carrier {
    ///////////////////////////////////////////////////////////
    // Variables
    ///////////////////////////////////////////////////////////
    private final List<ProductSpecification> mProducts = new ArrayList<>();
    private final int mMaxCapacity; // in units (e.g. apple = 1, bread = 2)

    ///////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////
    public Carrier(int maxCapacity) {
        mMaxCapacity = maxCapacity;
    }

    public int getTotalWeight(){
        int total = 0;
        for (ProductSpecification product : mProducts){
            total = total + product.getWeight();
        }
        return total;
    }

    public boolean canAdd(ProductSpecification product){
        return getTotalWeight() + product.getWeight() <= mMaxCapacity;
    }

    public void add(ProductSpecification product) {
        mProducts.add(product);
    }

    public void remove(ProductSpecification product) {
        mProducts.remove(product);
    }

    public List<ProductSpecification> takeAll() {
        List<ProductSpecification> products = new ArrayList<>(mProducts);
        mProducts.clear();
        return products;
    }
}
