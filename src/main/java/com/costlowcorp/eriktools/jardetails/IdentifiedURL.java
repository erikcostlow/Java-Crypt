/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.jardetails;

/**
 *
 * @author ecostlow
 */
public class IdentifiedURL {
    
    private String path;
    
    private final String className;
    
    private final String methodNameAndDesc;
    
    private String type;
    
    public IdentifiedURL(String path, String className, String methodNameAndDesc, String type){
        this.path=path;
        this.className=className;
        this.methodNameAndDesc=methodNameAndDesc;
        this.type=type;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodNameAndDesc() {
        return methodNameAndDesc;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
}
