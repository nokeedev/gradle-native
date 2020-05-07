package dev.nokee.platform.jni.fixtures.elements

import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.gradleplugins.test.fixtures.sources.java.JavaPackage
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceFileElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile

class JavaGreeterJUnitTest extends JavaSourceFileElement {
	private final SourceFileElement source

	@Override
	SourceFileElement getSource() {
		return source
	}

	@Override
	String getSourceSetName() {
		return 'test'
	}

	JavaGreeterJUnitTest(JavaPackage javaPackage = new JavaPackage('com.example.greeter')) {
		source = ofFile(sourceFile("java/${javaPackage.directoryLayout}", 'GreeterTest.java', """
package ${javaPackage.name};

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class GreeterTest {
    @Test
    public void testGreeter() {
        Greeter greeter = new Greeter();
        String greeting = greeter.sayHello("World");
        assertThat(greeting, equalTo("Bonjour, World!"));
    }

    @Test
    public void testNullGreeter() {
        Greeter greeter = new Greeter();
        String greeting = greeter.sayHello(null);
        assertThat(greeting, equalTo("name cannot be null"));
    }
}
"""))
	}
}
