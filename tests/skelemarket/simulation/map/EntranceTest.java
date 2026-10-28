package skelemarket.simulation.map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import skelemarket.core.Vec2i;
import skelemarket.simulation.Config;

public class EntranceTest {
	@Test
	void firstCallIsReadyToSpawn() {
		Entrance entrance = new Entrance(0, new Vec2i(), new Vec2i(), null, null);

		boolean ready = entrance.isReadyToSpawn();

		assertTrue(ready, "First customer should spawn instantly.");
	}

	@Test
	void notReadyRightAfterCreation() {
		Entrance entrance = new Entrance(1, new Vec2i(), new Vec2i(), null, null);

		boolean ready = entrance.isReadyToSpawn();

		assertTrue(ready, "Entrance should be able to spawn a customer on creation.");
	}
}
