package dev.gradleplugins.exemplarkit

import com.google.common.testing.EqualsTester
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

import static Step.builder
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.junit.jupiter.api.Assertions.*

class StepBuilderTest {
	@Test
	void "throws exception if no executable was specified"() {
		def ex = assertThrows(IllegalStateException) { builder().build() } as Executable
		assertEquals('Please specify an executable for this step.', ex.message)
	}

	@Nested
	class Equals {
		private static Step step(@DelegatesTo(Step.Builder) Closure closure) {
			def builder = builder()
			closure.delegate = builder
			closure.call(builder)
			closure.resolveStrategy = Closure.DELEGATE_FIRST
			return builder.build()
		}

		@Test
		void "can compare steps"() {
			new EqualsTester()
				.addEqualityGroup(
					step { execute('cat') },
					step { execute('cat') })
				.addEqualityGroup(step { execute('ls') })
				.addEqualityGroup(
					step { execute('ls', '-l') },
					step { execute('ls', '-l') })
				.addEqualityGroup(
					step { execute('cat', 'bar.txt') },
					step { execute('cat', 'bar.txt') })
				.addEqualityGroup(
					step { execute('ls').output('foo') },
					step { execute('ls').output('foo') })
				.addEqualityGroup(step { execute('ls').output('bar') })
				.addEqualityGroup(step { execute('cat').output('foo') })
				.addEqualityGroup(
					step { execute('cat').attribute('k1', 'v1') },
					step { execute('cat').attribute('k1', 'v1') })
				.testEquals()
		}
	}

	@Nested
	class InstanceIntegrity {
		static Step step = builder().execute('ls', '-a').build()

		@Test
		void "cannot modify returned arguments"() {
			assertAll({
				assertThrows(UnsupportedOperationException) { step.arguments.add('foo') } as Executable
				assertThrows(UnsupportedOperationException) { step.arguments.clear() } as Executable
				assertThrows(UnsupportedOperationException) { step.arguments.remove(0) } as Executable
			} as Executable)
		}
	}

	@Nested
	class ExecutableOnlyStep {
		static Step step = builder().execute('ls').build()

		@Test
		void "has executable"() {
			assertEquals(step.executable, 'ls')
		}

		@Test
		void "no output"() {
			assertFalse(step.output.present)
		}

		@Test
		void "no attributes"() {
			assertTrue(step.attributes.isEmpty())
		}

		@Test
		void "no arguments"() {
			assertTrue(step.arguments.isEmpty())
		}
	}

	@Nested
	class CommandLineBuilderSafety {
		@Test
		void "throws exception when executable is null"() {
			def ex = assertThrows(NullPointerException) { builder().execute(null) }
			assertEquals("Executable cannot be null.", ex.message)
		}

		@Test
		void "throws exception if any arguments are null"() {
			assertAll({
				assertThrows(NullPointerException) { builder().execute('ls', null) } as Executable
				assertThrows(NullPointerException) { builder().execute('ls', '-l', null)} as Executable
			}  as Executable)

			def ex = assertThrows(NullPointerException) { builder().execute('list', null) } as Executable
			assertEquals("No arguments can be null.", ex.message)
		}

		@Test
		void "replace executable and arguments on multiple execute invocation"() {
			def step = builder().execute('ls', 'foo').execute('cat', 'bar').build()
			assertEquals('cat', step.executable)
			assertThat(step.arguments, contains('bar'))
		}

		@Test
		void "strip whitespace before executable"() {
			def step = builder().execute('  dir').build()
			assertEquals('dir', step.executable)
		}

		@Test
		void "strip whitespace after executable"() {
			def step = builder().execute('echo   ').build()
			assertEquals('echo', step.executable)
		}

		@Test
		void "strip whitespace around arguments"() {
			def step = builder().execute('echo', '  Hello, world!  ', ' > ', ' /path/with space/ ').build()
			assertThat(step.arguments, contains('Hello, world!', '>', '/path/with space/'))
		}
	}

	@Nested
	class CanAttachArgumentsToStep {
		def step = builder().execute('ls', '-a', '/bin').build()

		@Test
		void "has executable"() {
			assertEquals(step.executable, 'ls')
		}

		@Test
		void "no output"() {
			assertFalse(step.output.present)
		}

		@Test
		void "no attributes"() {
			assertTrue(step.attributes.isEmpty())
		}

		@Test
		void "has arguments"() {
			assertThat(step.arguments, contains('-a', '/bin'))
		}
	}

	@Nested
	class CanAttachOutputToStep {
		def stepBuilder = builder().execute('ls', '-l', '/bin')

