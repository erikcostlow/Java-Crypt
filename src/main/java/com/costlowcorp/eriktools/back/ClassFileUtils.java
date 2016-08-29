/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.back;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ecostlow
 */
public class ClassFileUtils {
    private static final Map<Integer, String> JAVA_CLASS_FORMAT;
    
    static{
        final Map<Integer, String> temp = new HashMap<>(4);
        temp.put(45, "Java 1.1");
        temp.put(46, "Java 1.2");
        temp.put(47, "Java 1.3");
        temp.put(48, "Java 1.4");
        temp.put(49, "Java 5");
        temp.put(50, "Java 6");
        temp.put(51, "Java 7");
        temp.put(52, "Java 8");
        temp.put(53, "Java 9");
        JAVA_CLASS_FORMAT = Collections.unmodifiableMap(temp);
    }
    
    public static String getJavaVersion(int classFileVersion){
        return JAVA_CLASS_FORMAT.getOrDefault(classFileVersion, "Unknown Java " + classFileVersion);
    }
    
    public static int latestVersion(String o1, String o2){
            if(o1==null || o2==null){
                return 1;
            }else if(!o1.startsWith("Java")){
                return -1;
            }else if(!o2.startsWith("Java")){
                return 1;
            }
            
            final Double d1 = parseNumber(o1);
            final Double d2 = parseNumber(o2);
            return d1.compareTo(d2);
    }

    private static Double parseNumber(String o1) {
        final int index = o1.indexOf(' ');
        final String temp = o1.substring(index+1);
        return Double.parseDouble(temp);
    }
}
