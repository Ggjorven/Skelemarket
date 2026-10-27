package skelemarket.simulation.carrier;

import skelemarket.simulation.map.ProductSpecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public boolean hasProduct(String productName) {
        for (ProductSpecification product : mProducts) {
            if (product.getName().equals(productName))
                return true;
        }

        return false;
    }

    public boolean hasProducts() {
        return !mProducts.isEmpty();
    }

    public String getNextProductName() {
        return mProducts.isEmpty() ? null : mProducts.get(0).getName();
    }

    public List<ProductSpecification> takeProducts(String productName) {
        List<ProductSpecification> products = new ArrayList<>();
        for (ProductSpecification product : takeAll()) {
            if (product.getName().equals(productName))
                products.add(product);
            else
                mProducts.add(product);
        }

        return products;
    }

    public Map<String, Integer> getProductCounts() {
        Map<String, Integer> productCounts = new HashMap<>();
        for (ProductSpecification product : mProducts)
            productCounts.merge(product.getName(), 1, Integer::sum);

        return Map.copyOf(productCounts);
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
