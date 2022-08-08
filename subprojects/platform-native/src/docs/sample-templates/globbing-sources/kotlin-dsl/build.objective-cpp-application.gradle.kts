plugins {
	id("dev.nokee.objective-cpp-application")								// <1>
}

application {
	objectiveCppSources.setFrom(fileTree("srcs") { include("**/*.mm") })	// <2>
	privateHeaders.setFrom(fileTree("srcs") { include("**/*.hpp") })		// <3>
}
