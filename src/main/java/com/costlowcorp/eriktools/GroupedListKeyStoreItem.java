/*
 * Copyright Erik Costlow.
 * Not authorized for use or view by others.
 */
package com.costlowcorp.eriktools;

import com.costlowcorp.eriktools.back.CertificateUtilities;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Erik Costlow
 */
public class GroupedListKeyStoreItem {
    private final String name;
    
    private final Certificate certificate;
    
    private final Map<String, String> attributes;
    
    private final String expiration;
    
    private final String algorithm;
    
    private final String signatureAlgorithm;
    
    public GroupedListKeyStoreItem(String name){
        this.name=name;
        this.certificate=null;
        this.attributes=Collections.EMPTY_MAP;
        this.expiration="";
        this.algorithm="";
        this.signatureAlgorithm="";
    }
    
    public GroupedListKeyStoreItem(String alias, Certificate certificate){
        this.name=alias;
        this.certificate=certificate;
        this.attributes=CertificateUtilities.getFields(certificate);
        this.expiration=CertificateUtilities.getExpirationDate(certificate);
        this.algorithm=CertificateUtilities.getAlgorithm(certificate);
        this.signatureAlgorithm=CertificateUtilities.getSignatureAlgorithmTruncated(certificate);
    }

    public String getName() {
        return name;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getExpiration() {
        return expiration;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }
    
}
