rootProject.name = "OptifineGradle"

include (":plugin", ":tests")

project(":plugin").projectDir = file("projects/plugin")
project(":tests").projectDir = file("projects/tests")

