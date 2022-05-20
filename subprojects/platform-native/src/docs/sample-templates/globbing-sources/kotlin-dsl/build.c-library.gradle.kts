plugins {
	id("dev.nokee.c-library")										// <1>
}

application {
	cSources.setFrom(fileTree("srcs") { include("**/*.c") })		// <2>
	privateHeaders.setFrom(fileTree("hdrs") { include("**/*.h") })	// <3>
	publicHeaders.setFrom(fileTree("incs") { include("**/*.h") })	// <4>
}
