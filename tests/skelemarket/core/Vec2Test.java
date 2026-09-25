package skelemarket.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Vec2Test {
	@Test
	void zeroInit() {
		Vec2 vec = new Vec2();

		assertEquals(vec.getX(), 0.0f, "X should be 0.0f.");
		assertEquals(vec.getY(), 0.0f, "Y should be 0.0f.");
	}

	@Test
	void customInit() {
		Vec2 vec = new Vec2(1.0f, 2.0f);

		assertEquals(vec.getX(), 1.0f, "X should be 1.0f.");
		assertEquals(vec.getY(), 2.0f, "Y should be 2.0f.");
	}

	@Test
	void testSetGet() {
		Vec2 vec = new Vec2();

		vec.setX(3.0f);
		assertEquals(vec.getX(), 3.0f, "X should be 3.0f.");

		vec.setY(4.0f);
		assertEquals(vec.getY(), 4.0f, "Y should be 4.0f.");
	}
}
