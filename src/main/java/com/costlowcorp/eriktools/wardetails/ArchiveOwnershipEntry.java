/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.back.MadeBy;
import java.util.Date;

/**
 *
 * @author ecostlow
 */
public class ArchiveOwnershipEntry {
    private final String name;
    
    private Date whenMade;
    
    private MadeBy madeBy=MadeBy.THIRD_PARTY;
    
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

    void setWhenMade(Date date) {
        this.whenMade=date;
    }

    public MadeBy getOwnership() {
        return madeBy;
    }

    public void setMadeBy(MadeBy madeBy) {
        this.madeBy = madeBy;
    }
}
