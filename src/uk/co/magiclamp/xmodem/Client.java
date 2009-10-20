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
        byte[]  block           = new byte[128];
        int     marker;
        int     retries         = RETRIES;

        output.write(NAK);

        while ((marker = input.read()) != EOT) {

            if (marker != SOH) error("was expecting SOH");

            if (input.read() != expectedBlock) {
                error("was expecting block " + expectedBlock);
            }

            if (expectedBlock + input.read() != 255) {
                error("block check doesn't add up");
            }

            int sum = 0;
            int len = 0;
            for (int i = 0; i < block.length; i++) {
                block[i] = (byte) input.read();
                sum += block[i];
                if (block[i] != padding) {
                     len++;
                }
            }

            int checksum = input.read();

            if (sum % 256 != checksum) {
                retries--;
                if (retries > 0) {
                    output.write(NAK);
                    continue;
                } else {
                    error("error receiving block " + expectedBlock + "; gave up after " + RETRIES + " retries");
                }
            }

            collector.write(block, 0, len);

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
