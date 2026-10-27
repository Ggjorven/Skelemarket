package skelemarket.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UVTest {
	@Test
	void zeroInit() {
		UV uv = new UV();

		assertEquals(uv.getX(), 0, "X should be 0,");
		assertEquals(uv.getY(), 0, "Y should be 0.");
		assertEquals(uv.getWidth(), 0, "Width should be 0.");
		assertEquals(uv.getHeight(), 0, "Height should be 0.");
	}

	@Test
	void customInit() {
		UV uv = new UV(1, 2, 3, 4);

		assertEquals(uv.getX(), 1, "X should be 1,");
		assertEquals(uv.getY(), 2, "Y should be 2.");
		assertEquals(uv.getWidth(), 3, "Width should be 3.");
		assertEquals(uv.getHeight(), 4, "Height should be 4.");
	}

	@Test
	void testSetGet() {
		UV uv = new UV();

		uv.setX(5);
		assertEquals(uv.getX(), 5, "X should be 5.");

		uv.setY(6);
		assertEquals(uv.getY(), 6, "X should be 6.");

		uv.setWidth(7);
		assertEquals(uv.getWidth(), 7, "X should be 7.");

		uv.setHeight(8);
		assertEquals(uv.getHeight(), 8, "X should be 8.");
	}
}
