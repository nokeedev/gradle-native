plugins {
	id("dev.nokee.objective-c-application")	// <1>
}

application {
	objectiveCSources.setFrom("srcs/main.m", "srcs/potato/greeter.m")	// <2>
	privateHeaders.setFrom(fileTree("hdrs") { include("tomato/greeter.h") })	// <3>
}
