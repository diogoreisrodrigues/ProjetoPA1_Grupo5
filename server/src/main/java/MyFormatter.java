
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MyFormatter extends Formatter {
    public String format(LogRecord record){
        if(record.getLevel() == Level.INFO) {
            return record.getMessage() + "\r\n";
        }
        return null;
    }
}
