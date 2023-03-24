
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A custom formatter for log messages that only includes INFO-level records.
 * This formatter overrides the method to format log records.
 */
public class MyFormatter extends Formatter {
    /**
     * Formats the specified log record.
     *
     * @param record the log record to be formatted.
     * @return the formatted log message if the record has an INFO level, or null otherwise.
     */
    public String format(LogRecord record){
        if(record.getLevel() == Level.INFO) {
            return record.getMessage() + "\r\n";
        }
        return null;
    }
}
