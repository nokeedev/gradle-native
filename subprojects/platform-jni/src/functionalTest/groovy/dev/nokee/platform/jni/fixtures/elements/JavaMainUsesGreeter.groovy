package dev.nokee.platform.jni.fixtures.elements

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile

class JavaMainUsesGreeter extends JavaSourceElement implements GreeterElement {
    final SourceFileElement main

    @Override
    SourceElement getSources() {
        return main
    }

    JavaMainUsesGreeter() {
        def javaPackage = ofPackage('com.example.app')
        main = ofFile(sourceFile("java/${javaPackage.directoryLayout}", 'Main.java', """
package ${javaPackage.name};

import com.example.greeter.Greeter;

public class Main {
    public static void main(String[] args) {
        System.out.println(new Greeter().sayHello("World"));
    }
}
"""))
    }

    @Override
    String getExpectedOutput() {
        return 'Bonjour, World!'
    }
}
