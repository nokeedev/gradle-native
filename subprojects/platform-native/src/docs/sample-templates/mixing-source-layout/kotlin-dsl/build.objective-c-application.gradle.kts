plugins {
	id("dev.nokee.objective-c-application")	// <1>
}

application {
//	objectiveCSources.from("src/main/objectiveC", "src/main/objc") // default initially set by convention <2>
	objectiveCSources.from("sources/application.m")	// <3>
	objectiveCSources.from("sources/greeter.m")	// <4>

//	privateHeaders.from("src/main/headers") // default initially set by convention <4>
	privateHeaders.from("headers")
}
