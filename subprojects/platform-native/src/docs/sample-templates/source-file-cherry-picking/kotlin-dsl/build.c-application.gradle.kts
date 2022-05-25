plugins {
	id("dev.nokee.c-application")	// <1>
}

application {
	cSources.setFrom("srcs/main.c", "srcs/potato/greeter.c")	// <2>
	privateHeaders.setFrom(fileTree("hdrs") { include "tomato/greeter.h" })	// <3>
}
