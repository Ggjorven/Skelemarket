package skelemarket.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ColourTest {
	@Test
	void zeroInit() {
		Colour col = new Colour();

		assertEquals(col.getR(), 0, "R should be 0.");
		assertEquals(col.getG(), 0, "G should be 0.");
		assertEquals(col.getB(), 0, "B should be 0.");
		assertEquals(col.getA(), 255, "A should be 255.");
	}

	@Test
	void rgbInit() {
		Colour col = new Colour(1, 2, 3);

		assertEquals(col.getR(), 1, "R should be 1.");
		assertEquals(col.getG(), 2, "G should be 2.");
		assertEquals(col.getB(), 3, "B should be 3.");
		assertEquals(col.getA(), 255, "A should be 255.");

		col = new Colour(-1, 0, 0);

		assertEquals(col.getR(), 0, "R should be 0.");
		assertEquals(col.getG(), 0, "G should be 0.");
		assertEquals(col.getB(), 0, "B should be 0.");
		assertEquals(col.getA(), 255, "A should be 255.");
	}

	@Test
	void rgbaInit() {
		Colour col = new Colour(4, 5, 6, 7);

		assertEquals(col.getR(), 4, "R should be 4.");
		assertEquals(col.getG(), 5, "G should be 5.");
		assertEquals(col.getB(), 6, "B should be 6.");
		assertEquals(col.getA(), 7, "A should be 7.");

		col = new Colour(-2, 0, 0, 256);

		assertEquals(col.getR(), 0, "R should be 0.");
		assertEquals(col.getG(), 0, "G should be 0.");
		assertEquals(col.getB(), 0, "B should be 0.");
		assertEquals(col.getA(), 255, "A should be 255.");
	}

	@Test
	void testSetGet() {
		Colour col = new Colour();

		col.setR(8);
		assertEquals(col.getR(), 8, "R should be 8.");

		col.setG(9);
		assertEquals(col.getG(), 9, "G should be 9.");

		col.setB(10);
		assertEquals(col.getB(), 10, "B should be 10.");

		col.setA(11);
		assertEquals(col.getA(), 11, "A should be 11.");

		col.setR(-3);
		assertEquals(col.getR(), 0, "R should be 0.");

		col.setG(-4);
		assertEquals(col.getG(), 0, "G should be 0.");

		col.setB(-5);
		assertEquals(col.getB(), 0, "B should be 0.");

		col.setA(-6);
		assertEquals(col.getA(), 0, "A should be 0.");

		col.setR(257);
		assertEquals(col.getR(), 255, "R should be 255.");

		col.setG(258);
		assertEquals(col.getG(), 255, "G should be 255.");

		col.setB(259);
		assertEquals(col.getB(), 255, "B should be 255.");

		col.setA(260);
		assertEquals(col.getA(), 255, "A should be 255.");
	}

	@Test
	void testToString() {
		Colour col = new Colour(12, 13, 14, 15);
		String str = col.toString();

		assertEquals(str, "[12, 13, 14, 15]", "String should be [12, 13, 14, 15]");
	}
}
