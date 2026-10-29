package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogsCenterTest {
    @TempDir
    private Path tempDir;

    @Test
    public void packagedJarInPercentDirectory_writesLogBesideJar() throws Exception {
        Path appHome = Files.createDirectory(tempDir.resolve("%g"));
        Path jar = appHome.resolve("probe.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            addClass(output, AppPaths.class);
            addClass(output, LogsCenter.class);
            addClass(output, LogsCenterProbe.class);
        }

        Path java = Path.of(System.getProperty("java.home"), "bin", "java");
        Process process = new ProcessBuilder(java.toString(), "-cp", jar.toString(),
                LogsCenterProbe.class.getName()).redirectErrorStream(true).start();
        assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Log probe timed out");
        assertEquals(0, process.exitValue(), new String(process.getInputStream().readAllBytes()));
        try (var files = Files.list(appHome)) {
            assertTrue(files.anyMatch(path -> path.getFileName().toString().startsWith("addressbook.log")));
        }
        assertFalse(Files.exists(tempDir.resolve("0")));
    }

    private static void addClass(JarOutputStream output, Class<?> clazz) throws IOException {
        String resource = clazz.getName().replace('.', '/') + ".class";
        output.putNextEntry(new JarEntry(resource));
        try (InputStream input = clazz.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IOException("Missing class resource: " + resource);
            }
            input.transferTo(output);
        }
        output.closeEntry();
    }
}
