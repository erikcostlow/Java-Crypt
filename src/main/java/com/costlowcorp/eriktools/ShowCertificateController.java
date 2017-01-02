/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import com.costlowcorp.eriktools.back.CertificateUtilities;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class ShowCertificateController implements Initializable {

    private static final Map<String, String> ACRONYMS = new HashMap<>(7);
    /*
     * Lots of these extended keys.
     * @see http://www.oid-info.com/index.htm
     */
    private static final Map<String, String> EXTENDED_USAGE = new HashMap<>(9);

    static {
        ACRONYMS.put("CN", "Common Name");
        ACRONYMS.put("OU", "Organizational Unit");
        ACRONYMS.put("O", "Organization");
        ACRONYMS.put("L", "Locality/City");
        ACRONYMS.put("ST", "State");
        ACRONYMS.put("C", "Country");
        ACRONYMS.put("E", "Email");

        EXTENDED_USAGE.put("1.3.6.1.5.5.7.3.1", "TLS Web Server Authentication");
        EXTENDED_USAGE.put("1.3.6.1.5.5.7.3.2", "TLS Web Client Authentication");
        EXTENDED_USAGE.put("1.3.6.1.5.5.7.3.3", "Code signatures");
        EXTENDED_USAGE.put("1.3.6.1.5.5.7.3.4", "E-Mail signatures");
        EXTENDED_USAGE.put("1.3.6.1.5.5.7.3.8", "Timestamp authority");
    }

    @FXML
    private TextField alias;

    @FXML
    private TextField subject;

    @FXML
    private Text expirationDate;

    @FXML
    private Text expirationInEnglish;

    @FXML
    private Text algorithm;

    @FXML
    private Text signatureAlgorithm;

    @FXML
    private Text commonName;

    @FXML
    private Text organization;

    @FXML
    private Text locality;

    @FXML
    private Text country;

    @FXML
    private Text usage;

    @FXML
    private TextField serial;

    @FXML
    private Text issuedBy;

    @FXML
    private TextField sha256;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void initilize(Certificate certificate, String alias) {
        this.alias.textProperty().setValue(alias);
        subject.setText(CertificateUtilities.forWhom(certificate));
        expirationDate.setText(CertificateUtilities.getExpirationDateAsString(certificate));
        expirationInEnglish.setText("[InEnglish]");
        algorithm.setText(CertificateUtilities.getAlgorithm(certificate));
        signatureAlgorithm.setText(CertificateUtilities.getSignatureAlgorithm(certificate));
        final Map<String, String> fields = CertificateUtilities.getFields(certificate);
        commonName.setText(fields.getOrDefault("CN", "Unknown"));
        organization.setText(fields.getOrDefault("O", "Unknown"));
        locality.setText(fields.getOrDefault("L", "Unknown"));
        country.setText(fields.getOrDefault("C", "Unknown"));
        
        sha256.setText(CertificateUtilities.sha256(certificate));
        final X509Certificate x509 = (X509Certificate) certificate;
        final String issuer = x509.getSubjectDN().equals(x509.getIssuerDN()) ?
                "Self-issued" : String.valueOf(x509.getIssuerDN());
        issuedBy.setText(issuer);
        serial.setText(String.valueOf(x509.getSerialNumber()));
    }

    public void showDetails(ActionEvent e) {

    }

    public void export(ActionEvent e) {

    }
}