		@Test
		void "from a list of individual lines"() {
			def lines = ['total 4848',
						 '-rwxr-xr-x  1 root  wheel    35840 27 May 20:27 [',
						 '-r-xr-xr-x  1 root  wheel   623472 27 May 20:27 bash',
						 '-rwxr-xr-x  1 root  wheel    36768 27 May 20:27 cat',
						 '-rwxr-xr-x  1 root  wheel    47264 27 May 20:27 chmod',
						 '-rwxr-xr-x  1 root  wheel    42272 27 May 20:27 cp']
			def step = stepBuilder.output(*lines).build()

			assertTrue(step.output.present)
			assertEquals(lines.join(System.lineSeparator()), step.output.get())
		}

		@Test
		void "from a multi-line string"() {
			def output = '''total 4848
				|-rwxr-xr-x  1 root  wheel   529424 27 May 20:27 csh
				|rwxr-xr-x  1 root  wheel   110848 27 May 20:27 dash
				|rwxr-xr-x  1 root  wheel    41872 27 May 20:27 date
				|rwxr-xr-x  1 root  wheel    45120 27 May 20:27 dd
				|rwxr-xr-x  1 root  wheel    36512 27 May 20:27 df'''.stripMargin()
			def step = stepBuilder.output(output).build()

			assertTrue(step.output.present)
			assertEquals(output, step.output.get())
		}

		@Test
		void "can remove previous output by using null"() {
			def step = stepBuilder.output('foo').output(null).build()
			assertFalse(step.output.present)
		}

		@Test
		void "throws an exception if any lines are null"() {
			assertAll({
				assertThrows(NullPointerException) { stepBuilder.output('foo', null) } as Executable
				assertThrows(NullPointerException) { stepBuilder.output(null, 'foo') } as Executable
				assertThrows(NullPointerException) { stepBuilder.output('foo', 'bar', null) } as Executable
				assertThrows(NullPointerException) { stepBuilder.output('foo', 'bar', 'far', null) } as Executable
			} as Executable)

			def ex = assertThrows(NullPointerException) { stepBuilder.output('foo', 'bar', null) } as Executable
			assertEquals('No output lines can be null.', ex.message)
		}
	}

	@Nested
	class CanAttachAttributesToStep {
		def stepBuilder = builder().execute('cat', '/foo/bar')

		@Test
		void "attach single attribute"() {
			def step = stepBuilder.attribute('k1', 'v1').build()
			assertThat(step.attributes, aMapWithSize(1))
			assertThat(step.attributes, hasEntry('k1', 'v1'))
		}

		@Test
		void "attach multiple attributes individually"() {
			def step = stepBuilder.attribute('k2', 'v2').attribute('k3', 'v3').build()

			assertThat(step.attributes, aMapWithSize(2))
			assertThat(step.attributes, hasEntry('k2', 'v2'))
			assertThat(step.attributes, hasEntry('k3', 'v3'))
		}

		@Test
		void "attach multiple attributes"() {
			def step = stepBuilder.attributes([k6: 'v6', k7: 'v7']).build()

			assertThat(step.attributes, aMapWithSize(2))
			assertThat(step.attributes, hasEntry('k6', 'v6'))
			assertThat(step.attributes, hasEntry('k7', 'v7'))
		}

		@Test
		void "can replace attributes"() {
			def step = stepBuilder.attribute('k2', 'v2').attribute('k3', 'v3').attributes([k4: 'v4']).build()

			assertThat(step.attributes, aMapWithSize(1))
			assertThat(step.attributes, hasEntry('k4', 'v4'))
		}
	}

	@Nested
	class ToString {
		private static String step(@DelegatesTo(Step.Builder) Closure closure) {
			def builder = builder()
			closure.delegate = builder
			closure.call(builder)
			closure.resolveStrategy = Closure.DELEGATE_FIRST
			return builder.build().toString()
		}

		@Test
		void "executable only"() {
			assertEquals('Step{command=ls}', step { execute('ls') })
		}

		@Test
		void "executable with arguments"() {
			assertEquals('Step{command=cat /some/file}', step { execute('cat', '/some/file') })
		}

		@Test
		void "executable with multi-line output"() {
			assertEquals('Step{command=./foo, output=foo[...]}', step { execute('./foo').output('foo', 'bar', 'far') })
			assertEquals('Step{command=./foo, output=foo[...]}', step { execute('./foo').output('foo\r\nbar\r\nfar') })
			assertEquals('Step{command=./foo, output=foo[...]}', step { execute('./foo').output('foo\nbar\nfar') })
		}

		@Test
		void "executable with single line output"() {
			assertEquals('Step{command=./bar, output=bar}', step { execute('./bar').output('bar') })
		}

		@Test
		void "executable with attributes"() {
			assertEquals('Step{command=./far, attributes={k0=v0, k1=v1, k2=v2}}', step { execute('./far').attribute('k0', 'v0').attribute('k1', 'v1').attribute('k2', 'v2') })
		}
	}
}
