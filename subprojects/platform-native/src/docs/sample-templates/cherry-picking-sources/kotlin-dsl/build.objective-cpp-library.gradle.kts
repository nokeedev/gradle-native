plugins {
	id("dev.nokee.objective-cpp-library")	// <1>
}

library {
	objectiveCppSources.setFrom("srcs/main.mm", "srcs/potato/greeter.mm")	// <2>
	privateHeaders.setFrom(fileTree("hdrs") { include("tomato/greeter.hpp") })	// <3>
	publicHeaders.setFrom(fileTree("incs") { include("tomato/greeter.hpp") })	// <3>
}
