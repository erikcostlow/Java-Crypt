/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import com.costlowcorp.eriktools.back.CertificateAccessor;
import java.net.URL;
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

    void initilize(CertificateAccessor accessor, String alias) {
        this.alias.textProperty().setValue(alias);
        subject.setText(accessor.forWhom());
        expirationDate.setText(accessor.getExpirationDate());
        expirationInEnglish.setText("...");
        algorithm.setText(accessor.getAlgorithm());
        signatureAlgorithm.setText(accessor.getSignatureAlgorithm());
        
        sha256.setText(accessor.sha256());
        final String issuer = accessor.getCertificate().getSubjectDN().equals(accessor.getCertificate().getIssuerDN()) ?
                "Self-issued" : String.valueOf(accessor.getCertificate().getIssuerDN());
        issuedBy.setText(issuer);
        serial.setText(String.valueOf(accessor.getCertificate().getSerialNumber()));
    }

    public void showDetails(ActionEvent e) {

    }

    public void export(ActionEvent e) {

    }
}
