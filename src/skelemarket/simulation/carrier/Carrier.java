package skelemarket.simulation.carrier;

import skelemarket.simulation.map.ProductSpecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// The class that will track how much is in it and how much can be added
public abstract class Carrier {
    ///////////////////////////////////////////////////////////
    // Variables
    ///////////////////////////////////////////////////////////
    // List with groceries
    private final List<ProductSpecification> mProducts = new ArrayList<>();
    private final int mMaxCapacity; // in units (e.g. apple = 1, bread = 2)

    ///////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////
    // Create carrier and sets a max capacity
    public Carrier(int maxCapacity) {
        mMaxCapacity = maxCapacity;
    }

    // Calculates total weight of all products in the carrier
    public int getTotalWeight(){
        int total = 0;
        // looping trough every product in the carrier (list)
        for (ProductSpecification product : mProducts){
            total = total + product.getWeight();
        }
        return total;
    }

    // Check if new products can be added
    public boolean canAdd(ProductSpecification product){
        return getTotalWeight() + product.getWeight() <= mMaxCapacity;
    }

    // Checks if the list already has the product with a specific name
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

    // looks at the first product in the carrier and will return null if empty
    public String getNextProductName() {
        return mProducts.isEmpty() ? null : mProducts.get(0).getName();
    }

    // Can remove specific products from carrier for the stocker to stock shelves
    public List<ProductSpecification> takeProducts(String productName) {
        List<ProductSpecification> products = new ArrayList<>();
        // first every product will be taken out of the carrier
        for (ProductSpecification product : takeAll()) {
            if (product.getName().equals(productName))
                products.add(product); // than will the chosen product be separated from the rest
            else
                mProducts.add(product); // not the right product and will be added back
        }

        return products; // returns separated products
    }

    // merges all products with the same name with a value
    public Map<String, Integer> getProductCounts() {
        Map<String, Integer> productCounts = new HashMap<>();
        for (ProductSpecification product : mProducts)
            // Count all products with the same name
            productCounts.merge(product.getName(), 1, Integer::sum);

        return Map.copyOf(productCounts); // gives a copy of the list
    }

    // add product to carrier
    public void add(ProductSpecification product) {
        mProducts.add(product);
    }

    // can remove specific product from carrier
    public void remove(ProductSpecification product) {
        mProducts.remove(product);
    }

    // Takes all product from carrier and returns list with products
    public List<ProductSpecification> takeAll() {
        List<ProductSpecification> products = new ArrayList<>(mProducts);
        mProducts.clear();
        return products;
    }
}
