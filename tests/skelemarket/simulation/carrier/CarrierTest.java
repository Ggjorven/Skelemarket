package skelemarket.simulation.carrier;

import org.junit.jupiter.api.Test;
import skelemarket.simulation.Config;
import skelemarket.simulation.map.ProductSpecification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CarrierTest {

    @Test
    void canAddWhenThereIsRoom() {
        Basket basket = new Basket();
        ProductSpecification apple = new ProductSpecification("APPLE", 1, "FRUIT", null, null);

        assertTrue(basket.canAdd(apple), "An empty basket should accept a product of weight 1.");
    }

    @Test
    void cannotAddWhenFull() {
        Basket basket = new Basket();
        ProductSpecification pumpkin = new ProductSpecification("PUMPKIN", Config.BASKET_CAPACITY, "VEGETABLES", null, null);
        ProductSpecification apple = new ProductSpecification("APPLE", 1, "FRUIT", null, null);
        basket.add(pumpkin);

        assertFalse(basket.canAdd(apple), "A full basket should not accept another product.");
    }

    @Test
    void totalWeightAddsUp() {
        Basket basket = new Basket();
        ProductSpecification apple = new ProductSpecification("APPLE", 1, "FRUIT", null, null);
        basket.add(apple);
        basket.add(apple);

        assertEquals(2, basket.getTotalWeight(), "Two apples of weight 1 should weigh 2 in total");
    }

    @Test
    void takeAllEmptiesCarrier() {
        Basket basket = new Basket();
        ProductSpecification apple = new ProductSpecification("APPLE", 1, "FRUIT", null, null);
        basket.add(apple);
        basket.add(apple);

        List<ProductSpecification> taken = basket.takeAll();

        assertEquals(2, taken.size(), "Size 2 expected when added two apples to the basket");
        assertEquals(0, basket.getTotalWeight(), "Basket should be empty after takeAll.");
    }

    @Test
    void canAddProductThatFillsBasketExactly(){
        Basket basket = new Basket();
        ProductSpecification heavy = new ProductSpecification("PUMPKIN", Config.BASKET_CAPACITY, "FRUIT", null, null);

        assertTrue(basket.canAdd(heavy), "A product that exactly fills the basket should fit.");
    }
}
