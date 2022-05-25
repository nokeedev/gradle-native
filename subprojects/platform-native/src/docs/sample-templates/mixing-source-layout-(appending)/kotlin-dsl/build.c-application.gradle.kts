plugins {
	id("dev.nokee.c-application")	// <1>
}

application {
//	cSources.from("src/main/c") // default initially set by convention <2>
	cSources.from("sources/application.c")	// <3>
	cSources.from("sources/greeter.c")	// <4>

//	privateHeaders.from("src/main/headers") // default initially set by convention <4>
	privateHeaders.from("headers")
}
