/*
 * Copyright Erik Costlow.
 * Not authorized for use or view by others.
 */
package com.costlowcorp.eriktools.back;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Erik Costlow
 */
public class CertificateUtilities {

    private CertificateUtilities() {
    }

    /**
     * Human-readable word for a certificate that can sign code.
     */
    public static final String CANSIGN = "Digital Signature";

    private static final ThreadLocal<SimpleDateFormat> T_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    /**
     * @param certificate
     * @return SHA-256 hash of the certificate (non-delimited)
     */
    public static String sha256(Certificate certificate) {
        String retval;
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(certificate.getEncoded());
            byte[] mdbytes = md.digest();
            StringBuilder hexString = new StringBuilder(mdbytes.length);
            for (byte b : mdbytes) {
                hexString.append(Integer.toHexString(0xFF & b));
            }
            md.reset();

            retval = hexString.toString().toUpperCase();
        } catch (NoSuchAlgorithmException | CertificateException e) {
            retval = null;
        }
        return retval;
    }

    /**
     * @return Formatted date
     */
    public static String getExpirationDateAsString(Certificate certificate) {
        if (certificate instanceof X509Certificate) {
            return T_DATE_FORMAT.get().format(((X509Certificate) certificate).getNotAfter());
        }
        return null;
    }
    
    /**
     * @return Formatted date
     */
    public static Date getExpirationDateAsDate(Certificate certificate) {
        if (certificate instanceof X509Certificate) {
            return ((X509Certificate) certificate).getNotAfter();
        }
        return null;
    }

    /**
     * @param certificate
     * @return Formatted name of the algorithm
     */
    public static String getAlgorithm(Certificate certificate) {
        final PublicKey key = certificate.getPublicKey();
        final StringBuilder sb = new StringBuilder(key.getAlgorithm());
        if (key instanceof RSAPublicKey) {
            sb.append(' ');
            sb.append(((RSAPublicKey) key).getModulus().bitLength());
        } else if (key instanceof ECPublicKey) {
            sb.append(' ');
            final ECPublicKey blah = (ECPublicKey) key;
            sb.append(blah.getParams().getCurve().getField().getFieldSize());
        }
        return sb.toString();
    }

    /**
     * The algorithm used for certifying the certificate is a different hash
     * determined by the choice of getAlgorithm name. (bit length does not
     * matter)
     *
     * @param certificate
     * @return Signature algorithm name
     */
    public static String getSignatureAlgorithm(Certificate certificate) {
        final String retval;
        if (certificate instanceof X509Certificate) {
            final String textClaim = ((X509Certificate) certificate).getSigAlgName();
            retval = textClaim;
            /*
            try {
                final String objectIdentifierClaim = AlgorithmId.get(certificate.getSigAlgOID()).toString();
                if(textClaim.equalsIgnoreCase(objectIdentifierClaim)){
                    retval = textClaim;
                }else{
                    retval = "MISMATCH: " + textClaim + " vs. " + objectIdentifierClaim;
                }
            } catch (NoSuchAlgorithmException e) {
                retval = e.getMessage();
            }*/
        } else {
            retval = "UNKNOWN CERTIFICATE TYPE";
        }

        return retval;
    }

    /**
     *
     * @param certificate
     * @return Signature algorithm but without the "withXXX" term
     */
    public static String getSignatureAlgorithmTruncated(Certificate certificate) {
        final String sigAlg = getSignatureAlgorithm(certificate);
        return sigAlg.substring(0, sigAlg.indexOf("with"));
    }

    public static List<String> getKeyUsage(Certificate certificate) {
        if (certificate instanceof X509Certificate) {
            final X509Certificate x509 = (X509Certificate) certificate;
            final List<String> retval;
            if (x509.getKeyUsage() == null) {
                retval = Collections.emptyList();
            } else {
                retval = new ArrayList<>(1);
                final String[] words = {CANSIGN, "Non-Repudiation", "Key Encipherment", "Data Encipherment", "Key Agreement", "Key Cert Sign", "Offline Certification Revocation verification", "Encipher Only", "Decipher Only"};
                for (int i = 0; i < x509.getKeyUsage().length; i++) {
                    if (x509.getKeyUsage()[i]) {
                        retval.add(words[i]);
                    }
                }
            }

            return Collections.unmodifiableList(retval);
        }
        return Collections.EMPTY_LIST;
    }

    public static List<String> getExtendedKeyUsage(Certificate certificate) {
        if (certificate instanceof X509Certificate) {
            final X509Certificate x509 = (X509Certificate) certificate;
            List<String> retval;
            try {
                if (x509.getExtendedKeyUsage() == null) {
                    return Collections.emptyList();
                } else {
                    retval = new ArrayList<>();
                    for (String keyUsage : x509.getExtendedKeyUsage()) {
                        retval.add(keyUsage);
                    }
                }
            } catch (CertificateParsingException e) {
                retval = Collections.emptyList();
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return retval;
        }
        return Collections.EMPTY_LIST;
    }

    public static String forWhom(Certificate certificate) {
        //final X500Principal principal = certificate.getSubjectX500Principal();
        if(certificate==null){
            return null;
        }else if (certificate instanceof X509Certificate) {
            final X509Certificate x509 = (X509Certificate) certificate;
            return certificate == null ? null : x509.getSubjectDN().toString();// + " :: " + principal.getName("RFC1779");
        }
        return null;
    }
    
    public static Map<String, String> getFields(Certificate certificate) {
        final Map<String, String> retval = new LinkedHashMap<>(5);
        final String rfc1779 = forWhom(certificate);
        final String splitter = "=";
        int current = 0;
        while (rfc1779.indexOf(splitter, current) != -1) {
            int next = rfc1779.indexOf(splitter, current + 1);
            final String lsubstr = rfc1779.substring(current, next);
            String left = findLeft(lsubstr);

            int rnext = rfc1779.indexOf(splitter, next + 1);
            final String right;
            if (rnext == -1) {
                right = rfc1779.substring(next + 1);
            } else {
                final String tempright = rfc1779.substring(next + 1, rnext);
                right = tempright.substring(0, tempright.lastIndexOf(", "));
            }
            retval.put(left, right);
            current = next + 1;
        }
        return retval;
    }
    
    public static String getOwnerAttribute(Certificate certificate){
        final Map<String, String> attributes = getFields(certificate);
        return attributes.get("O");
    }
    
    private static String findLeft(String word) {
        final int pos = word.lastIndexOf(' ');
        return pos == -1 ? word : word.substring(pos + 1);
    }
}
