plugins {
	id("dev.nokee.cpp-library")	// <1>
}

library {
	cppSources.setFrom("srcs/main.cpp", "srcs/potato/greeter.cpp")	// <2>
	privateHeaders.setFrom(fileTree("hdrs") { include("tomato/greeter.hpp") })	// <3>
	publicHeaders.setFrom(fileTree("incs") { include("tomato/greeter.hpp") })	// <3>
}
