package at.rovo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This custom log formatter formats parts of a log record to a single line.
 *
 * @author Roman Vottner
 */
public class LogFormatter extends Formatter
{
    private Level logLevel = Level.INFO;

    public void setLevel(Level level)
    {
        this.logLevel = level;
    }

    @Override
    public String format(LogRecord record)
    {
        StringBuffer sb = new StringBuffer();
        // avoid output of log messages that don't match the required log-level
        if (record.getLevel().intValue() >= this.logLevel.intValue())
        {
            sb.append(this.calcDate(record.getMillis()));
            sb.append("   ");
            sb.append(record.getLevel());
            sb.append("\t");
            String loggerName = record.getLoggerName();
            // get just the class name
            sb.append(loggerName.substring(loggerName.lastIndexOf(".") + 1));
            sb.append("::");
            sb.append(record.getSourceMethodName());
            sb.append(":\t");
            Object[] objs = record.getParameters();
            String message = record.getMessage();
            if (objs != null)
            {
                for (int i = 0; i < objs.length; i++)
                {
                    if (objs[i] == null)
                    {
                        message = message.replace("{" + i + "}", "null");
                    }
                    else
                    {
                        message = message.replace("{" + i + "}", objs[i].toString());
                    }
                }
            }
            sb.append(message);
            sb.append("\n");
        }
        return sb.toString();
    }

    private String calcDate(long millisecs)
    {
        SimpleDateFormat date_format = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }
}
