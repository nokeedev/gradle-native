plugins {
	id("dev.nokee.swift-library")	// <1>
}

library {
//	swiftSources.from("src/main/swift") // default initially set by convention <2>
	swiftSources.from("sources/application.swift")	// <3>
	swiftSources.from("sources/greeter.swift")	// <4>
}
