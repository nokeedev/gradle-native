plugins {
	id("dev.nokee.swift-application")									// <1>
}

application {
	swiftSources.setFrom(fileTree("srcs") { include("**/*.swift") })	// <2>
}
