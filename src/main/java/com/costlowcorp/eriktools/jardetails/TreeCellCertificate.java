/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.jardetails;

import com.costlowcorp.eriktools.back.CertificateUtilities;
import java.security.cert.Certificate;
import java.util.Map;
import javafx.scene.control.TreeCell;

/**
 *
 * @author ecostlow
 */
public class TreeCellCertificate extends TreeCell<Certificate> {

    @Override
    protected void updateItem(Certificate item, boolean empty) {
        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
        if (empty) {
            setText(null);
            setGraphic(null);
        }else{
            final Map<String, String> props = CertificateUtilities.getFields(item);
            final String term = props.containsKey("CN") ? props.get("CN") : props.get("O");
             setText(term);
             setGraphic(null);
        }
    }

}
