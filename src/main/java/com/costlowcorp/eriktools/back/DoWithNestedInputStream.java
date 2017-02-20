/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.back;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author erik
 */
public class DoWithNestedInputStream {

    private final String[] filenames;
    
    private final Consumer<InputStream> onReached;

    public DoWithNestedInputStream(String[] filenames, Consumer<InputStream> onReached) {
        this.filenames = filenames;
        this.onReached = onReached;
    }

    public void blah(InputStream in) {
        try(ZipInputStream zin = new ZipInputStream(in)){
            walkThrough(zin, 0);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private boolean walkThrough(ZipInputStream zin, int index){
            boolean keepGoing=true;
        try {
            for (ZipEntry ze = zin.getNextEntry(); ze != null && keepGoing; ze = zin.getNextEntry()) {
                keepGoing = check(index, zin, ze);
                if(!keepGoing){
                    return keepGoing;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return keepGoing;
    }

    private boolean check(int index, ZipInputStream zin, ZipEntry entry) throws IOException {
        if (filenames[index].equals(entry.getName())) {
            if (index == filenames.length - 1) {
                onReached.accept(zin);
                return false;
            } else {
                try(InputStream in = new NestedInputStream(zin);
                        ZipInputStream zinner = new ZipInputStream(in)){
                    //TODO have this affect the return value
                    return walkThrough(zinner, index+1);
                }
            }
        }
        return true;
    }
}
