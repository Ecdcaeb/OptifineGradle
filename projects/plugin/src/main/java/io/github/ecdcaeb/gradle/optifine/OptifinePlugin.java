package io.github.ecdcaeb.gradle.optifine;

import io.github.ecdcaeb.gradle.optifine.extensions.OptifineExtension;
import io.github.ecdcaeb.gradle.optifine.tasks.AbstractDownloadTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

@SuppressWarnings("unused")
public class OptifinePlugin implements Plugin<Project> {
    private final ObjectFactory objects;

    @Inject
    public OptifinePlugin(ObjectFactory objects) {
        this.objects = objects;
    }

    @Override
    public void apply(Project project) {
        final OptifineExtension extension = project.getExtensions().create(
                "optifine",
                OptifineExtension.class,
                project,
                objects
        );

        TaskProvider<AbstractDownloadTask> downloadOptifineTask = project.getTasks().register(
                "downloadOptifine",
                AbstractDownloadTask.class,
                task -> {
                    task.getUrl().set(extension.getDistribution());
                    task.getOutputFile().set(extension.getCacheFile(null));
                    task.getTag().set("Optifine");
                }
        );

        Configuration optifineConfig = project.getConfigurations().create("optifine");
        optifineConfig.getOutgoing().artifact(downloadOptifineTask.flatMap(AbstractDownloadTask::getOutputFile));
    }
}
