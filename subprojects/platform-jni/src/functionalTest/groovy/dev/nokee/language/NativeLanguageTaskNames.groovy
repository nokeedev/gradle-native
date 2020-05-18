package dev.nokee.language

import dev.gradleplugins.integtests.fixtures.nativeplatform.AvailableToolChains

trait NativeLanguageTaskNames {
	abstract AvailableToolChains.InstalledToolChain getToolchainUnderTest()

	abstract String getLanguageTaskSuffix()

	String getSoftwareModelLanguageTaskSuffix() {
		return getLanguageTaskSuffix()
	}

	/**
	 * Returns the tasks for the project with the given path.
	 */
	ProjectTasks tasks(String project) {
		return new ProjectTasks(project, toolchainUnderTest, languageTaskSuffix, softwareModelLanguageTaskSuffix)
	}

	/**
	 * Returns the tasks for the root project.
	 */
	ProjectTasks getTasks() {
		return new ProjectTasks('', toolchainUnderTest, languageTaskSuffix, softwareModelLanguageTaskSuffix)
	}

	static class ProjectTasks {
		private final String project
		private final AvailableToolChains.InstalledToolChain toolChainUnderTest
		private final String languageTaskSuffix
		private final String softwareModelLanguageTaskSuffix
		private String architecture = null
		private String operatingSystemFamily = null
		private final String buildType = ''
		private String binaryType = ''

		ProjectTasks(String project, AvailableToolChains.InstalledToolChain toolChainUnderTest, String languageTaskSuffix, String softwareModelLanguageTaskSuffix) {
			this.toolChainUnderTest = toolChainUnderTest
			this.project = project
			this.languageTaskSuffix = languageTaskSuffix
			this.softwareModelLanguageTaskSuffix = softwareModelLanguageTaskSuffix
		}

		ProjectTasks withArchitecture(String architecture) {
			this.architecture = architecture
			return this
		}

		private withProject(String t) {
			project + ":" + t
		}

		ProjectTasks withOperatingSystemFamily(String operatingSystemFamily) {
			this.operatingSystemFamily = operatingSystemFamily
			return this
		}

		// TODO: Return a specialized type for shared library binary
		ProjectTasks getForSharedLibrary() {
			this.binaryType = 'sharedLibrary'
			return this
		}
		String getBinary() {
			return withProject("${binaryType}${buildType}${variant}")
		}
		List<String> getAllToBinary() {
			return allToLink + [binary]
		}

		String getCompile() {
			return withProject("compile${buildType}${variant}${languageTaskSuffix}")
		}

		String getObjects() {
			return withProject("objects${buildType}${variant}")
		}

		String getLink() {
			return withProject("link${buildType}${variant}")
		}

		String getCreate() {
			return withProject("create${buildType}${variant}")
		}

		String getInstall() {
			return withProject("install${buildType}${variant}")
		}

		String getAssemble() {
			return withProject("assemble${buildType}${variant}")
		}

		List<String> getAllToObjects() {
			return [compile, objects]
		}

		List<String> getAllToCreate() {
			return [compile, create]
		}

		List<String> getAllToLink() {
			return [compile, link]
		}

		List<String> getAllToInstall() {
			return allToLink + [install]
		}

		List<String> getAllToAssemble() {
			return allToLink + [assemble]
		}

		List<String> getAllToAssembleWithInstall() {
			return allToInstall + [assemble]
		}

		protected String getVariant() {
			String result = ""
			if (operatingSystemFamily != null) {
				result += operatingSystemFamily.toLowerCase().capitalize()
			}
			if (architecture != null) {
				result += architecture.toLowerCase().capitalize()
			}
			return result
		}
	}
}
