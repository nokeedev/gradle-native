plugins {
	id("dev.nokee.cpp-library")											// <1>
}

application {
	cppSources.setFrom(fileTree("srcs") { include("**/*.cpp") })		// <2>
	privateHeaders.setFrom(fileTree("hdrs") { include("**/*.hpp") })	// <3>
	publicHeaders.setFrom(fileTree("incs") { include("**/*.hpp") })		// <3>
}
