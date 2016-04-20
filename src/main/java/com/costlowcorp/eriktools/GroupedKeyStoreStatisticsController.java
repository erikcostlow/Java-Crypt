/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TreeItem;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class GroupedKeyStoreStatisticsController implements Initializable {

    @FXML
    private Text certCountText;

    @FXML
    private Hyperlink orgCount;

    @FXML
    private Hyperlink countryCount;

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
        final TreeItem<GroupedListKeyStoreItem> root = list.getRoot();
        final AtomicInteger totalCerts = new AtomicInteger(0);
        final AtomicInteger groups = new AtomicInteger(0);

        countCAs(root);

        final Set<String> countryCodes = new HashSet<>();
        root.getChildren().forEach(group -> {
            groups.incrementAndGet();
            totalCerts.addAndGet(group.getChildren().size());
            group.getChildren().stream()
                    .map(el -> el.getValue())
                    .map(el -> el.getAttributes().get("C"))
                    .filter(Objects::nonNull)
                    .findAny()
                    .ifPresent(acc -> countryCodes.add(acc));
        });
        final int countries = countryCodes.size();

        final String newString = String.format(certCountText.getText(), totalCerts.get());
        certCountText.setText(newString);
        
        final String caCount = String.format(orgCount.getText(), groups.get());
        orgCount.setText(caCount);
        orgCount.setOnAction(click -> countCAs(root));
        
        final String countryCount = String.format(this.countryCount.getText(), countries);
        this.countryCount.setText(countryCount);
        this.countryCount.setOnAction(click -> pieCountries(root));
    }

    private void countCAs(TreeItem<GroupedListKeyStoreItem> root) {
        chart.getData().clear();
        root.getChildren().stream()
                .forEach(child -> chart.getData().add(new PieChart.Data(child.getValue().getName(), child.getChildren().size())));
    }

    private void pieCountries(TreeItem<GroupedListKeyStoreItem> root) {
        chart.getData().clear();
        final Map<String, PieChart.Data> pieData = new HashMap<>();
        root.getChildren().stream()
                .flatMap(el -> el.getChildren().stream())
                .map(el -> el.getValue())
                .map(el -> el.getAttributes().get("C"))
                .filter(Objects::nonNull)
                .forEach(country -> {
                    if(pieData.containsKey(country)){
                        final PieChart.Data check = pieData.get(country);
                        final double next = check.getPieValue()+1;
                        check.setPieValue(next);
                    }else{
                        final PieChart.Data d = new PieChart.Data(country, 1);
                        chart.getData().add(d);
                        pieData.put(country, d);
                    }
                });
        
    }
}
