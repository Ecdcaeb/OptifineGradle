OptifineGradle is a gradle plugin that handles optifine dependencies.
Include deobf the optifine and apply the patches to the dev environment.

Unless you use `optifine#rawJar` and are working in a notch environment, you will need to use a Minecraft Development Gradle anyway. Examples include ForgeGradle, RetroFutureGradle, Unimined, NeoGradle, ModDevGradle, FabricLoom, etc.

Work is in its early stages, and the aforementioned MC Dev Gradle support isn't yet implemented. However, the basic framework is in place, and the next step is to implement each one individually.

We're currently do Unimined Impl.


## OptifineGradle

OptifineGradle is a gradle plugin that handles optifine dependencies.
Include deobf the optifine and apply the patches to the dev environment.

### Usage

```kts
optifine {
    distribution.set("https://optifine_third_party_publishing.org/1.12.2/HD-U5.jar")
    display.set("net.optifine:optifine:1.12.2-HD-U5") // a nice name
    minecraftEnvironment.set(MinecraftEnvironment.UNIMINED)
}

dependencies {
    implementation(optifine.runDevJar())
}
```
