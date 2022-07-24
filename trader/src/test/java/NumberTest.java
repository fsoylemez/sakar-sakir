import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberTest {

    @Test
    public void roundingTestBigDecimal() {
        BigDecimal someNumber = new BigDecimal("2.5754643583534");
        BigDecimal bigDecimal = someNumber.setScale(8, RoundingMode.DOWN);
        assertEquals("2.57546435", bigDecimal.toPlainString());
    }
}
