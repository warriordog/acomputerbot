package net.acomputerdog.ircbot.logging;

import java.io.IOException;
import java.io.OutputStream;

public class SplittingOutputStream extends OutputStream {

    private final OutputStream out1;
    private final OutputStream out2;

    public SplittingOutputStream(OutputStream out1, OutputStream out2) {
        super();
        if (out1 == null || out2 == null) throw new IllegalArgumentException("Output streams cannot be null!");
        this.out1 = out1;
        this.out2 = out2;
    }

    @Override
    public void write(int b) throws IOException {
        out1.write(b);
        out2.write(b);
    }

    @Override
    public void flush() throws IOException {
        out1.flush();
        out2.flush();
    }

    @Override
    public void close() throws IOException {
        out1.close();
        out2.close();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out1.write(b, off, len);
        out2.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out1.write(b);
        out2.write(b);
    }

    public OutputStream getOut1() {
        return out1;
    }

    public OutputStream getOut2() {
        return out2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplittingOutputStream)) return false;

        SplittingOutputStream that = (SplittingOutputStream) o;

        return out1.equals(that.out1) && out2.equals(that.out2);

    }

    @Override
    public int hashCode() {
        int result = out1.hashCode();
        result = 31 * result + out2.hashCode();
        return result;
    }
}
