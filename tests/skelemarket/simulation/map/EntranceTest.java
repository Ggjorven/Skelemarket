package skelemarket.simulation.map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import skelemarket.core.Vec2i;
import skelemarket.simulation.Config;

public class EntranceTest {
	@Test
	void firstCallIsReadyToSpawn() {
		Entrance entrance = new Entrance(new Vec2i(), new Vec2i(), null, null);

		boolean ready = entrance.isReadyToSpawn();

		assertTrue(ready, "First customer should spawn instantly.");
	}

	@Test
	void notReadyRightAfterSpawn() {
		Entrance entrance = new Entrance(new Vec2i(), new Vec2i(), null, null);
		entrance.isReadyToSpawn();

		boolean ready = entrance.isReadyToSpawn();

		assertFalse(ready, "No new customer should spawn right after the previous one.");
	}

	@Test
	void readyAgainAfterSpawnInterval() {
		Entrance entrance = new Entrance(new Vec2i(), new Vec2i(), null, null);
		entrance.isReadyToSpawn();

		for (int i = 0; i < Config.SPAWN_INTERVAL_TICKS; i++) {
			entrance.update();
		}
		boolean ready = entrance.isReadyToSpawn();

		assertTrue(ready, "Customer is ready to be spawned in");

	}

	@Test
	void notReadyOneTickBeforeInterval() {
		Entrance entrance = new Entrance(new Vec2i(), new Vec2i(), null, null);
		entrance.isReadyToSpawn();

		for (int i = 0; i < Config.SPAWN_INTERVAL_TICKS - 1; i++) {
			entrance.update();
		}
		boolean ready = entrance.isReadyToSpawn();

		assertFalse(ready, "Customer should not spawn one tick before the interval.");
	}
}
