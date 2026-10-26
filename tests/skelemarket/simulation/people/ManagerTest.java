package skelemarket.simulation.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import skelemarket.core.Vec2i;
import skelemarket.simulation.map.ProductSpecification;
import skelemarket.simulation.map.Shelf;

class ManagerTest {
	@Test
	void queuesEachLowStockShelfOnlyOnceUntilTheTaskIsCompleted() {
		ProductSpecification apple = new ProductSpecification("APPLE", 1, "FRUIT", null, null);
		Shelf shelf = new Shelf(1, new Vec2i(), new Vec2i(), null, null, "FRUIT", false,
				Map.of("APPLE", 4), Map.of("FRUIT", Map.of("APPLE", apple)));

		while (shelf.getProductCount("APPLE") > 1)
			shelf.takeProduct("APPLE");

		Manager manager = new Manager();
		manager.notifyLowStock(shelf);
		manager.notifyLowStock(shelf);

		assertEquals(1, manager.getPendingRestockCount());
		assertSame(shelf, manager.claimNextRestockRequest().orElseThrow());
		assertTrue(manager.claimNextRestockRequest().isEmpty());

		manager.completeRestock(shelf);
		manager.notifyLowStock(shelf);
		assertEquals(1, manager.getPendingRestockCount());
	}
}
