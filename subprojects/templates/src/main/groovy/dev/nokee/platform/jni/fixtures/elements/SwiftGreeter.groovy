package dev.nokee.platform.jni.fixtures.elements


import dev.gradleplugins.test.fixtures.sources.SourceFile
import dev.gradleplugins.test.fixtures.sources.SourceFileElement

class SwiftGreeter extends SourceFileElement {
	@Override
	SourceFile getSourceFile() {
		return sourceFile('swift', 'greeter.swift', """
public class Greeter {
	public init() {}
	public func sayHello(name: String) -> String {
		return "Bonjour, " + name + "!";
	}
}
""")
	}
}
