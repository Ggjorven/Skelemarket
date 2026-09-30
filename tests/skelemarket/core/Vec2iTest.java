package skelemarket.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Vec2iTest {
	@Test
	void zeroInit() {
		Vec2i vec = new Vec2i();

		assertEquals(vec.getX(), 0, "X should be 0.");
		assertEquals(vec.getY(), 0, "Y should be 0.");
	}

	@Test
	void customInit() {
		Vec2i vec = new Vec2i(1, 2);

		assertEquals(vec.getX(), 1, "X should be 1.");
		assertEquals(vec.getY(), 2, "Y should be 2.");
	}

	@Test
	void testSetGet() {
		Vec2i vec = new Vec2i();

		vec.setX(3);
		assertEquals(vec.getX(), 3, "X should be 3.");

		vec.setY(4);
		assertEquals(vec.getY(), 4, "Y should be 4.");
	}
}
