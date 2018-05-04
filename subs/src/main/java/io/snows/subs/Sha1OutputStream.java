package io.snows.subs;

import android.util.Base64;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1OutputStream extends OutputStream {
    protected OutputStream out;
    protected MessageDigest digest;

    public Sha1OutputStream(OutputStream out) throws NoSuchAlgorithmException {
        this.out = out;
        this.digest = MessageDigest.getInstance("SHA1");
    }

    @Override
    public void write(int b) throws IOException {
        throw new IOException("not impl");
    }

    @Override
    public void write(byte b[]) throws IOException {
        out.write(b);
        digest.update(b);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        digest.update(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    public String sha1() {
        return Base64.encodeToString(digest.digest(),0);
    }
}
