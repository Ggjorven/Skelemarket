package skelemarket;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertTrue;

public class FirstTest {
    @Test
    void testBasicMath() {
        int a = 2;
        int b = 2;

        int result = a + b;

        assertEquals(4, result, "2 + 2 should equal 4");
    }
}
