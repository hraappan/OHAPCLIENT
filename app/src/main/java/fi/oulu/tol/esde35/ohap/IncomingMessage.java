package fi.oulu.tol.esde35.ohap;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Created by Hannu Raappana on 12.5.2015.
 */
public class IncomingMessage {
    private final static String TAG = "IncomingMessage";
    /**
     * The internal buffer. It is reserved in the readExactly() method.
     */
    private byte[] buffer;

    /**
     * The position where the next byte should be taken from.
     */
    private int position;

    /**
     * Character set used to convert strings.
     */
    private final Charset charset = Charset.forName("UTF-8");

    /**
     * Reads the specified amount of bytes from the given InputStream.
     *
     * @param inputStream the InputStream from which the bytes are read
     * @param length the amount of bytes to be read
     * @return the byte array of which length is the given length
     * @throws IOException when the actual read throws an exception
     */
    private static byte[] readExactly(InputStream inputStream, int length) throws IOException {
        byte[] bytes = new byte[length];
        int offset = 0;
        while (length > 0) {
            int got = inputStream.read(bytes, offset, length);
            if (got == -1)
                throw new EOFException("End of message input.");
            offset += got;
            length -= got;
        }
        return bytes;
    }

    /*
    Reads one message from the given InputStream into the internal buffer:
    Reads two bytes from the InputStream and converts those into 16 bit
    unsigned integer as integer16(). Then, reads that amount of bytes into
    the internal buffer.
     */
    public void readFrom(InputStream is) throws  IOException{

        //Reset the position.
        //position = 0;
       int value = (is.read() &0xFF) << 8 | is.read()&0xFF;
       Log.d(TAG, "The length value is: " + value );

        buffer = readExactly(is, value);
    }

    /*
    Takes the next byte from the internal buffer as integer8()
    and converts it to a binary value (boolean).
     */

    public boolean binary8() {

        if((buffer[position+=1] & 0xFF) == 1) {

            return true;
        }
        else {
            return false;
        }
    }
    /*
    Takes the next byte from the internal buffer.
     */

    public int integer8() {
        int value = (buffer[position] & 0xFF);
    return value;
    }

    /*
    Takes the next two bytes from the internal buffer and converts
    them to a 16 bit unsigned integer.
     */

    public int integer16() {
        int i = (buffer[position+=1] & 0xFF) <<8 | (buffer[position+=1] & 0xFF);
    return i;

    }

    /*
    Takes the next four bytes from the internal buffer and converts
    them to a 32 bit unsigned integer.
     */
    public int integer32() {

        int i = ((buffer[position+=1]) << 24)&0xFF|
                ((buffer[position+=1]) << 16)&0xFF|
                ((buffer[position+=1]) << 8)& 0xFF |
                ((buffer[position+=1]) << 0)& 0xFF;

    return  i;
    }

    /*
    Takes the next eight bytes from the internal buffer and converts
    them to a 64 bit floating point number (double).
     */

    public double decimal64() {

        long bits =
                ((buffer[position+=1]) << 56) &0xFF|
                ((buffer[position+=1]) << 48) &0xFF|
                ((buffer[position+=1]) << 40) &0xFF|
                ((buffer[position+=1]) << 32) &0xFF|
                ((buffer[position+=1]) << 24) &0xFF|
                ((buffer[position+=1]) << 16) &0xFF|
                ((buffer[position+=1]) << 8)  &0xFF|
                ((buffer[position+=1]) << 0)  &0xFF;
        double d = Double.longBitsToDouble(bits);
    return d;
    }

    /*
    Takes the next two bytes and as integer8() and uses that as a length
    of the string. Then, takes the length bytes from the internal buffer
    and converts them into a string using UTF-8 decoding.
    */

    public String text() {
        String string=null;
        int length = 0;
        length = buffer[position+=1] & 0xff| buffer [position+=1] & 0xff;
        Log.d(TAG, "The length of the string is: " + length);
        Log.d(TAG, "The length of the buffer: " + buffer.length);
        Log.d(TAG, "Current position is: " + position);

                string = new String(buffer, position+1, length, charset);

        position+=length;

            //Move the pointer to the end of the string.
        Log.d(TAG, "Current position is: " + position);

    return string;
    }

}
