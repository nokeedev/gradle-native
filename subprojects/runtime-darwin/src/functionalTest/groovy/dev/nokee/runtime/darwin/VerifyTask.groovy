package dev.nokee.runtime.darwin


import java.util.function.Consumer

final class VerifyTask {
	private final List<String> verifySegments = new ArrayList<>()

	VerifyTask() {
		verifySegments << '''
			def verifyTask = tasks.register('verify')
		'''
	}

	VerifyTask that(Consumer<? super Verification> builder) {
		def verification = new Verification()
		builder.accept(verification)
		verifySegments << """
		|	verifyTask.configure {
		|${verification.toString()}
		|	}
		|""".stripMargin()
		return this
	}

	@Override
	String toString() {
		return verifySegments.join('\n')
	}

	static class Verification {
		private String artifactType
		private boolean transformed = false
		private String fileName
		private File path

		Verification artifactType(String type) {
			artifactType = type
			this
		}

		void transformed(String fileName) {
			transformed = true
			this.fileName = fileName
		}

		void directory(File path) {
			this.fileName = path.name
			this.path = path
		}

		@Override
		String toString() {
			def result = """
			|	doLast {
			|		def resolvedFile = configurations.test.incoming.artifactView {
			|			attributes {
			|				attribute(Attribute.of('artifactType', String), '${artifactType}')
			|			}
			|		}.files.singleFile
			|		assert resolvedFile.directory
			|""".stripMargin()
			if (path != null) {
				result << """
				|		assert resolvedFile.absolutePath == '${path}'
				|""".stripMargin()
			}
			if (transformed) {
				result << """
				|		assert resolvedFile.name == '${fileName}'
				|		assert resolvedFile.parentFile.name == 'transformed' ||
				|			resolvedFile.path.contains('/.transforms/') ||
				|			resolvedFile.path.contains('/transforms-')
				|""".stripMargin()
			} else {
				result << """
				|		assert resolvedFile.name == '${fileName}'
				|		assert resolvedFile.parentFile.name != 'transformed' &&
				|			!resolvedFile.path.contains('/.transforms/') &&
				|			!resolvedFile.path.contains('/transforms-')
				|""".stripMargin()
			}
			result << '''
			|	}
			|'''.stripMargin()
		}
	}

	static VerifyTask verifyTask() {
		return new VerifyTask()
	}
}
