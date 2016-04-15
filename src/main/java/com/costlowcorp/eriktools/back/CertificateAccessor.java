package com.costlowcorp.eriktools.back;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * User: ecostlow <erik.costlow@oracle.com>
 * Date: 8/28/13
 * Time: 1:05 PM
 */
public class CertificateAccessor {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Human-readable word for a certificate that can sign code.
     */
    public static final String CANSIGN = "Digital Signature";

    private final X509Certificate certificate;

    public CertificateAccessor(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public CertificateAccessor(Certificate certificate) {
        this.certificate = (X509Certificate) certificate;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    /**
     * @return SHA-256 hash of the certificate (non-delimited)
     */
    public String sha256() {
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
            e.printStackTrace();
            retval = null;
        }
        return retval;
    }

    /**
     * @return Formatted date
     */
    public String getExpirationDate() {
        return DATE_FORMAT.format(certificate.getNotAfter());
    }

    /**
     * @return Formatted name of the algorithm
     */
    public String getAlgorithm() {
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
     * The algorithm used for certifying the certificate is a different hash determined by the choice of getAlgorithm name.
     * (bit length does not matter)
     *
     * @return Signature algorithm name
     */
    public String getSignatureAlgorithm() {
        final String textClaim = certificate.getSigAlgName();
        final String retval = textClaim;
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

        return retval;
    }

    /**
     *
     * @return Signature algorithm but without the "withXXX" term
     */
    public String getSignatureAlgorithmTruncated(){
        final String sigAlg = getSignatureAlgorithm();
        return sigAlg.substring(0, sigAlg.indexOf("with"));
    }

    public List<String> getKeyUsage() {
        final List<String> retval;
        if (certificate.getKeyUsage() == null) {
            retval = Collections.emptyList();
        } else {
            retval = new ArrayList<>(1);
            final String[] words = {CANSIGN, "Non-Repudiation", "Key Encipherment", "Data Encipherment", "Key Agreement", "Key Cert Sign", "Offline Certification Revocation verification", "Encipher Only", "Decipher Only"};
            for (int i = 0; i < certificate.getKeyUsage().length; i++) {
                if (certificate.getKeyUsage()[i]) {
                    retval.add(words[i]);
                }
            }
        }

        return Collections.unmodifiableList(retval);
    }

    public List<String> getExtendedKeyUsage() {
        List<String> retval;
        try {
            if (certificate.getExtendedKeyUsage() == null) {
                return Collections.emptyList();
            } else {
                retval = new ArrayList<>();
                for (String keyUsage : certificate.getExtendedKeyUsage()) {
                    retval.add(keyUsage);
                }
            }
        } catch (CertificateParsingException e) {
            retval = Collections.emptyList();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return retval;
    }

    public String forWhom() {
        //final X500Principal principal = certificate.getSubjectX500Principal();
        return certificate==null ? null : certificate.getSubjectDN().toString();// + " :: " + principal.getName("RFC1779");
    }

    public List<CertificateField> getFields() {
        final List<CertificateField> retval = new ArrayList<>(2);
        final String rfc1779 = forWhom();
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
            retval.add(new CertificateField(left, right));
            current = next + 1;
        }
        return retval;
    }

    public String getOwnerAttribute(){
        final List<CertificateField> attributes = getFields();
        final CertificateField attr = attributes.stream().filter(t -> "O".equals(t.getName())).findAny().orElse(null);
        return attr==null ? null : attr.getValue();
    }

    private String findLeft(String word) {
        final int pos = word.lastIndexOf(' ');
        return pos == -1 ? word : word.substring(pos + 1);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(CertificateAccessor.class.getSimpleName());
        sb.append('[');
        sb.append(forWhom());
        sb.append('\t');
        sb.append(getAlgorithm());
        sb.append('\t');
        sb.append(getExpirationDate());
        sb.append(']');
        return sb.toString();
    }

    public static ALGORITHM_CHECK isAlgorithmOk(String algorithm) {
        if(algorithm.toLowerCase().startsWith("sha1")){
            //Everybody is hating on SHA1 right now.
            return ALGORITHM_CHECK.WARNING;
        }else if(algorithm.toLowerCase().startsWith("md5")){
            //MD5 is even less popular
            return ALGORITHM_CHECK.WARNING;
        }else if(algorithm.toLowerCase().startsWith("dsa")){
            return ALGORITHM_CHECK.WARNING;
        }

        final Properties security = new Properties();
        final Path path = FileSystems.getDefault().getPath(System.getProperty("java.home"), "lib", "security", "java.security");
        try {
            security.load(Files.newInputStream(path, StandardOpenOption.READ));
            final String[] split = security.getProperty("jdk.certpath.disabledAlgorithms").split(",\\s?");
            for (String s : split) {
                final String lcase = s.toLowerCase();
                if(lcase.contains("keysize")){
                    if(algorithm.toLowerCase().equals(lcase.replace("keysize < ", ""))){
                        return ALGORITHM_CHECK.WARNING;
                    }
                }else if (algorithm.toLowerCase().startsWith(lcase)) {
                    return ALGORITHM_CHECK.INVALID;
                }
            }
        } catch (IOException e) {
            Logger.getLogger(CertificateAccessor.class.getSimpleName()).warning("Unable to check algorithm name");
        }
        return ALGORITHM_CHECK.OK;
    }

    /**
     * A few states for saying how good an algorithm is at present.
     */
    public static enum ALGORITHM_CHECK {
        /**
         * This algorithm looks OK to use.
         */
        OK,
        /**
         * This algorithm looks OK to use but is at the minimum keysize, meaning it is probably next to go.
         */
        WARNING,
        /**
         * This algorithm is no longer allowed.
         */
        INVALID;
    }
}
