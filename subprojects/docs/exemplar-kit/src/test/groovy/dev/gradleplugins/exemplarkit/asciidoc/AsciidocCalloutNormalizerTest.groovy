package dev.gradleplugins.exemplarkit.asciidoc


import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static dev.gradleplugins.exemplarkit.asciidoc.AsciidocCalloutNormalizer.normalize
import static org.junit.jupiter.api.Assertions.assertEquals

class AsciidocCalloutNormalizerTest {
	@ParameterizedTest
	@ValueSource(strings = ['// <1>', '// (2)', '# <3>', '# (4)'])
	void "removes asciidoc callout of single line"(String callout) {
		assertEquals('ls', normalize("ls ${callout}"))
	}

	@ParameterizedTest
	@ValueSource(strings = ['// <5>', '// (6)', '# <7>', '# (8)'])
	void "removes asciidoc callout of multiple line"(String callout) {
		assertEquals('''Foo
			|Bar
			|Far
			|'''.stripMargin(),
			normalize("""Foo
				|Bar ${callout}
				|Far
				|""".stripMargin()))
	}

	@ParameterizedTest
	@ValueSource(strings = ['// <9>', '// (1)', '# <2>', '# (3)'])
	void "removes asciidoc callout with tailing whitespace"(String callout) {
		assertEquals('./gradlew assemble', normalize("./gradlew assemble ${callout}  "))
	}

	@ParameterizedTest
	@ValueSource(strings = ['  // <4>', '   // (5)', '    # <6>', '     # (7)'])
	void "removes asciidoc callout without whitespace between text annotation"(String callout) {
		assertEquals('mkdir -p src/main/cpp', normalize("mkdir -p src/main/cpp${callout}"))
	}

	@ParameterizedTest
	@ValueSource(strings = ['//<8>', '//(9)', '#<1>', '#(2)'])
	void "removes asciidoc callout without whitespace between text and annotation chars"(String callout) {
		assertEquals('unzip -o foo.zip', normalize("unzip -o foo.zip ${callout}"))
	}

	@ParameterizedTest
	@ValueSource(strings = ['//  <3>', '//   (4)', '#    <5>', '#     (6)'])
	void "removes asciidoc callout with more than one space between text and annotation chars"(String callout) {
		assertEquals('tree .', normalize("tree . ${callout}"))
	}

	@ParameterizedTest
	@ValueSource(strings = ['// <7>', '//  (8)', ' #  <9> ', '  #  (1)  '])
	void "ignore non-ending pattern like not at the end"(String callout) {
		assertEquals("sed -e '{${callout}}'".toString(), normalize("sed -e '{${callout}}'"))
	}

	@ParameterizedTest
	@ValueSource(strings = ['// <10>', '// (213)', '# <4563>', '# (98732)'])
	void "removes asciidoc callout of with multiple digits"(String callout) {
		assertEquals('cmd /c msvc.bat', normalize("cmd /c msvc.bat ${callout}"))
	}
}
