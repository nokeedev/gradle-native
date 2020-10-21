package dev.nokee.platform.jni.fixtures.elements

import dev.gradleplugins.fixtures.sources.SourceFile
import dev.gradleplugins.fixtures.sources.SourceFileElement

import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage

class JavaMainUsesGreeter extends SourceFileElement implements GreeterElement {
	final SourceFile main

	@Override
	SourceFile getSourceFile() {
		return main
	}

	JavaMainUsesGreeter() {
		def javaPackage = ofPackage('com.example.app')
		main = sourceFile("java/${javaPackage.directoryLayout}", 'Main.java', """
package ${javaPackage.name};

import com.example.greeter.Greeter;

public class Main {
	public static void main(String[] args) {
		System.out.println(new Greeter().sayHello("World"));
	}
}
""")
	}

	@Override
	String getExpectedOutput() {
		return 'Bonjour, World!'
	}
}
