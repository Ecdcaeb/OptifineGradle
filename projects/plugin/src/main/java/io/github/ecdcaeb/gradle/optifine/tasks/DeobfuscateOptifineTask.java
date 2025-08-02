package io.github.ecdcaeb.gradle.optifine.tasks;

import io.github.ecdcaeb.gradle.optifine.api.IMinecraftEnvironment;
import io.github.ecdcaeb.gradle.optifine.extensions.OptifineExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DeobfuscateOptifineTask extends DefaultTask {

    private Property<IMinecraftEnvironment.MappingName> inputMapping;
    private Property<IMinecraftEnvironment.MappingName> outputMapping;
    private final RegularFileProperty inputFile;
    private final RegularFileProperty outputFile;

    public DeobfuscateOptifineTask() {
        this.inputFile = getProject().getObjects().fileProperty();
        this.outputFile = getProject().getObjects().fileProperty();
        this.inputMapping = getProject().getObjects().property(IMinecraftEnvironment.MappingName.class);
        this.outputMapping = getProject().getObjects().property(IMinecraftEnvironment.MappingName.class);
    }

    @InputFile
    public RegularFileProperty getInputFile() {
        return inputFile;
    }

    @OutputFile
    public RegularFileProperty getOutputFile() {
        return outputFile;
    }

    @Input
    public Property<IMinecraftEnvironment.MappingName> getInputMapping() {
        return inputMapping;
    }

    @Input
    public Property<IMinecraftEnvironment.MappingName> getOutputMapping() {
        return outputMapping;
    }

    @TaskAction
    public void transform() throws IOException {
        Path input = getInputFile().get().getAsFile().toPath();
        Path output = getOutputFile().get().getAsFile().toPath();

        if (!Files.exists(input)) {
            throw new IOException("Input file not found: " + input);
        }

        Files.createDirectories(output.getParent());

        if (Files.exists(output) && Files.getLastModifiedTime(output)
                .compareTo(Files.getLastModifiedTime(input)) >= 0) {
            getLogger().lifecycle("Using cached transformation: {}", output);
            return;
        }

        getLogger().lifecycle("Transforming Optifine: {} -> {}", input, output);

        Path tempFile = Files.createTempFile(output.getParent(), "transform", ".tmp");

        try {
            IMinecraftEnvironment minecraftEnvironment = this.getProject().getExtensions().getByType(OptifineExtension.class).getMinecraftEnvironment().get();

            minecraftEnvironment.deobfJar(this.getProject(), input, tempFile, inputMapping.get(), outputMapping.get());

            if (!Files.exists(tempFile) || Files.size(tempFile) == 0) {
                throw new IOException("Transformation failed: Output file is empty or missing");
            }

            Files.move(tempFile, output, StandardCopyOption.ATOMIC_MOVE);
            getLogger().lifecycle("Transformation complete: {}", output);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
