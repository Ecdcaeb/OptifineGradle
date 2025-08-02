package io.github.ecdcaeb.gradle.optifine.utils;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class DecompilerUtil {
    private static final Map<String, Object> preference = IFernflowerPreferences.getDefaults();

    static {
        preference.put(IFernflowerPreferences.DUMP_CODE_LINES, "1");
    }
    public static void decompileJar(List<File> libs, Path input, Path output) {
        try (ConsoleDecompiler consoleDecompiler = new ConsoleDecompiler(output.toFile(), preference, new IFernflowerLogger() {
            @Override
            public void writeMessage(String s, Severity severity) {
                System.out.println("[OptifineGradle]" + severity.prefix + s);
            }

            @Override
            public void writeMessage(String s, Severity severity, Throwable throwable) {
                System.out.println("[OptifineGradle]" + severity.prefix + s);
                throwable.printStackTrace(System.out);
            }
        }){}){
            for (File lib : libs) {
                consoleDecompiler.addLibrary(lib);
            }
            consoleDecompiler.addSource(input.toFile());
            consoleDecompiler.decompileContext();
        } catch (IOException ioException){
            try {
                Files.deleteIfExists(output);
            } catch (IOException ignored) {
            }
            System.out.println("[OptifineGradle]ERROR : Decompile Failed for " + input);
        }
    }
}
