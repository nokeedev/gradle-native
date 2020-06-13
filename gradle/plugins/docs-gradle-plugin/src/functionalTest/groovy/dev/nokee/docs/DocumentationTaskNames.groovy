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
				return withProject("generate${toCamelCase(sampleName)}SampleGradleWrapper")
			}

			List<String> getAllToAssembleContent() {
				return [withProject("generate${toCamelCase(sampleName)}SampleContent"), withProject("process${toCamelCase(sampleName)}SampleAsciidoctor")]
			}

			List<String> getAllToAssembleGroovyDsl() {
				return [gradleWrapper, withProject("generate${toCamelCase(sampleName)}SampleContent"), withProject('configureGroovyDslSettingsConfiguration'), withProject("process${toCamelCase(sampleName)}GroovyDslSettingsFile")]
			}

			String getZipGroovyDsl() {
				return withProject("zip${toCamelCase(sampleName)}SampleGroovyDsl")
			}

			List<String> getAllToZipGroovyDsl() {
				return allToAssembleGroovyDsl + [zipGroovyDsl]
			}

			List<String> getAllToAssembleKotlinDsl() {
				return [gradleWrapper, withProject("generate${toCamelCase(sampleName)}SampleContent"), withProject('configureKotlinDslSettingsConfiguration'), withProject("process${toCamelCase(sampleName)}KotlinDslSettingsFile")]
			}

			String getZipKotlinDsl() {
				return withProject("zip${toCamelCase(sampleName)}SampleKotlinDsl")
			}

			List<String> getAllToZipKotlinDsl() {
				return allToAssembleKotlinDsl + [zipKotlinDsl]
			}

			List<String> getAllToStageSample() {
				return allToAssembleGroovyDsl + allToAssembleKotlinDsl + allToAssembleContent + allToZipGroovyDsl + allToZipKotlinDsl + [withProject("stage${toCamelCase(sampleName)}Sample")]
			}

			List<String> getAllToStageSamples() {
				return allToStageSample + [withProject('stageSamples')]
			}

			List<String> getAllToAssembleSamples() {
				return allToStageSample + [withProject('stageSamples'), withProject('assembleSamples')]
			}

			List<String> getAllToStageBake() {
				return allToStageSamples + [withProject('assembleDsl'), withProject('dslMetaData'), withProject('processDocsAsciidoctors'), withProject('stageBake')]
			}

			List<String> getAllToSocial() {
				// Missing some tasks here, but that is fine for now.
				return [withProject("compile${toCamelCase(sampleName)}SampleAsciicast"), withProject("generate${toCamelCase(sampleName)}SampleAsciinema"), withProject("compile${toCamelCase(sampleName)}SampleGif"), withProject("extract${toCamelCase(sampleName)}SampleScreenshot"), withProject("create${toCamelCase(sampleName)}SampleEmbeddedPlayer")]
			}

			List<String> getAllToStageDocumentation() {
				return allToStageSamples + allToSocial + [
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
