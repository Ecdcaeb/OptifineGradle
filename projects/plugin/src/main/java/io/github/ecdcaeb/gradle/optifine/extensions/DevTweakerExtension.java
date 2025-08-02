package io.github.ecdcaeb.gradle.optifine.extensions;

import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.wrapper.Download;

public class DevTweakerExtension {
    private final Property<String> distribution;

    public DevTweakerExtension(Project project, ObjectFactory objects) {
        this.distribution = objects.property(String.class);
        this.distribution.set("https://github.com/OpenCubicChunks/OptiFineDevTweaker/releases/download/2.6.15/aa_do_not_rename_OptiFineDevTweaker-2.6.15-all.jar");
    }

    public Property<String> getDistribution() {
        return distribution;
    }
}
