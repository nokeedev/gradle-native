plugins {
	id("dev.nokee.objective-cpp-application")	// <1>
}

application {
	objectiveCppSources.setFrom("srcs/main.mm", "srcs/potato/greeter.mm")	// <2>
	privateHeaders.setFrom(fileTree("hdrs") { include("tomato/greeter.hpp") })	// <3>
}
