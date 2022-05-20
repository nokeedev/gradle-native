plugins {
	id("dev.nokee.objective-cpp-library")									// <1>
}

application {
	objectiveCppSources.setFrom(fileTree("srcs") { include("**/*.mm") })	// <2>
	privateHeaders.setFrom(fileTree("srcs") { include("**/*.hpp") })		// <3>
	publicHeaders.setFrom(fileTree("incs") { include("**/*.hpp") })			// <3>
}
