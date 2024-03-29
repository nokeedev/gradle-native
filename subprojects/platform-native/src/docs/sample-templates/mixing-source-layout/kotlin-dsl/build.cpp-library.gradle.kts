plugins {
	id("dev.nokee.cpp-library")	// <1>
}

library {
//	cppSources.from("src/main/cpp") // default initially set by convention <2>
	cppSources.from("sources/application.cpp")	// <3>
	cppSources.from("sources/greeter.cpp")	// <4>

//	privateHeaders.from("src/main/headers") // default initially set by convention <4>
	privateHeaders.from("headers")

//	publicHeaders.from("src/main/public") // default initially set by convention <4>
	publicHeaders.from("includes")
}
