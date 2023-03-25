import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.util.logging.*;

public class MyFormatterTest {

    @Test
    @DisplayName("Test format method with INFO log level")
    public void testFormatWithInfoLevel() {
        MyFormatter formatter = new MyFormatter();
        LogRecord record = new LogRecord(Level.INFO, "Test message");

        String formattedMessage = formatter.format(record);

        assertEquals("Test message\r\n", formattedMessage);
    }

    @Test
    @DisplayName("Test format method with non-INFO log record")
    public void testFormatWithNonInfoLevel() {
        MyFormatter formatter = new MyFormatter();
        LogRecord record = new LogRecord(Level.WARNING, "Test message");

        String formattedMessage = formatter.format(record);

        assertNull(formattedMessage);
    }
}
