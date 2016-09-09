/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.fx.utils;

import com.costlowcorp.eriktools.ErikUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import javafx.scene.image.Image;

/**
 *
 * @author ecostlow
 */
public class ImageGrabber {
    private static final ClassLoader LOADER = ImageGrabber.class.getClassLoader();
    private static final Map<String, Image> IMAGES = new WeakHashMap<>();

    private static Image retrieve(String file) throws IOException {
        try(InputStream in = LOADER.getResourceAsStream("images/icons/" + file)){
            return new Image(in);
        }
    }
    
    private ImageGrabber(){
        
    }
    
    public static Image imageFor(String filename){
        if(IMAGES.containsKey(filename)){
            return IMAGES.get(filename);
        }
        try{
        final Image retval;
        final String extension = ErikUtils.getExtension(filename);
        switch(extension){
            case "class":
            case "java":
                retval = retrieve("javaClass.gif");
                break;
            case "properties":
            case "mf":
                retval = retrieve("config.png");
                break;
            case "html":
                retval = retrieve("html.png");
                break;
            case "js":
            case "json":
                retval = retrieve("javascript.png");
                break;
            case "xml":
                retval = retrieve("xml.png");
                break;
            case "sf":
            case "rsa":
                retval = retrieve("lock.png");
                break;
            case "jar":
                retval = retrieve("jar.png");
                break;
            case "png":
            case "gif":
            case "svg":
            case "jpg":
            case "jpeg":
                retval = retrieve("image.png");
                break;
            default:
                retval=null;
        }
        IMAGES.put(extension, retval);
        return retval;
        }catch(Exception e){
            Logger.getLogger(ImageGrabber.class.getSimpleName()).info("Unable to get Image for " + filename + " -- " + e.getMessage());
            return null;
        }
    }
}
