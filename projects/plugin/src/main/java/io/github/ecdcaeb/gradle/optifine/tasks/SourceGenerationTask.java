package io.github.ecdcaeb.gradle.optifine.tasks;

import io.github.ecdcaeb.gradle.optifine.extensions.OptifineExtension;
import io.github.ecdcaeb.gradle.optifine.utils.DecompilerUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.BiConsumer;

public class SourceGenerationTask extends DefaultTask {

    private final RegularFileProperty inputFile;
    private final RegularFileProperty outputFile;

    public SourceGenerationTask() {
        this.inputFile = getProject().getObjects().fileProperty();
        this.outputFile = getProject().getObjects().fileProperty();
    }

    @InputFile
    public RegularFileProperty getInputFile() {
        return inputFile;
    }

    @OutputFile
    public RegularFileProperty getOutputFile() {
        return outputFile;
    }

    @TaskAction
    public void generateSources() throws IOException {
        Path input = getInputFile().get().getAsFile().toPath();
        Path output = getOutputFile().get().getAsFile().toPath();

        if (!Files.exists(input)) {
            throw new IOException("Input file not found: " + input);
        }

        Files.createDirectories(output.getParent());

        if (Files.exists(output) && Files.getLastModifiedTime(output)
                .compareTo(Files.getLastModifiedTime(input)) >= 0) {
            getLogger().lifecycle("Using cached sources: {}", output);
            return;
        }

        getLogger().lifecycle("Generating sources: {} -> {}", input, output);

        Path tempFile = Files.createTempFile(output.getParent(), "sources", ".tmp");

        try {

            DecompilerUtil.decompileJar(this.getProject().getExtensions().getByType(OptifineExtension.class).getMinecraftEnvironment()
                    .get().getMinecraftClient(this.getProject()), input, tempFile);
            //generateFunction.accept(input, tempFile);

            if (!Files.exists(tempFile) || Files.size(tempFile) == 0) {
                throw new IOException("Source generation failed: Output file is empty or missing");
            }

            Files.move(tempFile, output, StandardCopyOption.ATOMIC_MOVE);
            getLogger().lifecycle("Source generation complete: {}", output);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
