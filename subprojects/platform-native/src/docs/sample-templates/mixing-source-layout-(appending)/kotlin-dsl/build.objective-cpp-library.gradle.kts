plugins {
	id("dev.nokee.objective-cpp-library")	// <1>
}

library {
//	objectiveCppSources.from("src/main/objectiveCpp", "src/main/objcpp") // default initially set by convention <2>
	objectiveCppSources.from("sources/application.mm")	// <3>
	objectiveCppSources.from("sources/greeter.mm")	// <4>

//	privateHeaders.from("src/main/headers") // default initially set by convention <4>
	privateHeaders.from("headers")

//	publicHeaders.from("src/main/public") // default initially set by convention <4>
	publicHeaders.from("includes")
}
