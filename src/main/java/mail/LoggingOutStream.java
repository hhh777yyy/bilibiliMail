package mail;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

public class LoggingOutStream extends OutputStream {

    protected boolean hasBeenClosed = false;

    protected byte[] buf;

    protected int count;

    private int bufLength;

    public static final int DEFAULT_BUFFER_LENGTH = 2048;

    protected Logger logger;

    protected Level level;

    private LoggingOutStream()
    {

    }



    public LoggingOutStream(Logger log, Level level)
            throws IllegalArgumentException {
        if (log == null) {
            throw new IllegalArgumentException("cat == null");
        }
        if (level == null) {
            throw new IllegalArgumentException("priority == null");
        }

        this.level = level;
        logger = log;
        bufLength = DEFAULT_BUFFER_LENGTH;
        buf = new byte[DEFAULT_BUFFER_LENGTH];
        count = 0;
    }



    public void close()
    {
        flush();
        hasBeenClosed = true;
    }


    @Override
    public void write(final int b) throws IOException
    {
        if (hasBeenClosed) {
            throw new IOException("The stream has been closed.");
        }

        if (count == bufLength)
        {
            final int newBufLength = bufLength + DEFAULT_BUFFER_LENGTH;
            final byte[] newBuf = new byte[newBufLength];

            System.arraycopy(buf, 0, newBuf, 0, bufLength);

            buf = newBuf;
            bufLength = newBufLength;
        }

        buf[count] = (byte) b;
        count++;
    }



    public void flush()
    {
        if (count == 0)
        {
            return;
        }


        if (count == 1 && ((char) buf[0]) == '\n')
        {
            reset();
            return;
        }
        final byte[] theBytes = new byte[buf.length];
        System.arraycopy(buf, 0, theBytes, 0, count);
        logger.log(level, new String(theBytes));
        reset();
    }


    private void reset()
    {
        count = 0;
    }

}
