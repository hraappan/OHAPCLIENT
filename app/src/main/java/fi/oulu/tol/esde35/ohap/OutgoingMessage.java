package fi.oulu.tol.esde35.ohap;

import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by Hannu Raappana on 12.5.2015.
 */
public class OutgoingMessage {


    private static final String TAG = "OutgoingMessage";
    /**
     * The internal buffer. It will be grown if the message do not fit to it.
     */
    private byte[] buffer = new byte[256];

    /**
     * The position where the next byte should be appended. The initial position
     * skips the space reserved for the message length.
     */
    private int position = 2;

    /**
     * Character set used to convert strings.
     */
    private final Charset charset = Charset.forName("UTF-8");


    /**
     * Ensures that the internal buffer have room for the specified amount of
     * bytes. Grows the buffer when needed by doubling its size.
     *
     * @param appendLength the amount of bytes to be appended
     */
    private void ensureCapacity(int appendLength) {
        if (position + appendLength < buffer.length)
            return;
        int newLength = buffer.length * 2;
        while (position + appendLength >= newLength)
            newLength *= 2;
        buffer = Arrays.copyOf(buffer, newLength);
    }

    /*
    Writes one message into the given OutputStream from the internal buffer:
    Sets the count of appended bytes into the first two bytes of the internal
    buffer as integer16(). Note that the room for the length must have been
    reserved at the beginning. Then, writes the count + 2 (thus, takes into
    account the length that was just set) bytes from the internal buffer into
    the OutputStream.
     */
    public void writeTo(OutputStream os) {

        buffer[0] = (byte) (position-2 >> 8);
        buffer[1] = (byte) (position-2);

        try {
            os.write(buffer, 0, position);
            os.flush();

        }catch(IOException e) {
            Log.d(TAG, "There was an exception: " + e);

        }
    }

    /*
    Converts the given binary value (boolean) to a byte (false is 0, true is 1) and
    appends it into the internal buffer as integer8().
     */
    public OutgoingMessage binary8(boolean bool) {
        if(bool)
            integer8(1);
        else
            integer8(0);
        return this;
    }

    /*
    Appends the given byte into the internal buffer.
     */
    public OutgoingMessage integer8(int value) {
        ensureCapacity(1);
            buffer[position] = (byte) (value);
            position++;
        return this;

    }

    /*
     Converts the given 16 bit unsigned integer to
      two bytes and appends those into the internal buffer.
     */
    public OutgoingMessage integer16(int value) {
        ensureCapacity(2);
        buffer[position] = (byte) (value >> 8);
        position++;
        buffer[position] = (byte) (value);
        position++;

        return this;
    }

    /*
    Converts the given 32 bit unsigned integer to
    four bytes and appends those into the internal buffer.
     */
    public OutgoingMessage integer32(int value) {
        ensureCapacity(4);
        buffer[position] = (byte) (value >> 24);
        position++;
        buffer[position] = (byte) (value >> 16);
        position++;
        buffer[position] = (byte) (value >> 8);
        position++;
        buffer[position] = (byte) (value);
        position++;
        return this;
    }

    /*
    Converts the given 64 bit floating point number (double)
     to eight bytes and appends those into the internal buffer.
     */
    public OutgoingMessage decimal64(double value) {

        ensureCapacity(8);
        long v = Double.doubleToLongBits(value);

        buffer[position] = (byte)(v >> 56);
        position++;
        buffer[position] = (byte)(v >> 48);
        position++;
        buffer[position] = (byte)(v >> 40);
        position++;
        buffer[position] = (byte)(v >>  32);
        position++;
        buffer[position] = (byte)(v >>  24);
        position++;
        buffer[position] = (byte)(v >> 16);
        position++;
        buffer[position] = (byte)(v >>  8);
        position++;
        buffer[position] = (byte)(v);
        position++;
    return this;
    }

    /*
    Converts the given string to bytes using UTF-8 encoding.
    Then, appends the length of the string in bytes into the
    internal buffer as integer16() and finally appends the bytes also.
     */
    public OutgoingMessage text(String text) {

        byte [] bytes = text.getBytes(charset);
            integer16(text.length());

        ensureCapacity(bytes.length);
        for(int i = 0; i<bytes.length; i++) {
            buffer[position] = bytes[i];
            position++;
        }
    return this;
    }



}
