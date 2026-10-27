package skelemarket.simulation.map;

import org.junit.jupiter.api.Test;
import skelemarket.core.Vec2f;
import skelemarket.core.Vec2i;
import skelemarket.simulation.people.Customer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckoutTest {
    @Test
    void customerIsFirstAfterJoiningEmptyQueue(){
        Checkout checkout = new Checkout(0, new Vec2i(), new Vec2i(), null, null, true);
        Customer customer = new Customer(new Vec2f(), new Vec2f(), null, null, null);

        checkout.join(customer);

        assertTrue(checkout.isFirst(customer), "The only customer in line should be first.");
    }

    @Test
    void secondCustomerIsNotFirst(){
        Checkout checkout = new Checkout(0, new Vec2i(), new Vec2i(), null, null, true);
        Customer customer1 = new Customer(new Vec2f(), new Vec2f(), null, null, null);
        Customer customer2 = new Customer(new Vec2f(), new Vec2f(), null, null, null);
        checkout.join(customer1);
        checkout.join(customer2);

        assertFalse(checkout.isFirst(customer2), "The second customer in line should not be first.");
    }

    @Test
    void secondCustomerIsFirstAfterFirstLeaves(){
        Checkout checkout = new Checkout(0, new Vec2i(), new Vec2i(), null, null, true);
        Customer customer1 = new Customer(new Vec2f(), new Vec2f(), null, null, null);
        Customer customer2 = new Customer(new Vec2f(), new Vec2f(), null, null, null);
        checkout.join(customer1);
        checkout.join(customer2);
        checkout.leave(customer1);

        assertTrue(checkout.isFirst(customer2), "The second customer in line should be first after the first customer leaves.");
    }

    @Test
    void emptyQueueHasNoFirst(){
        Checkout checkout = new Checkout(0, new Vec2i(), new Vec2i(), null, null, true);
        Customer customer = new Customer(new Vec2f(), new Vec2f(), null, null, null);

        assertFalse(checkout.isFirst(customer), "The second customer in line should be first after the first customer leaves.");
    }
}
