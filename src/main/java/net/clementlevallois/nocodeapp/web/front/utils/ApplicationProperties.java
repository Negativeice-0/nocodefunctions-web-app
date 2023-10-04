/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.nocodeapp.web.front.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LEVALLOIS
 */
public class ApplicationProperties {

    private static Properties privateProperties;
    private static Path rootProjectPath;
    private static Path i18nStaticResourcesFullPath;
    private static Path userGeneratedVosviewerPublicDirectoryFullPath;
    private static Path userGeneratedVosviewerPrivateDirectoryFullPath;
    private static Path userGeneratedGephistoPublicDirectoryFullPath;
    private static Path userGeneratedGephistoPrivateDirectoryFullPath;
    private static Path gephistoRootRelativePath;
    private static Path vosviewerRootRelativePath;
    private static Path gephistoRootFullPath;
    private static Path vosviewerRootFullPath;

    private static final String ENV_VARIABLE_ROOTPROJECT = "root.project";
    private static final String ENV_VARIABLE_PROPERTIES_FILE = "properties.relative.path.and.filename";
    private static final String ENV_VARIABLE_I18N_DIR = "i18n.relative.path";
    private static final String ENV_VARIABLE_VOSVIEWER_DIR = "relative.path.vosviewer";
    private static final String ENV_VARIABLE_GEPHISTO_DIR = "relative.path.gephisto";
    private static final String ENV_VARIABLE_PUBLIC_DIR = "relative.path.public";
    private static final String ENV_VARIABLE_PRIVATE_DIR = "relative.path.private";
    private static final String ENV_VARIABLE_USER_CREATED_FILES_DIR = "relative.path.user.created.files";

    public static void load() {
        loadEnvironmentVariablesOnWindows();
        rootProjectPath = loadRootProjectPath();
        privateProperties = loadPrivateProperties();
        i18nStaticResourcesFullPath = loadI18nStaticResourcesFullPath();
        userGeneratedVosviewerPublicDirectoryFullPath = loadVosviewerPublicFullPath();
        userGeneratedVosviewerPrivateDirectoryFullPath = loadVosviewerPrivatePath();
        userGeneratedGephistoPublicDirectoryFullPath = loadGephistoPublicFullPath();
        userGeneratedGephistoPrivateDirectoryFullPath = loadGephistoPrivateFullPath();
        gephistoRootRelativePath = loadGephistoRootRelativePath();
        vosviewerRootRelativePath = loadVosviewerRootRelativePath();
        vosviewerRootFullPath = loadVosviewerRootFullPath();
        gephistoRootFullPath = loadGephistoRootFullPath();
    }

    private static Path loadRootProjectPath() {
        String rootProjectProperty = System.getProperty(ENV_VARIABLE_ROOTPROJECT);
        Path rootPath = null;
        if (rootProjectProperty == null || rootProjectProperty.isBlank()) {
            System.out.println("system property for root project path not correctly loaded");
            System.out.println("you need to add --systemproperties sys.properties in the command launching the app");
            System.out.println("where sys.properties is a text file in the same directory as the Payara server or any server you use");
            System.out.println("EXITING NOW because without these properties, the app can't function");
            System.exit(-1);
        } else {
            rootPath = Path.of(rootProjectProperty);
            if (Files.isDirectory(rootPath)) {
                return rootPath;
            } else {
                System.out.println("root folder loaded from env variables does not exist");
                System.out.println("path is: " + rootProjectProperty);
                System.out.println("EXITING NOW because without these properties, the app can't function");
                System.exit(-1);
            }
        }
        return rootPath;
    }

