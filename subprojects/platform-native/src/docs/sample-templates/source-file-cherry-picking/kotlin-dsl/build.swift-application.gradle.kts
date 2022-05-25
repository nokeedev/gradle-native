plugins {
	id("dev.nokee.swift-application")	// <1>
}

application {
	swiftSources.setFrom("srcs/main.swift", "srcs/potato/greeter.c")	// <2>
}
