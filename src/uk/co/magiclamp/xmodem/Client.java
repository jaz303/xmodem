package uk.co.magiclamp.xmodem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Jason Frame
 */
public class Client {

    private static final int RETRIES    = 10;

    private static final byte SOH       = 0x1;
    private static final byte EOT       = 0x4;
    private static final byte ACK       = 0x6;
    private static final byte NAK       = 0x15;
    private static final byte SUB       = 0x1A;

    private final InputStream   input;
    private final OutputStream  output;
    private final byte          padding;

    public Client(InputStream i, OutputStream o, byte p) {
        input   = i;
        output  = o;
        padding = p;
    }

    public Client(InputStream i, OutputStream o) {
        this(i, o, SUB);
    }

    public byte[] read() throws IOException {

        ByteArrayOutputStream collector = new ByteArrayOutputStream();

        int     expectedBlock   = 1;
        byte[]  block           = new byte[131];
        boolean complete        = false;
        int     marker;
        int     retries         = RETRIES;
        
        output.write(NAK);

        while ((marker = input.read()) != EOT) {

            if (marker != SOH) error("was expecting SOH");

            if (input.read(block) != 131) {
                error("was expecting block to be 131 bytes");
            }
            
            if (block[0] != expectedBlock) {
                error("was expecting block " + expectedBlock);
            }
            
            if (block[0] + block[1] != 255) {
                error("block check doesn't add up");
            }

            int checksum = 0;
            for (int i = 2; i < 130; i++) {
                checksum += block[i];
            }

            if (checksum % 255 != block[130]) {
                retries--;
                if (retries > 0) {
                    output.write(NAK);
                    continue;
                } else {
                    error("error receiving block " + expectedBlock + "; gave up after " + RETRIES + " retries");
                }
            }

            int peek = 2;
            int len  = 0;
            
            while (len <= 128 && block[peek++] != padding) len++;

            collector.write(block, 2, len);

            retries = RETRIES;
            output.write(ACK);
            
            expectedBlock++;

        }

        return collector.toByteArray();

    }

    private void error(String message) throws TransferException {
        throw new TransferException(message);
    }
}
