package dev.nokee.runtime.nativebase


import java.util.function.Function

final class VerifyTask {
	private final List<String> verifySegments = new ArrayList<>()

	VerifyTask() {
		verifySegments << '''
			def verifyTask = tasks.register('verify')
		'''
	}

	VerifyTask that(Function<? super Verification, ? extends String> builder) {
		def verification = new Verification()
		verifySegments << """
		|	verifyTask.configure {
		|		doLast {
		|			assert configurations.${builder.apply(verification)}
		|		}
		|	}
		|""".stripMargin()
		return this
	}

	@Override
	String toString() {
		return verifySegments.join('\n')
	}

	static class Verification {
		String artifactType(String type) {
			return """incoming.artifactView {
				|			attributes {
				|				attribute(Attribute.of('artifactType', String), '${type}')
				|			}
				|			lenient = true
				|		}.files""".stripMargin()
		}

		String allFiles() {
			return 'incoming.artifactView { lenient = true }.files'
		}
	}

	static VerifyTask verifyTask() {
		return new VerifyTask()
	}
}
