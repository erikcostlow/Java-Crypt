package com.costlowcorp.eriktools;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.*;
import javafx.scene.control.TreeTableCell;
import javafx.util.Callback;

/**
 * Display the contents of a certificate keystore grouped by Organization. A
 * separate file is used to determine Merger & Acquisition activity amongst
 * Certificate Authorities and group situations where one Certificate Authority
 * acquired another and just hasn't updated the name on the certificate (fair
 * behavior). Created by ecostlow on 4/24/14.
 */
public class GroupedListKeyStore extends TreeTableView<GroupedListKeyStoreItem> {

    public GroupedListKeyStore(KeyStore keyStore) {

        TreeTableColumn<GroupedListKeyStoreItem, String> trustedCol = new TreeTableColumn<>("Type");
        trustedCol.setCellValueFactory(vf -> {
            final GroupedListKeyStoreItem item = vf.getValue().getValue();
            if (item.getCertificate() == null) {
                return new SimpleStringProperty("");
            }
            try {
                final String alias = item.getName();
                final String s = keyStore.isKeyEntry(alias) ? "Private" : "Public";
                return new SimpleStringProperty(s);
            } catch (KeyStoreException e) {
                return new SimpleStringProperty("INVALID");
            }
        });

        final TreeTableColumn<GroupedListKeyStoreItem, String> ownerCol = new TreeTableColumn<>("Owner");
//        ownerCol.setCellFactory(param -> new TreeTableCell<GroupedListKeyStoreItem, String>() {
//            @Override
//            protected void updateItem(String item, boolean empty) {
//                super.updateItem(item, empty);
//                
//            }
//        });
        ownerCol.setCellValueFactory(cvf -> {
            {
                final GroupedListKeyStoreItem item = cvf.getValue().getValue();
                final StringProperty retval;
                if (item.getCertificate() == null) {
                    //cvf.getValue().setGraphic(new ImageView("/folder.png"));
                    retval = new ReadOnlyStringWrapper(item.getAttributes().getOrDefault("O", item.getName()));
                } else {
                    //cvf.getValue().setGraphic(null);
                    retval = new SimpleStringProperty(item.getName());
                }
                return retval;
            }
        });

        //From observation, lots of people seem to have quotes or spaces in their attributes, so ignore when sorting.
        ownerCol.setComparator((o1, o2) -> o1.trim().replaceAll("\"", "").compareToIgnoreCase(o2.trim().replaceAll("\"", "")));

        final TreeTableColumn<GroupedListKeyStoreItem, String> cnCol = new TreeTableColumn<>("Common Name (CN)");
        cnCol.setCellValueFactory(cvf -> {
            final GroupedListKeyStoreItem item = cvf.getValue().getValue();
            final String str = item.getCertificate() == null ? "" : item.getAttributes().getOrDefault("CN", "Unspecified");
            final StringProperty retval = new SimpleStringProperty(str);
            return retval;
        });

        final TreeTableColumn<GroupedListKeyStoreItem, String> expCol = new TreeTableColumn<>("Expires On");
        expCol.setCellValueFactory(cvf -> new ReadOnlyStringWrapper(cvf.getValue().getValue().getExpiration()));

        TreeTableColumn<GroupedListKeyStoreItem, String> algCol = new TreeTableColumn<>("Algorithm");
        algCol.setCellValueFactory(cvf -> new ReadOnlyStringWrapper(cvf.getValue().getValue().getAlgorithm()));

        TreeTableColumn<GroupedListKeyStoreItem, String> sigAlgCol = new TreeTableColumn<>("Signature");
        sigAlgCol.setCellValueFactory(cvf -> new ReadOnlyStringWrapper(cvf.getValue().getValue().getSignatureAlgorithm()));

        final TreeItem<GroupedListKeyStoreItem> root = new TreeItem<>(new GroupedListKeyStoreItem("root"));
        setRoot(root);
        setShowRoot(false);

        populate(root, keyStore);

        getColumns().addAll(ownerCol, cnCol, trustedCol, expCol, algCol, sigAlgCol);
        setShowRoot(false);
        setTableMenuButtonVisible(true);

        getSortOrder().setAll(ownerCol);
        sort();
    }

    /*
     * Read the list of Merger and Acquisition activity.
     * Walk through the list of certificates in the KeyStore.
     * If a certificate belongs to a conglomerate, nest it appropriately.
     * If the certificate belongs to a Certificate Authority outside of M&A activity, create a new group.
     */
    private void populate(TreeItem<GroupedListKeyStoreItem> root, KeyStore keyStore) {
        final ObservableList<GroupedListKeyStoreItem> certificateOL = convertToOL(keyStore);
        final Properties whoOwnsWho = new Properties();
        try {
            whoOwnsWho.load(getClass().getClassLoader().getResourceAsStream("whoOwnsWho.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Map<String, TreeItem<GroupedListKeyStoreItem>> largePlayers = new HashMap<>();
        whoOwnsWho.stringPropertyNames().stream().sorted().forEach(player -> {
            final TreeItem<GroupedListKeyStoreItem> item = new TreeItem<>(new GroupedListKeyStoreItem(player));
            item.setExpanded(true);
            final String[] subs = whoOwnsWho.getProperty(player).split("\\|");
            Arrays.asList(subs).stream().forEach(sub -> largePlayers.put(sub.toLowerCase(), item));
            largePlayers.put(player.toLowerCase(), item);
            root.getChildren().add(item);
        });

        certificateOL.stream().forEach(ca -> {
            final String owner = ca.getAttributes().get("O");
            final String ownerLCase = owner.toLowerCase();
            if (largePlayers.containsKey(ownerLCase)) {
                largePlayers.get(ownerLCase).getChildren().add(new TreeItem<>(ca));
            } else {
                final TreeItem<GroupedListKeyStoreItem> holder = new TreeItem<>(new GroupedListKeyStoreItem(owner));
                root.getChildren().add(holder);
                holder.setExpanded(true);
                holder.getChildren().add(new TreeItem<>(ca));
                largePlayers.put(ownerLCase, holder);
            }
        });

        root.getChildren().removeIf(child -> child.getChildren().isEmpty());
    }

    private ObservableList<GroupedListKeyStoreItem> convertToOL(KeyStore keyStore) {
        final ObservableList<GroupedListKeyStoreItem> retval = FXCollections.observableArrayList();
        final Enumeration<String> certificateEnum;
        try {
            certificateEnum = keyStore.aliases();
            while (certificateEnum.hasMoreElements()) {
                final String alias = certificateEnum.nextElement();
                final Certificate cert = keyStore.getCertificate(alias);
                final GroupedListKeyStoreItem accessor = new GroupedListKeyStoreItem(alias, cert);
                retval.add(accessor);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return retval;
    }

}
