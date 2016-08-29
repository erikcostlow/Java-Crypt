/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.back;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * Helper for crawling through an archive of archives of...<br/>
 * For example, a WAR that includes a JAR that includes another JAR.<br/>
 * Or an EAR that includes a WAR that includes...
 *
 * @author ecostlow
 */
public class NestedInputStream extends InputStream {

    final ZipInputStream parent;

    public NestedInputStream(ZipInputStream zis) {
        this.parent = zis;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return parent.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return parent.read(b);
    }

    @Override
    public int read() throws IOException {
        return parent.read();
    }

    @Override
    public boolean markSupported() {
        return parent.markSupported();
    }

    @Override
    public synchronized void mark(int readlimit) {
        parent.mark(readlimit); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int available() throws IOException {
        return parent.available(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long skip(long n) throws IOException {
        return parent.skip(n); //To change body of generated methods, choose Tools | Templates.
    }
}
