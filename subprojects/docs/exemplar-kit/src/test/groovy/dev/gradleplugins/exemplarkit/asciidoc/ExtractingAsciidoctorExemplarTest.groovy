package dev.gradleplugins.exemplarkit.asciidoc

import dev.gradleplugins.exemplarkit.Step
import dev.gradleplugins.exemplarkit.testers.StepTester
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static AsciidocExemplarStepLoader.extractFromAsciiDoc
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.emptyIterable
import static org.hamcrest.Matchers.equalTo

class ExtractingAsciidoctorExemplarTest {
	@TempDir
	protected Path testDirectory

	private File asciidoc(String content = '') {
		File adocFile = testDirectory.resolve('foo.adoc').toFile()
		adocFile << content
		return adocFile
	}

	@Nested
	class EmptyAsciidocFile {
		@Test
		void "has no steps"() {
			assertThat(extractFromAsciiDoc(asciidoc()), emptyIterable())
		}
	}

	@Nested
	class SingleTerminalListingBlockWithSingleCommandAndNoOutput {
		List<Step> steps

		@BeforeEach
		void "an adoc file"() {
			File adocFile = asciidoc('''= Title
				|
				|[listing.terminal]
				|----
				|$ cd foo/bar
				|----
				|'''.stripMargin())
			this.steps =  extractFromAsciiDoc(adocFile)
		}

		@Test
		void "has one step"() {
			assertThat(steps.size(), equalTo(1))
		}

		@Nested
		class OnlyStep extends StepTester {
			@Override
			Step getStepUnderTest() {
				return steps.get(0)
			}

			@Override
			String getExpectedExecutable() {
				return 'cd'
			}

			@Override
			List<String> getExpectedArguments() {
				return ['foo/bar']
			}

			@Override
			String getExpectedOutput() {
				return ""
			}

			@Override
			Map<String, Object> getExpectedAttributes() {
				return ['1': 'listing', style: 'listing', role: 'terminal']
			}
		}
	}

	@Nested
	class SingleTerminalListingBlockWithMultipleCommand {
		List<Step> steps

		@BeforeEach
		void "an adoc file"() {
			File adocFile = asciidoc('''= Title
				|
				|[listing.terminal]
				|----
				|$ ls /usr/bin
				|$ cat foo
				|Some
				|Text
				| * In
				| * Foo
				|----
				|'''.stripMargin())
			this.steps =  extractFromAsciiDoc(adocFile)
		}

		@Test
		void "has two steps"() {
			assertThat(steps.size(), equalTo(2))
		}

		@Nested
		class FirstStep extends StepTester {
			@Override
			Step getStepUnderTest() {
				return steps.get(0)
			}

			@Override
			String getExpectedExecutable() {
				return 'ls'
			}

			@Override
			List<String> getExpectedArguments() {
				return ['/usr/bin']
			}

			@Override
			String getExpectedOutput() {
				return ""
			}

			@Override
			Map<String, Object> getExpectedAttributes() {
				return ['1': 'listing', style: 'listing', role: 'terminal']
			}
		}

		@Nested
		class SecondStep extends StepTester {
			@Override
			Step getStepUnderTest() {
				return steps.get(1)
			}

			@Override
			String getExpectedExecutable() {
				return 'cat'
			}

			@Override
			List<String> getExpectedArguments() {
				return ['foo']
			}

			@Override
			String getExpectedOutput() {
				return '''Some
					|Text
					| * In
					| * Foo'''.stripMargin()
			}

			@Override
			Map<String, Object> getExpectedAttributes() {
				return ['1': 'listing', style: 'listing', role: 'terminal']
			}
		}
	}

	@Nested
	class SingleTerminalListingBlockWithSingleCommandAndOutput {
		List<Step> steps

		@BeforeEach
		void "an adoc file"() {
			File adocFile = asciidoc('''= Title
				|
				|[listing.terminal]
				|----
				|$ ./gradlew clean assemble
				|
				|BUILD SUCCESSFUL
				|5 actionable tasks: 5 executed
				|----
				|'''.stripMargin())
			this.steps =  extractFromAsciiDoc(adocFile)
		}

		@Test
		void "has one step"() {
			assertThat(steps.size(), equalTo(1))
		}

		@Nested
		class FirstStep extends StepTester {
			@Override
			Step getStepUnderTest() {
				return steps.get(0)
			}

			@Override
			String getExpectedExecutable() {
				return './gradlew'
			}

			@Override
			List<String> getExpectedArguments() {
				return ['clean', 'assemble']
			}

			@Override
			String getExpectedOutput() {
				return '''
					|BUILD SUCCESSFUL
					|5 actionable tasks: 5 executed'''.stripMargin()
			}

			@Override
			Map<String, Object> getExpectedAttributes() {
				return ['1': 'listing', style: 'listing', role: 'terminal']
			}
		}
	}

	@Nested
	class CommandLineAsciidocCalloutNormalizerTest {
		List<Step> steps

		@BeforeEach
		void "an adoc file"() {
			File adocFile = asciidoc('''= Title
				|
				|[listing.terminal]
				|----
				|$ ./gradlew clean assemble // <1>
				|
				|BUILD SUCCESSFUL
				|5 actionable tasks: 5 executed
				|----
				|'''.stripMargin())
			this.steps =  extractFromAsciiDoc(adocFile)
		}

		@Nested
		class FirstStep extends StepTester {
			@Override
			Step getStepUnderTest() {
				return steps.get(0)
			}

			@Override
			String getExpectedExecutable() {
				return './gradlew'
			}

			@Override
			List<String> getExpectedArguments() {
				return ['clean', 'assemble']
			}

			@Override
			String getExpectedOutput() {
				return '''
					|BUILD SUCCESSFUL
					|5 actionable tasks: 5 executed'''.stripMargin()
			}

			@Override
			Map<String, Object> getExpectedAttributes() {
				return ['1': 'listing', style: 'listing', role: 'terminal']
			}
		}
	}

	@Nested
	class CommandOutputAsciidocCalloutNormalizerTest {
		List<Step> steps

		@BeforeEach
		void "an adoc file"() {
			File adocFile = asciidoc('''= Title
				|
				|[listing.terminal]
				|----
				|$ ./gradlew clean assemble
				|
				|BUILD SUCCESSFUL // (2)
				|5 actionable tasks: 5 executed
				|----
				|'''.stripMargin())
			this.steps =  extractFromAsciiDoc(adocFile)
		}

		@Nested
		class FirstStep extends StepTester {
			@Override
			Step getStepUnderTest() {
				return steps.get(0)
			}

			@Override
			String getExpectedExecutable() {
				return './gradlew'
			}

			@Override
			List<String> getExpectedArguments() {
				return ['clean', 'assemble']
			}

			@Override
			String getExpectedOutput() {
				return '''
					|BUILD SUCCESSFUL
					|5 actionable tasks: 5 executed'''.stripMargin()
			}

			@Override
			Map<String, Object> getExpectedAttributes() {
				return ['1': 'listing', style: 'listing', role: 'terminal']
			}
		}
	}
}
