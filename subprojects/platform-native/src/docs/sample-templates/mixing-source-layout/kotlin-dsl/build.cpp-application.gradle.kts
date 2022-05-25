plugins {
	id("dev.nokee.cpp-application")	// <1>
}

application {
//	cppSources.from("src/main/cpp") // default initially set by convention <2>
	cppSources.from("sources/application.cpp")	// <3>
	cppSources.from("sources/greeter.cpp")	// <4>

//	privateHeaders.from("src/main/headers") // default initially set by convention <4>
	privateHeaders.from("headers")
}
