package io.github.ecdcaeb.gradle.optifine.api;

import io.github.ecdcaeb.gradle.optifine.extensions.OptifineExtension;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface IMinecraftEnvironment {
    default List<File> getMinecraftClient(Project project) {
        throw new AbstractMethodError("NONE! MinecraftEnvironment not Implemented");
    }
    default void deobfJar(Project project, Path input, Path output, MappingName inputMapping, MappingName outputMapping) {
        throw new AbstractMethodError("NONE! MinecraftEnvironment not Implemented");
    }

    enum MappingName {
        OBF {
            @Override
            public String getMappingName(Project project) {
                return project.getExtensions().getByType(OptifineExtension.class)
                        .getMapping().getObfMappingName().get();
            }
        },
        SRG{
            @Override
            public String getMappingName(Project project) {
                return project.getExtensions().getByType(OptifineExtension.class)
                        .getMapping().getSrgMappingName().get();
            }
        },
        MCP{
            @Override
            public String getMappingName(Project project) {
                return project.getExtensions().getByType(OptifineExtension.class)
                        .getMapping().getMcpMappingName().get();
            }
        };

        public abstract String getMappingName(Project project);
    }
}