    private static Properties loadPrivateProperties() {
        Path privatePropsFilePath = null;
        Properties props = null;
        String privatePropsFilePathAsString = System.getProperty(ENV_VARIABLE_PROPERTIES_FILE);
        if (privatePropsFilePathAsString == null || privatePropsFilePathAsString.isBlank()) {
            System.out.println("system property for properties file relative path not correctly loaded");
            System.out.println("you need to add --systemproperties sys.properties in the command launching the app");
            System.out.println("where sys.properties is a text file in the same directory as the Payara server or any server you use");
            System.out.println("EXITING NOW because without these properties, the app can't function");
            System.exit(-1);
        } else {
            privatePropsFilePath = rootProjectPath.resolve(Path.of(privatePropsFilePathAsString));
            if (!Files.exists(privatePropsFilePath)) {
                System.out.println("private properties file path loaded from env variables does not exist");
                System.out.println("path is: " + privatePropsFilePath.toString());
                System.out.println("EXITING NOW because without these properties, the app can't function");
                System.exit(-1);
            }
        }
        try (InputStream is = new FileInputStream(privatePropsFilePath.toFile())) {
            props = new Properties();
            props.load(is);
        } catch (IOException ex) {
            Logger.getLogger(ApplicationProperties.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("could not open the file for private properties");
            System.out.println("path is: " + privatePropsFilePath.toString());
            System.out.println("EXITING NOW because without these properties, the app can't function");
            System.exit(-1);
        }
        return props;

    }

    private static Path loadI18nStaticResourcesFullPath() {
        Path i18nResourcesFullPath = null;
        String i18nStaticResourcesRelativePath = System.getProperty(ENV_VARIABLE_I18N_DIR);
        if (i18nStaticResourcesRelativePath == null || i18nStaticResourcesRelativePath.isBlank()) {
            System.out.println("system property for i18n static resources relative path not correctly loaded");
            System.out.println("you need to add --systemproperties sys.properties in the command launching the app");
            System.out.println("where sys.properties is a text file in the same directory as the Payara server or any server you use");
            System.out.println("EXITING NOW because without these properties, the app can't function");
            System.exit(-1);
        } else {
            i18nResourcesFullPath = rootProjectPath.resolve(Path.of(i18nStaticResourcesRelativePath));
            if (Files.isDirectory(i18nResourcesFullPath)) {
                return i18nResourcesFullPath;
            } else {
                System.out.println("directory for i18n static resources loaded from env variables does not exist");
                System.out.println("path is: " + i18nResourcesFullPath.toString());
                System.out.println("EXITING NOW because without these properties, the app can't function");
                System.exit(-1);
            }
        }
        return i18nResourcesFullPath;
    }

    public static Properties getPrivateProperties() {
        return privateProperties;
    }

    public static Path getExternalFolderForInternationalizationFiles() {
        return i18nStaticResourcesFullPath;
    }

    private static Path loadVosviewerPrivatePath() {
        String ug = System.getProperty(ENV_VARIABLE_USER_CREATED_FILES_DIR);
        String vv = System.getProperty(ENV_VARIABLE_VOSVIEWER_DIR);
        String privateFolder = System.getProperty(ENV_VARIABLE_PRIVATE_DIR);
        return rootProjectPath.resolve(Path.of(ug)).resolve(Path.of(vv)).resolve(Path.of(privateFolder));
    }

    private static Path loadVosviewerPublicFullPath() {
        String ug = System.getProperty(ENV_VARIABLE_USER_CREATED_FILES_DIR);
        String vv = System.getProperty(ENV_VARIABLE_VOSVIEWER_DIR);
        String publicFolder = System.getProperty(ENV_VARIABLE_PUBLIC_DIR);
        return rootProjectPath.resolve(Path.of(ug)).resolve(Path.of(vv)).resolve(Path.of(publicFolder));
    }

    private static Path loadGephistoPublicFullPath() {
        String ug = System.getProperty(ENV_VARIABLE_USER_CREATED_FILES_DIR);
        String gephisto = System.getProperty(ENV_VARIABLE_GEPHISTO_DIR);
        String publicFolder = System.getProperty(ENV_VARIABLE_PUBLIC_DIR);
        return rootProjectPath.resolve(Path.of(ug)).resolve(Path.of(gephisto)).resolve(Path.of(publicFolder));
    }

    private static Path loadGephistoPrivateFullPath() {
        String ug = System.getProperty(ENV_VARIABLE_USER_CREATED_FILES_DIR);
        String gephisto = System.getProperty(ENV_VARIABLE_GEPHISTO_DIR);
        String privateFolder = System.getProperty(ENV_VARIABLE_PRIVATE_DIR);
        return rootProjectPath.resolve(Path.of(ug)).resolve(Path.of(gephisto)).resolve(Path.of(privateFolder));
    }

    private static Path loadGephistoRootRelativePath() {
        String gephisto = System.getProperty(ENV_VARIABLE_GEPHISTO_DIR);
        return Path.of(gephisto);
    }

    private static Path loadVosviewerRootRelativePath() {
        String vosviewer = System.getProperty(ENV_VARIABLE_VOSVIEWER_DIR);
        return Path.of(vosviewer);
    }

    private static Path loadGephistoRootFullPath() {
        String ug = System.getProperty(ENV_VARIABLE_USER_CREATED_FILES_DIR);
        String gephisto = System.getProperty(ENV_VARIABLE_GEPHISTO_DIR);
        return rootProjectPath.resolve(Path.of(ug)).resolve(Path.of(gephisto));
    }

    private static Path loadVosviewerRootFullPath() {
        String ug = System.getProperty(ENV_VARIABLE_USER_CREATED_FILES_DIR);
        String vosviewer = System.getProperty(ENV_VARIABLE_VOSVIEWER_DIR);
        return rootProjectPath.resolve(Path.of(ug)).resolve(Path.of(vosviewer));
    }

    public static Path getUserGeneratedVosviewerPublicDirectoryFullPath() {
        return userGeneratedVosviewerPublicDirectoryFullPath;
    }

    public static Path getUserGeneratedVosviewerPrivateDirectoryFullPath() {
        return userGeneratedVosviewerPrivateDirectoryFullPath;
    }

    public static Path getUserGeneratedGephistoPublicDirectoryFullPath() {
        return userGeneratedGephistoPublicDirectoryFullPath;
    }

    public static Path getUserGeneratedGephistoPrivateDirectoryFullPath() {
        return userGeneratedGephistoPrivateDirectoryFullPath;
    }

    public static Path getRootProjectFullPath() {
        return rootProjectPath;
    }

    public static Path getI18nStaticResourcesFullPath() {
        return i18nStaticResourcesFullPath;
    }

    public static Path getGephistoRootRelativePath() {
        return gephistoRootRelativePath;
    }

    public static Path getVosviewerRootRelativePath() {
        return vosviewerRootRelativePath;
    }

    public static Path getGephistoRootFullPath() {
        return gephistoRootFullPath;
    }

    public static Path getVosviewerRootFullPath() {
        return vosviewerRootFullPath;
    }

    private static void loadEnvironmentVariablesOnWindows() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return;
        }
        List<String> vars = null;
        String currentWorkingDirectory = System.getProperty("user.dir");
        System.out.println("working dir: "+ currentWorkingDirectory);
        try {
            vars = Files.readAllLines(Path.of("sys.properties"), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.out.println("running on windows, could not find the file sys.properties containing all environment variablesl");
            Logger.getLogger(ApplicationProperties.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (String line : vars) {
            if (line.startsWith("#")){
                continue;
            }
            String[] fields = line.split("=");
            System.setProperty(fields[0], fields[1]);
        }
    }
}
