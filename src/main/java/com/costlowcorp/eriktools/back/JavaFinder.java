package com.costlowcorp.eriktools.back;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * User: ecostlow <erik.costlow@oracle.com>
 * Date: 8/7/13
 * Time: 11:16 AM
 */
public class JavaFinder {
    private final String HOME = System.getProperty("java.home");

    /**
     *
     * @return List of Java installations on the system
     */
    public SortedSet<String> findJavaInstallations(){
        final SortedSet<String> javaLocations = new TreeSet<>((o1, o2) -> {
            if(o1.equals(o2)){
                return 0;
            }else if(o1.equals(HOME)){
                return -1;
            }else{
                return o1.compareTo(o2);
            }
        });

        final String home = HOME;
        javaLocations.add(home);

        final Path loc = Paths.get(home);
        final Path parent = loc.getParent().getFileName().toString().startsWith("jdk") ? loc.getParent().getParent() : loc.getParent();

        try(final DirectoryStream<Path> dir = Files.newDirectoryStream(parent)){
            for(Path file : dir){
                final String filename = file.getFileName().toString();
                if(file.toFile().isDirectory()){
                    if(filename.startsWith("jdk")){
                        javaLocations.add(file.resolve("jre").toString());
                    }else if(filename.startsWith("jre")){
                        javaLocations.add(file.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return javaLocations;
    }

    /**
     *
     * @return Directory where user-specific information is stored.
     */
    public Path findUserSpecificJavaConfigurationArea(){
        final Path path;
        final String os = System.getProperty("os.name");
        //final String osVersion = System.getProperty("os.version");
        final String userHome = System.getProperty("user.home");
        if(String.valueOf(os).startsWith("Windows")){
            path = Paths.get(userHome, "AppData", "LocalLow", "Sun", "Java", "Deployment", "Security");
        }else{
            path = Paths.get(userHome, ".java", "Sun", "Java", "Deployment", "Security");
        }
        return path;
    }
}
