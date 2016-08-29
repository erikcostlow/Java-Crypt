/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import java.util.Date;

/**
 *
 * @author ecostlow
 */
public class ArchiveOwnershipEntry {
    private final String name;
    
    private final Date whenMade;
    
    public ArchiveOwnershipEntry(String name, Date whenMade){
        this.name=name;
        this.whenMade=whenMade;
    }

    public String getName() {
        return name;
    }

    public Date getWhenMade() {
        return whenMade;
    }
}
