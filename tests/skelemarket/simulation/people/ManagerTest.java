package skelemarket.simulation.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import skelemarket.core.Colour;
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
		skelemarket.simulation.map.Map map = new skelemarket.simulation.map.Map(List.of(), new Colour(),
				Map.of("FRUIT", Map.of("APPLE", apple)), Map.of());
		manager.notifyLowStock(shelf);
		manager.notifyLowStock(shelf);

		assertEquals(1, manager.getPendingRestockCount());
		assertSame(shelf, manager.claimNextRestockRequest(map).orElseThrow());
		assertTrue(manager.claimNextRestockRequest(map).isEmpty());

		manager.completeRestock(shelf);
		manager.notifyLowStock(shelf);
		assertEquals(1, manager.getPendingRestockCount());
	}
}
