/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

/**
 *
 * @author ecostlow
 */
public class ErikUtils {
    private static final String UNKNOWN = "UNKNOWN";
 
    /**
     * 
     * @param filename
     * @return The string after the last period (non-inclusive).
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return UNKNOWN;
        }
        filename = filename.toLowerCase();
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return UNKNOWN;
        } else if (lastDot == filename.length() - 1) {
            return UNKNOWN;
        }
        return filename.substring(lastDot + 1);
    }
    
    /**
     * 
     * @param filename
     * @return The filename portion of something slashed, like /folder/filename.png or just filename.txt
     */
    public static final String justFilename(String filename){
        final String retval;
        final int end;
        if(filename.endsWith("/")){
            end = filename.length()-1;
        }else{
            end = filename.length();
        }
        final int start = filename.contains("/") ? filename.lastIndexOf('/', end-1)+1 : 0;
        retval = filename.substring(start, end);
        return retval;
    }
}
