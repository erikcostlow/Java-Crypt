/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import com.costlowcorp.eriktools.back.CertificateAccessor;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TreeItem;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class GroupedKeyStoreStatisticsController implements Initializable {

    @FXML
    private Text basicStats;

    @FXML
    private PieChart chart;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    void initialize(GroupedListKeyStore list) {
        final TreeItem<CertificateAccessor> root = list.getRoot();
        final AtomicInteger totalCerts = new AtomicInteger(0);
        final AtomicInteger groups = new AtomicInteger(0);

        final Set<String> countryCodes = new HashSet<>();
        root.getChildren().forEach(group -> {
            groups.incrementAndGet();
            totalCerts.addAndGet(group.getChildren().size());
            group.getChildren().stream().map(el -> el.getValue())
                    .flatMap(acc -> acc.getFields().stream())
                    .filter(el -> "C".equals(el.getName()))
                    .findAny()
                    .ifPresent(acc -> countryCodes.add(acc.getValue()));
        });
        final int countries = countryCodes.size();
        
        final String newString = String.format(basicStats.getText(), totalCerts.get(), groups.get(), countries);
        basicStats.setText(newString);
    }

}
