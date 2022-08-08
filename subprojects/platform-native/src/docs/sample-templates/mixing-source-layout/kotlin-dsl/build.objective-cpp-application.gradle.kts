plugins {
	id("dev.nokee.objective-cpp-application")	// <1>
}

application {
//	objectiveCppSources.from("src/main/objectiveCpp", "src/main/objcpp") // default initially set by convention <2>
	objectiveCppSources.from("sources/application.mm")	// <3>
	objectiveCppSources.from("sources/greeter.mm")	// <4>

//	privateHeaders.from("src/main/headers") // default initially set by convention <4>
	privateHeaders.from("headers")
}
