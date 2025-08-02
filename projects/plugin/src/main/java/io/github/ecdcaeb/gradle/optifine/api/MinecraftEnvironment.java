package io.github.ecdcaeb.gradle.optifine.api;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import xyz.wagyourtail.unimined.api.UniminedExtension;
import xyz.wagyourtail.unimined.api.minecraft.MinecraftConfig;
import xyz.wagyourtail.unimined.internal.minecraft.MinecraftProvider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xyz.wagyourtail.unimined.internal.mods.ModsProvider;

public enum MinecraftEnvironment implements IMinecraftEnvironment {
    NONE(),
    UNIMINED(){
        @Override
        public List<File> getMinecraftClient(Project project) {
            LinkedList<File> linkedList = new LinkedList<>();
            for (Map.Entry<SourceSet, MinecraftConfig> entry : project.getExtensions().getByType(UniminedExtension.class).getMinecrafts().entrySet()) {
                linkedList.add(entry.getValue().getMinecraftData().getOfficialClientMappingsFile());
            }
            return linkedList;
        }

        @Override
        public void deobfJar(Project project, Path input, Path output, MappingName inputMapping, MappingName outputMapping) {
            for (Map.Entry<SourceSet, MinecraftConfig> entry : project.getExtensions().getByType(UniminedExtension.class).getMinecrafts().entrySet()) {
                if (entry.getValue() instanceof MinecraftProvider minecraftProvider) {
                    try {
                        Files.copy(
                                getClasspathAs(project, minecraftProvider.getMods(), outputMapping, input).iterator().next().toPath(),
                                output,
                                StandardCopyOption.REPLACE_EXISTING
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        }

        static Constructor<?> constructor_namespace;
        static Method getClasspathAs;
        static {
            try {
                constructor_namespace = Class.forName("xyz.wagyourtail.unimined.mapping.Namespace")
                        .getDeclaredConstructor(String.class);

                constructor_namespace.setAccessible(true);
                getClasspathAs = ModsProvider.class.getDeclaredMethod("getClasspathAs");
                getClasspathAs.setAccessible(true);
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        private static Set<File> getClasspathAs(Project project, ModsProvider modsProvider, MappingName outputMapping, Path input) {
            try {
                return (Set<File>)getClasspathAs.invoke(modsProvider, constructor_namespace.newInstance(outputMapping.getMappingName(project)), Set.of(input.toFile()));
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
