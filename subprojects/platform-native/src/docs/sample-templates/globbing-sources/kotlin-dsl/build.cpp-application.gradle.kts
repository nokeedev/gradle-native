plugins {
	id("dev.nokee.cpp-application")										// <1>
}

application {
	cppSources.setFrom(fileTree("srcs") { include("**/*.cpp") })		// <2>
	privateHeaders.setFrom(fileTree("hdrs") { include("**/*.hpp") })	// <3>
}
