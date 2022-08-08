plugins {
	id("dev.nokee.c-library")	// <1>
}

library {
	cSources.setFrom("srcs/main.c", "srcs/potato/greeter.c")	// <2>
	privateHeaders.setFrom(fileTree("hdrs") { include("tomato/greeter.h") })	// <3>
	publicHeaders.setFrom(fileTree("incs") { include("tomato/greeter.h") })	// <3>
}
