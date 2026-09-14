package exam_management_syatem.packaging;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Deterministic, standards-compliant JAR manifest generator.
 * Uses Java's official Manifest API to handle RFC-compliant line folding for Class-Path.
 */
public class GenerateManifest {

    public static void main(String[] args) {
        try {
            String libDirPath = args.length > 0 ? args[0] : "dist/lib";
            String outputPath = args.length > 1 ? args[1] : "target/MANIFEST.MF";
            String version = args.length > 2 ? args[2] : "2.0.6";

            File libDir = new File(libDirPath);
            File[] jars = libDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
            if (jars == null) {
                jars = new File[0];
            }
            Arrays.sort(jars, Comparator.comparing(File::getName));

            StringBuilder cp = new StringBuilder();
            for (File j : jars) {
                if (cp.length() > 0) {
                    cp.append(" ");
                }
                cp.append("lib/").append(j.getName());
            }

            Manifest manifest = new Manifest();
            Attributes attr = manifest.getMainAttributes();
            attr.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            attr.put(Attributes.Name.MAIN_CLASS, "exam_management_syatem.app.Main");
            attr.put(new Attributes.Name("Class-Path"), cp.toString());
            attr.put(new Attributes.Name("Implementation-Title"), "Exam Management System");
            attr.put(new Attributes.Name("Implementation-Version"), version);

            Path out = Paths.get(outputPath);
            if (out.getParent() != null) {
                Files.createDirectories(out.getParent());
            }

            try (OutputStream fos = Files.newOutputStream(out)) {
                manifest.write(fos);
            }

            System.out.println("[GenerateManifest] Successfully generated " + outputPath +
                    " with " + jars.length + " classpath dependencies.");
        } catch (Exception e) {
            System.err.println("[GenerateManifest] Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
