plugins {
	id("dev.nokee.swift-application")	// <1>
}

application {
//	swiftSources.from("src/main/swift") // default initially set by convention <2>
	swiftSources.from("sources/application.swift")	// <3>
	swiftSources.from("sources/greeter.swift")	// <4>
}
