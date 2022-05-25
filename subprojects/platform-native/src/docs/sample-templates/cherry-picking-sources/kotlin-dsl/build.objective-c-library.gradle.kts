plugins {
	id("dev.nokee.objective-c-library")	// <1>
}

library {
	objectiveCSources.setFrom("srcs/main.m", "srcs/potato/greeter.m")	// <2>
	privateHeaders.setFrom(fileTree("hdrs") { include("tomato/greeter.h") })	// <3>
	publicHeaders.setFrom(fileTree("incs") { include("tomato/greeter.h") })	// <3>
}
