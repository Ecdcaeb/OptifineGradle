package io.github.ecdcaeb.gradle.optifine.tasks;

import io.github.ecdcaeb.gradle.optifine.utils.NetworkUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class AbstractDownloadTask extends DefaultTask {

    @Input
    public abstract Property<String> getTag();

    @Input
    public abstract Property<String> getUrl();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void download() throws Exception {
        String url = NetworkUtil.redirectUrl(getUrl().get());
        Path outputPath = getOutputFile().get().getAsFile().toPath();

        Files.createDirectories(outputPath.getParent());

        if (Files.exists(outputPath) && Files.size(outputPath) > 0) {
            getLogger().lifecycle("Using cached {}: {}", this.getTag(), outputPath);
            return;
        }

        getLogger().lifecycle("Downloading {} from: {}", this.getTag(), url);

        Path tempFile = Files.createTempFile(outputPath.getParent(), "download", ".tmp");

        try {
            URL downloadUrl = new URL(url);
            try (BufferedInputStream in = new BufferedInputStream(downloadUrl.openStream());
                 FileOutputStream out = new FileOutputStream(tempFile.toFile())) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            if (Files.size(tempFile) == 0) {
                throw new IOException("Download failed: File is empty");
            }

            Files.move(tempFile, outputPath, StandardCopyOption.ATOMIC_MOVE);
            getLogger().lifecycle("Downloaded {} to: {}", getTag(), outputPath);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

}
