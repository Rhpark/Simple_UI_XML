plugins {
    base
}

tasks.register("consumerSmokeCheck") {
    group = "verification"
    description = "Compiles one independent consumer module per locally published Maven artifact"
    dependsOn(
        ":maven_consumer_smoke:core:compileDebugKotlin",
        ":maven_consumer_smoke:xml:compileDebugKotlin",
        ":maven_consumer_smoke:compose:compileDebugKotlin",
        ":maven_consumer_smoke:system_manager:compileDebugKotlin",
    )
}
