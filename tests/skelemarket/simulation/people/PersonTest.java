package skelemarket.simulation.people;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import skelemarket.core.Vec2f;

public class PersonTest {
    @Test
    void arrivesAfterExactNumberOfSteps() {
        Customer customer = new Customer(new Vec2f(0, 0), new Vec2f(), null, null, null);
        customer.setDestination(new Vec2f(10, 0));

        for (int i = 0; i < 9; i++) {
            customer.stepTowardsDestination();
        }
        assertFalse(customer.hasArrived(), "Should not have arrived after 9 steps.");

        customer.stepTowardsDestination();
        assertTrue(customer.hasArrived(), "Should have arrived after 10 steps.");
    }

    @Test
    void diagonalStepHasSameLength() {
        Customer customer = new Customer(new Vec2f(0, 0), new Vec2f(), null, null, null);
        customer.setDestination(new Vec2f(30, 40));

        customer.stepTowardsDestination();

        assertEquals(0.6f, customer.mPosition.getX(), 0.0001f, "X should move 0.6 for a diagonal step.");
        assertEquals(0.8f, customer.mPosition.getY(), 0.0001f, "Y should move 0.8 for a diagonal step.");
    }

    @Test
    void doesNotMoveWithoutDestination(){
        Customer customer = new Customer(new Vec2f(5,5), new Vec2f(), null, null, null);

        customer.stepTowardsDestination();

        assertEquals(5f, customer.mPosition.getX(), 0.0001f, "X should not change without a destination.");
        assertEquals(5f, customer.mPosition.getY(), 0.0001f, "Y should not change without a destination.");
    }
}
