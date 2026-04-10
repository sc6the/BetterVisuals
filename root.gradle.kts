plugins {
    kotlin("jvm") version "1.9.10" apply false
    id("org.polyfrost.multi-version.root")
}

preprocess {
    "1.8.9-forge"(10809, "srg")
}
