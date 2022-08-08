plugins {
	id("dev.nokee.swift-library")	// <1>
}

library {
	swiftSources.setFrom("srcs/main.swift", "srcs/potato/greeter.swift") // <2>
}
