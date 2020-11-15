plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.cpp-language")
}

sourceSets["main"].java.srcDir("srcs")			// <1>
library {
	sources {
		configure("cpp") { from("srcs") }		// <2>
		configure("headers") { from("incs") }	// <3>
	}
}
