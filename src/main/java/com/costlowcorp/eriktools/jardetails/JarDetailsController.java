/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.jardetails;

import com.costlowcorp.eriktools.ErikUtils;
import com.costlowcorp.eriktools.back.CertificateUtilities;
import com.costlowcorp.eriktools.back.ClassFileUtils;
import com.costlowcorp.eriktools.toolentry.JDepsGrabber;
import com.costlowcorp.fx.utils.UIUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.attribute.FileTime;
import java.security.CodeSigner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.PopOver;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class JarDetailsController implements Initializable {

    private final Map<String, MessageDigest> digests = new HashMap<>(3);

    @FXML
    private TextField filename;

    @FXML
    private TextField builtOn;

    @FXML
    private TextField builtWith;

    @FXML
    private TextField runsOn;

    @FXML
    private TextArea manifest;

    @FXML
    private TextFlow signedContainer;

    @FXML
    private ScrollPane detailPane;

    @FXML
    private PieChart languageChart;

    @FXML
    private VBox embeddedJars;

    @FXML
    private TextFlow jdepsInfo;
    
    @FXML
    private TextField mainClass;
    
    @FXML
    private TableView<PackageCount> packageTable;
    
    @FXML
    private TableColumn<PackageCount, String> packageCol;
    
    @FXML
    private TableColumn<PackageCount, Integer> classCountCol;
    
    @FXML
    private TableColumn<PackageCount, Boolean> sealedCol;

    public JarDetailsController() {
        try {
            digests.put("MD5-Digest", MessageDigest.getInstance("MD5"));
            digests.put("SHA1-Digest", MessageDigest.getInstance("SHA-1"));
            digests.put("SHA-256-Digest", MessageDigest.getInstance("SHA-256"));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(JarDetailsController.class.getName()).log(Level.SEVERE, "Unable to use hashing algorithms", ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        StringConverter<Object> sc = new StringConverter<Object>() {
            @Override
            public String toString(Object t) {
                return t == null ? null : t.toString();
            }
 
            @Override
            public Object fromString(String string) {
                return string;
            }
        };
        packageTable.setEditable(true);
        packageCol.setCellValueFactory(new PropertyValueFactory<>("packageName"));
        packageCol.setCellFactory((TableColumn<PackageCount, String> param) -> new TextFieldTableCell<>());
        classCountCol.setCellValueFactory(new PropertyValueFactory<>("classCount"));
        sealedCol.setCellValueFactory(new PropertyValueFactory("sealed"));
    }

    public void populateWith(File jarFile) {
        filename.setText(jarFile.getAbsolutePath());
        new Thread(() -> populateMeta(jarFile)).start();
        new Thread(() -> scanContents(jarFile)).start();
    }

    private void populateMeta(File jarFile) {
        try (JarFile jar = new JarFile(jarFile, false)) { //Validate later
            final Manifest fileManifest = jar.getManifest();
            final Attributes attributes = fileManifest.getMainAttributes();
            final String createdBy = attributes.containsKey("Build-Jdk") ? attributes.getValue("Build-Jdk") : attributes.getValue("Created-By");
            final ByteArrayOutputStream manifestOut = new ByteArrayOutputStream();
            fileManifest.write(manifestOut);
            final String manifestStr = manifestOut.toString();

            final String jdepsOutput = JDepsGrabber.INSTANCE.run(jarFile.toPath()).trim();
            final String mainClass = attributes.getValue("Main-Class");

            Platform.runLater(() -> {
                this.builtWith.setText(createdBy);
                this.manifest.setText(manifestStr);
                if(mainClass==null || mainClass.isEmpty()){
                    this.mainClass.setText("N/A");
                    this.mainClass.setDisable(true);
                }else{
                    this.mainClass.setText(mainClass);
                }
                final AtomicInteger places = new AtomicInteger();
                final AtomicInteger things = new AtomicInteger();
                final String jdepsText;
                if (jdepsOutput.contains("->")) {
                    Arrays.stream(jdepsOutput.split("\n")).forEach(line -> {
                        if (line.startsWith("      ->")) {
                            things.incrementAndGet();
                        } else if (line.startsWith("   ")) {
                            places.incrementAndGet();
                        }
                    });
                    jdepsText = String.format("Yes, %d places and %d things.", places.get(), things.get());
                    final Hyperlink jdepsLink = new Hyperlink(jdepsText);
                    jdepsLink.setOnAction(event -> {
                        final Region newNode;
                        if (languageChart.equals(detailPane.getContent())) {
                            final TextArea area = new TextArea();
                            area.setText(jdepsOutput);
                            area.setEditable(false);
                            final Label title = new Label("JDeps Output");
                            final Label desc = new Label("If you can change code, use the known replacements. If you cannot, check for library updates.");
                            desc.setWrapText(true);
                            final VBox vb = new VBox(title, desc, area);
                            VBox.setVgrow(area, Priority.ALWAYS);
                            newNode = vb;
                        }else{
                            newNode = languageChart;
                        }
                        flipDetails(newNode);
                        
                        /*final PopOver popover = new PopOver(area);
                        popover.cornerRadiusProperty().set(3.0);
                        popover.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);
                        popover.show(jdepsLink);*/
                    });
                    jdepsInfo.getChildren().setAll(jdepsLink);
                } else {
                    jdepsText = "No code issues detected.";
                    jdepsInfo.getChildren().setAll(new Label(jdepsText));
                }
            });

            final Optional<JarEntry> mostRecentlyUpdatedEntry = jar.stream().max((JarEntry o1, JarEntry o2) -> {
                final FileTime o1Time = o1.getLastModifiedTime();
                final FileTime o2Time = o2.getLastModifiedTime();
                if (o1Time == null || o2Time == null) {
                    return 1;
                }
                return o1Time.compareTo(o2Time);
            });
            mostRecentlyUpdatedEntry.ifPresent(entry -> {
                final FileTime when = entry.getLastModifiedTime();
                Platform.runLater(() -> builtOn.setText(String.valueOf(when)));
            });

        } catch (IOException ex) {
            Logger.getLogger(JarDetailsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void scanContents(File jarFile) {
        try (JarFile jar = new JarFile(jarFile, true)) {
            updateCodeSigning(jar);
        } catch (IOException ex) {
            Logger.getLogger(JarDetailsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateSignatureNo() {
        signedContainer.getChildren().setAll(new Label("No"));
    }

    private void updateSignatureYes(Set<CodeSigner> codeSigners, Set<Certificate> certificates, Set<String> unsignedFiles, Set<String> signedFiles, Set<String> signedInvalidFiles) {
        final Hyperlink yesLink;
        if (!codeSigners.isEmpty()) {
            final Iterator<CodeSigner> iter = codeSigners.iterator();
            final CodeSigner first = iter.next();
            final CertPath path = first.getSignerCertPath();
            final Certificate firstCert = first.getSignerCertPath().getCertificates().get(0);
            final Map<String, String> params = CertificateUtilities.getFields(firstCert);
            final String term = params.getOrDefault("CN", params.get("O"));
            yesLink = new Hyperlink(term);

            final Optional<Certificate> earliest = codeSigners.parallelStream()
                    .flatMap(signer -> signer.getTimestamp() == null ? signer.getSignerCertPath().getCertificates().stream() : signer.getTimestamp().getSignerCertPath().getCertificates().stream())
                    .filter(cert -> CertificateUtilities.getExpirationDateAsDate(cert) != null)
                    .min((Certificate o1, Certificate o2) -> CertificateUtilities.getExpirationDateAsDate(o1).compareTo(CertificateUtilities.getExpirationDateAsDate(o2)));
            final Label validUntil;
            if (earliest.isPresent()) {
                validUntil = new Label("Signature valid until " + CertificateUtilities.getExpirationDateAsString(earliest.get()));
            } else {
                validUntil = new Label("Unknown expiration");
            }
            final Label timeStampLabel;

            if (first.getTimestamp() == null) {
                timeStampLabel = new Label(" but not timestamped. ");
            } else {

                final Date date = first.getTimestamp().getTimestamp();
                LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                timeStampLabel = new Label(" on " + ld.format(DateTimeFormatter.ISO_DATE));

            }
            final Hyperlink unsignedFilesLink = signatureLink(unsignedFiles, " Unsigned files");
            final Hyperlink signedFilesLink = signatureLink(signedFiles, " Validated files");
            final Hyperlink signedInvalidFilesLink = signatureLink(signedInvalidFiles, " Tampered files");
            final VBox fileSignatures = new VBox(unsignedFilesLink, signedFilesLink, signedInvalidFilesLink);
            signedContainer.getChildren().setAll(new Label("Yes, by "), yesLink, timeStampLabel, validUntil, fileSignatures);

            yesLink.setOnAction(event -> {
                final Region newNode;
                if (languageChart.equals(detailPane.getContent())) {
                    FXMLLoader ldr = UIUtils.load(CertificatePathController.class);
                    final CertificatePathController ctrl = ldr.getController();
                    ctrl.initialize(path);
                    newNode = ldr.getRoot();
                } else {
                    newNode = languageChart;
                }

                flipDetails(newNode);
            });
        } else {
            yesLink = new Hyperlink("Yes.");
            signedContainer.getChildren().setAll(yesLink);
        }

    }

    private static Hyperlink signatureLink(Set<String> files, String suffix) {
        final Hyperlink retval = new Hyperlink(files.size() + " " + suffix);
        retval.setOnAction(event -> {
            final TextArea area = new TextArea();
            area.setText(String.join(System.getProperty("line.separator", "\n"), files));
            final PopOver popover = new PopOver(area);
            popover.cornerRadiusProperty().set(3.0);
            popover.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);
            popover.show(retval);
        });
        return retval;
    }

    private void updateCodeSigning(JarFile jar) throws IOException {
        final Set<CodeSigner> codeSigners = new HashSet<>();
        final Set<Certificate> certificates = new HashSet<>();
        final Map<String, Integer> fileCount = new HashMap<>();
        final Manifest jarManifest = jar.getManifest();

        final SortedSet<String> unsignedFiles = new TreeSet<>(String::compareToIgnoreCase);
        final SortedSet<String> signedFiles = new TreeSet<>(String::compareToIgnoreCase);
        final SortedSet<String> signedInvalidFiles = new TreeSet<>(String::compareToIgnoreCase);
        final Set<String> javaVersions = new HashSet<>(5);
        final Set<String> jarFilesInJar = new TreeSet<>();
        final Map<String, AtomicInteger> packageCount = new TreeMap<>();
        final ObservableList<PackageCount> packageData = FXCollections.observableArrayList();
        
        jar.stream()
                .filter(entry -> !entry.isDirectory())
                .forEach(entry -> {
                    final String name = entry.getName();
                    final String extension = ErikUtils.getExtension(name);
                    if ("jar".equalsIgnoreCase(extension)) {
                        jarFilesInJar.add(name);
                    }
                    final BiConsumer<byte[], Integer> readFileIfNeeded;
                    final Consumer<Void> countProperly;
                    if ("class".equals(extension)) {
                        //@TODO parse with ASM
                        final ByteArrayOutputStream out = new ByteArrayOutputStream();
                        readFileIfNeeded = (bytes, length) -> readClassBytes(out, bytes, length);

                        countProperly = (vf) -> {
                            final ClassReader reader = new ClassReader(out.toByteArray());
                            final ClassFileMetaVisitor v = new ClassFileMetaVisitor(Opcodes.ASM5, null);
                            reader.accept(v, ClassReader.SKIP_CODE);
                            fileCount.put(v.getLanguage(), fileCount.getOrDefault(v.getLanguage(), 0) + 1);
                            javaVersions.add(v.getJava());
                            
                            final String packageName = v.getName().contains("/") ? v.getName().substring(0, v.getName().lastIndexOf('/')) : "";
                            if(packageCount.containsKey(packageName)){
                                packageCount.get(packageName).getAndIncrement();
                            }else{
                                packageCount.put(packageName, new AtomicInteger(1));
                            }
                        };

                    } else {
                        countProperly = v -> fileCount.put(extension, fileCount.getOrDefault(extension, 0) + 1);
                        readFileIfNeeded = (bytes, length) -> Objects.nonNull(bytes);
                    }
                    final byte[] bytes = new byte[2048];
                    try (InputStream in = jar.getInputStream(entry)) {
                        for (int length = in.read(bytes); length > 0; length = in.read(bytes)) {
                            for (MessageDigest digest : digests.values()) {
                                digest.update(bytes, 0, length);
                            }
                            readFileIfNeeded.accept(bytes, length);
                        }

                    } catch (IOException ex) {
                        Logger.getLogger(JarDetailsController.class.getName()).log(Level.SEVERE, "Unable to read " + entry, ex);
                    }
                    countProperly.accept(null);

                    if (entry.getCodeSigners() != null) {
                        codeSigners.addAll(Arrays.asList(entry.getCodeSigners()));
                    }
                    if (entry.getCertificates() != null) {
                        certificates.addAll(Arrays.asList(entry.getCertificates()));
                    }

                    final Attributes attributes = jarManifest.getAttributes(name);
                    checkSignatures(name, attributes, unsignedFiles, signedFiles, signedInvalidFiles);

                    digests.values().forEach(MessageDigest::reset);
                });
        packageCount.entrySet().stream().map(pCount -> new PackageCount(pCount.getKey(), pCount.getValue().get(), checkSeal(pCount.getKey(), jarManifest))).forEach(packageData::add);
        Platform.runLater(() -> {
            if (codeSigners.isEmpty() || certificates.isEmpty()) {
                updateSignatureNo();
            } else {
                updateSignatureYes(codeSigners, certificates, unsignedFiles, signedFiles, signedInvalidFiles);
            }
            fileCount.forEach((ext, count) -> languageChart.getData().add(new PieChart.Data(ext, count.doubleValue())));
            javaVersions.stream().max(ClassFileUtils::latestVersion).ifPresent(ver -> runsOn.setText(ver));
            if (jarFilesInJar.isEmpty()) {
                embeddedJars.getChildren().add(new Label("None"));
            } else {
                jarFilesInJar.stream().map(j -> new Label(j)).forEach(l -> embeddedJars.getChildren().add(l));
            }
            packageTable.setItems(packageData);
        });
    }

    private static void readClassBytes(ByteArrayOutputStream out, byte[] bytes, int length) {
        out.write(bytes, 0, length);
    }

    private static String byteToHex(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void checkSignatures(String name, Attributes attributes, SortedSet<String> unsignedFiles, SortedSet<String> signedFiles, SortedSet<String> signedInvalidFiles) {
        if (attributes == null) {
            unsignedFiles.add(name);
        }
        if (attributes != null) {
            final Optional<Entry<Object, Object>> optionalAttribute = attributes.entrySet().stream()
                    .filter(attribute -> digests.keySet().contains(String.valueOf(attribute.getKey())))
                    .findAny();
            if (optionalAttribute.isPresent()) {
                final Entry<Object, Object> attribute = optionalAttribute.get();
                final MessageDigest digest = digests.get(String.valueOf(attribute.getKey()));
                final byte[] digested = digest.digest();
                final String unconverted = new String(digested);
                final String attributeDecoded = new String(Base64.getDecoder().decode(String.valueOf(attribute.getValue())));
                if (unconverted.equals(attributeDecoded)) {
                    signedFiles.add(name);
                } else {
                    signedInvalidFiles.add(name);
                }
            } else {
                unsignedFiles.add(name);
            }
        }
    }

    private void flipDetails(Region newNode) {
        newNode.prefHeightProperty().bind(languageChart.heightProperty());
        newNode.prefWidthProperty().bind(languageChart.widthProperty());
        RotateTransition start = new RotateTransition(Duration.millis(750), detailPane);
        start.setAxis(Rotate.Y_AXIS);
        start.setFromAngle(0);
        start.setToAngle(90);
        start.setInterpolator(Interpolator.LINEAR);
        start.setOnFinished(event -> detailPane.setContent(newNode));

        RotateTransition end = new RotateTransition(Duration.millis(750), detailPane);
        end.setAxis(Rotate.Y_AXIS);
        end.setFromAngle(270);
        end.setToAngle(360);
        end.setInterpolator(Interpolator.LINEAR);

        final SequentialTransition sq = new SequentialTransition(start, end);
        sq.play();
    }

    private boolean checkSeal(String key, Manifest jarManifest) {
        if(jarManifest.getAttributes(key)==null){
            return false;
        }
        final Attributes attributes = jarManifest.getAttributes(key);
        final String s = String.valueOf(attributes.getOrDefault("sealed", "false"));
        return Boolean.valueOf(s);
    }
}
