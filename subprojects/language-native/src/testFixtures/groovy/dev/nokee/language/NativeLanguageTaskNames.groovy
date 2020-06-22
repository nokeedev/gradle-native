package dev.nokee.language

trait NativeLanguageTaskNames implements NativeProjectTaskNames {
	abstract String getLanguageTaskSuffix()

	/**
	 * Returns the tasks for the project with the given path.
	 */
	NativeProjectTasks tasks(String project) {
		return new ProjectTasks(project, languageTaskSuffix)
	}

	/**
	 * Returns the tasks for the root project.
	 */
	NativeProjectTasks getTasks() {
		return new ProjectTasks('', languageTaskSuffix)
	}

	static class ProjectTasks implements NativeProjectTasks {
		private final String project
		private final String languageTaskSuffix
		private final String architecture
		private final String operatingSystemFamily
		private final String buildType = ''
		private final String binaryType

		ProjectTasks(String project, String languageTaskSuffix) {
			this(project, languageTaskSuffix, null, null, '')
		}

		ProjectTasks(String project, String languageTaskSuffix, String architecture, String operatingSystemFamily, String binaryType) {
			this.project = project
			this.operatingSystemFamily = operatingSystemFamily
			this.architecture = architecture
			this.languageTaskSuffix = languageTaskSuffix
			this.binaryType = binaryType
		}

		ProjectTasks withArchitecture(String architecture) {
			return new ProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType)
		}

		String withProject(String t) {
			project + ":" + t
		}

		String withVariant(String prefix) {
			return "${prefix}${variant.capitalize()}"
		}

		String withVariant(String prefix, String suffix) {
			return "${prefix}${variant.capitalize()}${suffix.capitalize()}"
		}

		ProjectTasks withOperatingSystemFamily(String operatingSystemFamily) {
			return new ProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType)
		}

		// TODO: Return a specialized type for shared library binary
		ProjectTasks getForSharedLibrary() {
			return new ProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, 'sharedLibrary')
		}
		String getBinary() {
			return withProject(withVariant("${binaryType}${buildType}"))
		}
		List<String> getAllToBinary() {
			return allToLink + [binary]
		}

		String getCompile() {
			return withProject(withVariant("compile${buildType}", languageTaskSuffix))
		}

		String getObjects() {
			return withProject(withVariant("objects${buildType}"))
		}

		String getLink() {
			return withProject(withVariant("link${buildType}"))
		}

		String getCreate() {
			return withProject(withVariant("create${buildType}"))
		}

		String getInstall() {
			return withProject(withVariant("install${buildType}"))
		}

		String getAssemble() {
			return withProject(withVariant("assemble${buildType}"))
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
