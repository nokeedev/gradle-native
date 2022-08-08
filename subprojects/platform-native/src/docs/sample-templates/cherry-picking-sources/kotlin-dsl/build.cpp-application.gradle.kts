plugins {
	id("dev.nokee.cpp-application")	// <1>
}

application {
	cppSources.setFrom("srcs/main.cpp", "srcs/potato/greeter.cpp")	// <2>
	privateHeaders.setFrom(fileTree("hdrs") { include("tomato/greeter.hpp") })	// <3>
}
