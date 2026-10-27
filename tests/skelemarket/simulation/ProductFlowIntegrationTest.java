package skelemarket.simulation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import skelemarket.core.Vec2f;
import skelemarket.core.Vec2i;
import skelemarket.simulation.carrier.Basket;
import skelemarket.simulation.map.Checkout;
import skelemarket.simulation.map.ProductSpecification;
import skelemarket.simulation.map.Shelf;
import skelemarket.simulation.people.Customer;

public class ProductFlowIntegrationTest {
    @Test
    void productMovesFromMagazineToCheckout() {
        ProductSpecification apple = new ProductSpecification("APPLE", 1, "FRUIT", null, null);
        java.util.Map<String, java.util.Map<String, ProductSpecification>> products =
                java.util.Map.of("FRUIT", java.util.Map.of("APPLE", apple));

        Shelf magazine = new Shelf(0, new Vec2i(), new Vec2i(), null, null, "FRUIT", true,
                java.util.Map.of("APPLE", 10), products);
        Shelf storeShelf = new Shelf(1, new Vec2i(), new Vec2i(), null, null, "FRUIT", false,
                java.util.Map.of("APPLE", 2), products);

        Basket basket = new Basket();
        Customer customer = new Customer(new Vec2f(), new Vec2f(), null, null, null);
        Checkout checkout = new Checkout(2, new Vec2i(), new Vec2i(), null, null, true);

        magazine.addProduct("APPLE", 10);
        while (storeShelf.takeProduct("APPLE")) {
        }

        // Stocker refills the store shelf from the magazine
        magazine.takeProduct("APPLE");
        magazine.takeProduct("APPLE");
        storeShelf.addProduct("APPLE", 2);

        assertEquals(8, magazine.getProductCount("APPLE"), "Magazine should have 8 apples left after refilling.");
        assertEquals(2, storeShelf.getProductCount("APPLE"), "Store shelf should hold 2 apples after refilling.");

        // Customer takes both apples into the basket
        storeShelf.takeProduct("APPLE");
        basket.add(apple);
        storeShelf.takeProduct("APPLE");
        basket.add(apple);

        assertEquals(0, storeShelf.getProductCount("APPLE"), "Store shelf should be empty after the customer took both apples.");
        assertEquals(2, basket.getTotalWeight(), "Basket should weigh 2 after taking two apples.");

        // Customer joins the checkout
        checkout.join(customer);

        assertTrue(checkout.isFirst(customer), "The customer should be first in an empty queue");

        // Customer pays
        List<ProductSpecification> paid = basket.takeAll();

        assertEquals(2, paid.size(), "The checkout should receive both apples.");
        assertEquals(0, basket.getTotalWeight(), "Basket should be empty after paying.");
    }
}