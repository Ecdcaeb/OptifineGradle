package io.github.ecdcaeb.gradle.optifine.extensions;

import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class RenamingExtension {
    private final Property<String> obfMappingName;
    private final Property<String> srgMappingName;
    private final Property<String> mcpMappingName;

    public RenamingExtension(Project project, ObjectFactory objects) {
        this.mcpMappingName = objects.property(String.class);
        this.srgMappingName = objects.property(String.class);
        this.obfMappingName = objects.property(String.class);

        this.getMcpMappingName().set("mcp");
        this.getObfMappingName().set("notch");
        this.getSrgMappingName().set("srg");
    }

    public Property<String> getMcpMappingName() {
        return mcpMappingName;
    }

    public Property<String> getObfMappingName() {
        return obfMappingName;
    }

    public Property<String> getSrgMappingName() {
        return srgMappingName;
    }
}
