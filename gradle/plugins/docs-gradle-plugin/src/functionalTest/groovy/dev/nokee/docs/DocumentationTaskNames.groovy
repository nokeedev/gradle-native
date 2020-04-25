package dev.nokee.docs


import static org.gradle.util.GUtil.toCamelCase

trait DocumentationTaskNames {
	/**
	 * Returns the tasks for the root project.
	 */
	ProjectTasks getTasks() {
		return new ProjectTasks('')
	}

	static class ProjectTasks {
		private final String project

		ProjectTasks(String project) {
			this.project = project
		}

		private withProject(String t) {
			project + ":" + t
		}

		WithSampleTasks withSample(String sampleName) {
			return new WithSampleTasks(sampleName);
		}

		class WithSampleTasks {
			String sampleName

			WithSampleTasks(String sampleName) {
				this.sampleName = sampleName
			}

			String getGradleWrapper() {
				return withProject('generateSamplesGradleWrapper')
			}

			List<String> getAllToAssembleGroovyDsl() {
				return [gradleWrapper, withProject("generate${toCamelCase(sampleName)}SampleContent"), withProject('configureGroovyDslSettingsConfiguration'), withProject("process${toCamelCase(sampleName)}GroovyDslSettingsFile"), withProject("assemble${toCamelCase(sampleName)}GroovyDsl")]
			}

			String getZipGroovyDsl() {
				return withProject("zip${toCamelCase(sampleName)}GroovyDslSample")
			}

			List<String> getAllToZipGroovyDsl() {
				return allToAssembleGroovyDsl + [zipGroovyDsl]
			}

			List<String> getAllToAssembleKotlinDsl() {
				return [gradleWrapper, withProject("generate${toCamelCase(sampleName)}SampleContent"), withProject('configureKotlinDslSettingsConfiguration'), withProject("process${toCamelCase(sampleName)}KotlinDslSettingsFile"), withProject("assemble${toCamelCase(sampleName)}KotlinDsl")]
			}

			String getZipKotlinDsl() {
				return withProject("zip${toCamelCase(sampleName)}KotlinDslSample")
			}

			List<String> getAllToZipKotlinDsl() {
				return allToAssembleKotlinDsl + [zipKotlinDsl]
			}

			List<String> getAllToStageSamples() {
				return allToAssembleGroovyDsl + allToAssembleKotlinDsl +
				 [withProject("process${toCamelCase(sampleName)}SampleAsciidoctors"), withProject("process${toCamelCase(sampleName)}SamplesAsciidoctorsMetadata"), withProject('stageSamples')]
			}

			List<String> getAllToAssembleSamples() {
				return allToStageSamples + [withProject('assembleSamples')] + allToZipGroovyDsl + allToZipKotlinDsl
			}

			List<String> getAllToStageBake() {
				return allToStageSamples + [withProject('assembleDsl'), withProject('dslMetaData'), withProject('processDocsAsciidoctors'), withProject('stageBake')]
			}

			List<String> getAllToStageDocumentation() {
				return allToStageSamples + allToZipGroovyDsl + allToZipKotlinDsl + [
					withProject("generateSamplesAsciinema${toCamelCase(sampleName)}"),
					withProject('assembleAsciicast'), withProject('compileDocsAsciicast'), withProject('extractDocsScreenshot'), withProject('compileDocsGif'), withProject('createDocsPlayer'),
					withProject('compileDocsDot'), withProject('processDocsAsciidoctors'),
					withProject('assembleDsl'), withProject('dslMetaData'),
					withProject('stageBake'), withProject('bake'),
					withProject('stageDocumentation')]
			}

			List<String> getAllToAssembleDocumentation() {
				return allToStageDocumentation + [withProject('assembleDocumentation')]
			}

			List<String> getAllToPublish() {
				return allToStageDocumentation + [withProject('zipJbakeBaked'), withProject('zipJbakeTemplates'),
					withProject('generateMetadataFileForBakedPublication'), withProject('generatePomFileForBakedPublication'), withProject('publishBakedPublicationToMavenRepository'),
				 	withProject('generateMetadataFileForJbakePublication'), withProject('generatePomFileForJbakePublication'), withProject('publishJbakePublicationToMavenRepository'),
					withProject('publish')]
			}
		}
	}
}
