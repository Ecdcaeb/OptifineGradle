package io.github.ecdcaeb.gradle.optifine.extensions;

import io.github.ecdcaeb.gradle.optifine.api.IMinecraftEnvironment;
import io.github.ecdcaeb.gradle.optifine.api.MinecraftEnvironment;
import io.github.ecdcaeb.gradle.optifine.tasks.AbstractDownloadTask;
import io.github.ecdcaeb.gradle.optifine.tasks.SourceGenerationTask;
import io.github.ecdcaeb.gradle.optifine.tasks.DeobfuscateOptifineTask;
import io.github.ecdcaeb.gradle.optifine.utils.DecompilerUtil;
import io.github.ecdcaeb.gradle.optifine.utils.HashUtil;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;

public class OptifineExtension {
    private final Project project;
    private final ObjectFactory objects;

    private final Property<String> distribution;
    private final Property<String> display;
    private final Property<IMinecraftEnvironment> minecraftEnvironment;
    private final DevTweakerExtension devTweaker;
    private final RenamingExtension mapping;

    public OptifineExtension(Project project, ObjectFactory objects) {
        this.project = project;
        this.objects = objects;

        this.distribution = objects.property(String.class);
        this.display = objects.property(String.class);
        this.minecraftEnvironment = objects.property(IMinecraftEnvironment.class);
        this.minecraftEnvironment.set(MinecraftEnvironment.NONE);
        this.devTweaker = objects.newInstance(DevTweakerExtension.class);
        this.mapping = objects.newInstance(RenamingExtension.class);
    }

    public Property<String> getDistribution() {
        return distribution;
    }

    public Property<String> getDisplay() {
        return display;
    }

    public Property<IMinecraftEnvironment> getMinecraftEnvironment() {
        return minecraftEnvironment;
    }

    public DevTweakerExtension getDevTweaker() {
        return devTweaker;
    }

    public RenamingExtension getMapping() {
        return mapping;
    }

    private String getVersionWithHash() {
        String[] parts = getDisplay().get().split(":");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid display format. Expected 'group:name:version'");
        }
        return parts[2] + "-" + HashUtil.sha256(getDistribution().get());
    }

    public String getCachePath(String classifier) {
        String[] parts = getDisplay().get().split(":");
        String group = parts[0].replace('.', '/');
        String name = parts[1];
        String version = getVersionWithHash();

        return group + "/" + name + "/" + version + "/" +
                name + "-" + version + (classifier != null ? "-" + classifier : "") + ".jar";
    }

    public File getCacheFile(String classifier) {
        return new File(project.getGradle().getGradleUserHomeDir(),
                "caches/optifine_gradle/" + getCachePath(classifier));
    }

    private FileCollection deobfuscatedJar(IMinecraftEnvironment.MappingName outputMapping,
                                           boolean generateSources) {
        String taskName = String.format("Optifine_deobfuscate_from_%s_to_%s", IMinecraftEnvironment.MappingName.OBF.name(), outputMapping.name());

        TaskProvider<DeobfuscateOptifineTask> deobfuscationTask = project.getTasks().register(
                taskName,
                DeobfuscateOptifineTask.class,
                task -> {
                    task.getInputFile().set(project.getTasks().named("downloadOptifine", AbstractDownloadTask.class)
                            .flatMap(AbstractDownloadTask::getOutputFile));
                    task.getOutputFile().set(getCacheFile(outputMapping.name()));
                    task.getInputMapping().set(IMinecraftEnvironment.MappingName.OBF);
                    task.getOutputMapping().set(outputMapping);
                }
        );

        deobfuscationTask.configure(task -> task.dependsOn("downloadOptifine"));

        if (!generateSources) {
            return project.files(deobfuscationTask.flatMap(DeobfuscateOptifineTask::getOutputFile));
        }

        String sourceTaskName = "Optifine_genSource_" + outputMapping.name();

        TaskProvider<SourceGenerationTask> sourceTask = project.getTasks().register(
                sourceTaskName,
                SourceGenerationTask.class,
                task -> {
                    task.getInputFile().set(deobfuscationTask.flatMap(DeobfuscateOptifineTask::getOutputFile));
                    task.getOutputFile().set(getCacheFile(outputMapping.name() + "-sources"));
                }
        );

        sourceTask.configure(task -> task.dependsOn(deobfuscationTask));

        return project.files(
                deobfuscationTask.flatMap(DeobfuscateOptifineTask::getOutputFile),
                sourceTask.flatMap(SourceGenerationTask::getOutputFile)
        );
    }

    public FileCollection rawJar() {
        return rawJar(false);
    }

    public FileCollection rawJar(boolean generateSources) {
        TaskProvider<AbstractDownloadTask> downloadTask = project.getTasks().named(
                "downloadOptifine",
                AbstractDownloadTask.class
        );

        if (!generateSources) {
            return project.files(downloadTask.flatMap(AbstractDownloadTask::getOutputFile));
        }

        TaskProvider<SourceGenerationTask> sourceTask = project.getTasks().register(
                "generateRawSources",
                SourceGenerationTask.class,
                task -> {
                    task.getInputFile().set(downloadTask.flatMap(AbstractDownloadTask::getOutputFile));
                    task.getOutputFile().set(getCacheFile("sources"));
                }
        );

        sourceTask.configure(task -> task.dependsOn(downloadTask));

        return project.files(
                downloadTask.flatMap(AbstractDownloadTask::getOutputFile),
                sourceTask.flatMap(SourceGenerationTask::getOutputFile)
        );
    }

    public FileCollection srgJar() {
        return srgJar(false);
    }

    public FileCollection srgJar(boolean generateSources) {
        return deobfuscatedJar(IMinecraftEnvironment.MappingName.SRG, generateSources);
    }

    public FileCollection mcpJar() {
        return mcpJar(false);
    }

    public FileCollection mcpJar(boolean generateSources) {
        return deobfuscatedJar(IMinecraftEnvironment.MappingName.MCP, generateSources);
    }

    public FileCollection devTweaker() {
        TaskProvider<AbstractDownloadTask> downloadDevTweakerTask = project.getTasks().register(
                "downloadOptifineDevTweaker",
                AbstractDownloadTask.class,
                task -> {
                    OptifineExtension extension = project.getExtensions().getByType(OptifineExtension.class);
                    task.getUrl().set(extension.getDevTweaker().getDistribution());
                    task.getOutputFile().set(extension.getCacheFile("devTweaker-" + HashUtil.sha256(extension.getDevTweaker().getDistribution().get())));
                    task.getTag().set("OptifineDevTweaker");
                }
        );
        return project.files(downloadDevTweakerTask.flatMap(AbstractDownloadTask::getOutputFile));
    }
}
