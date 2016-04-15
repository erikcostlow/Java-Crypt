package com.costlowcorp.eriktools;

import com.costlowcorp.eriktools.back.CertificateAccessor;
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

/**
 * Display the contents of a certificate keystore grouped by Organization.
 * A separate file is used to determine Merger & Acquisition activity amongst Certificate Authorities and group
 * situations where one Certificate Authority acquired another and just hasn't updated the name on the certificate
 * (fair behavior).
 * Created by ecostlow on 4/24/14.
 */
public class GroupedListKeyStore extends TreeTableView<CertificateAccessor> {

    public GroupedListKeyStore(KeyStore keyStore){

        TreeTableColumn<CertificateAccessor, String> trustedCol = new TreeTableColumn<>("Type");
        trustedCol.setCellValueFactory(vf -> {
            if(vf.getValue().getValue().getCertificate()==null){
                return new SimpleStringProperty("");
            }
            try {
                final String alias = keyStore.getCertificateAlias(vf.getValue().getValue().getCertificate());
                final String s = keyStore.isKeyEntry(alias) ? "Private" : "Public";
                return new SimpleStringProperty(s);
            } catch (KeyStoreException e) {
                return new SimpleStringProperty("INVALID");
            }
        });

        final TreeTableColumn<CertificateAccessor, String> ownerCol = new TreeTableColumn<>("Owner");
        ownerCol.setCellValueFactory(cvf -> {
            {
                try {
                    if(cvf.getValue().getValue().getCertificate()==null){
                        cvf.getValue().setGraphic(new ImageView("/folder.png"));
                        return new ReadOnlyStringWrapper(cvf.getValue().getValue().getOwnerAttribute());
                    }
                    final StringProperty retval = new SimpleStringProperty(keyStore.getCertificateAlias(cvf.getValue().getValue().getCertificate()));
                    return retval;
                } catch (KeyStoreException e) {
                    return null;
                }
            }
        });

        //From observation, lots of people seem to have quotes or spaces in their attributes, so ignore when sorting.
        ownerCol.setComparator((o1, o2) -> o1.trim().replaceAll("\"", "").compareToIgnoreCase(o2.trim().replaceAll("\"", "")));

        final TreeTableColumn<CertificateAccessor, String> expCol = new TreeTableColumn<>("Expires On");
        expCol.setCellValueFactory(cvf -> new ReadOnlyStringWrapper(cvf.getValue().getValue().getExpirationDate()));

        TreeTableColumn<CertificateAccessor, String> algCol = new TreeTableColumn<>("Algorithm");
        algCol.setCellValueFactory(cvf -> new ReadOnlyStringWrapper(cvf.getValue().getValue().getAlgorithm()));

        TreeTableColumn<CertificateAccessor, String> sigAlgCol = new TreeTableColumn<>("Signature");
        sigAlgCol.setCellValueFactory(cvf -> new ReadOnlyStringWrapper(cvf.getValue().getValue().getSignatureAlgorithmTruncated()));


        final TreeItem<CertificateAccessor> root = new TreeItem<>(new CertificateAccessorFaker(""));
        setRoot(root);
        setShowRoot(false);

        populate(root, keyStore);

        getColumns().addAll(ownerCol, trustedCol, expCol, algCol, sigAlgCol);
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
    private void populate(TreeItem<CertificateAccessor> root, KeyStore keyStore){
        final ObservableList<CertificateAccessor> certificateOL = convertToOL(keyStore);
        final Properties whoOwnsWho = new Properties();
        try {
            whoOwnsWho.load(getClass().getClassLoader().getResourceAsStream("whoOwnsWho.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Map<String, TreeItem<CertificateAccessor>> largePlayers = new HashMap<>();
        whoOwnsWho.stringPropertyNames().stream().sorted().forEach(player -> {
            final TreeItem<CertificateAccessor> item = new TreeItem<>(new CertificateAccessorFaker(player));
            item.setExpanded(true);
            final String[] subs = whoOwnsWho.getProperty(player).split("\\|");
            Arrays.asList(subs).stream().forEach(sub -> largePlayers.put(sub.toLowerCase(), item));
            largePlayers.put(player.toLowerCase(), item);
            root.getChildren().add(item);
        });

        certificateOL.stream().forEach(ca -> {
            final String owner = ca.getOwnerAttribute().toLowerCase();
            if(largePlayers.containsKey(owner)){
                largePlayers.get(owner).getChildren().add(new TreeItem<>(ca));
            }else{
                final TreeItem<CertificateAccessor> holder = new TreeItem<>(new CertificateAccessorFaker(ca.getOwnerAttribute()));
                root.getChildren().add(holder);
                holder.setExpanded(true);
                holder.getChildren().add(new TreeItem<>(ca));
                largePlayers.put(owner, holder);
            }
        });

        root.getChildren().removeIf(child -> child.getChildren().isEmpty());
    }

    private ObservableList<CertificateAccessor> convertToOL(KeyStore keyStore) {
        final ObservableList<CertificateAccessor> retval = FXCollections.observableArrayList();
        final Enumeration<String> certificateEnum;
        try {
            certificateEnum = keyStore.aliases();
            while (certificateEnum.hasMoreElements()) {
                final String alias = certificateEnum.nextElement();
                final Certificate cert = keyStore.getCertificate(alias);
                final CertificateAccessor accessor = new CertificateAccessor(cert);
                retval.add(accessor);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return retval;
    }

    private static class CertificateAccessorFaker extends CertificateAccessor{
        private final String fakeOwner;
        public CertificateAccessorFaker(String fakeOwner){
            super(null);
            this.fakeOwner = fakeOwner;
        }

        @Override
        public String getOwnerAttribute(){
            return fakeOwner;
        }

        @Override
        public String getExpirationDate() {
            return null;
        }

        @Override
        public String getAlgorithm() {
            return null;
        }

        @Override
        public String getSignatureAlgorithm() {
            return null;
        }

        @Override
        public String getSignatureAlgorithmTruncated() {
            return null;
        }
    }
}
