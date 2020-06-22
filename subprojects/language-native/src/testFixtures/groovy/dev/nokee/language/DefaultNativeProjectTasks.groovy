package dev.nokee.language

class DefaultNativeProjectTasks implements NativeProjectTasks {
	private final String project
	private final String languageTaskSuffix
	private final String architecture
	private final String operatingSystemFamily
	private final String buildType = ''
	private final String binaryType

	DefaultNativeProjectTasks(String project, String languageTaskSuffix) {
		this(project, languageTaskSuffix, null, null, '')
	}

	DefaultNativeProjectTasks(String project, String languageTaskSuffix, String architecture, String operatingSystemFamily, String binaryType) {
		this.project = project
		this.operatingSystemFamily = operatingSystemFamily
		this.architecture = architecture
		this.languageTaskSuffix = languageTaskSuffix
		this.binaryType = binaryType
	}

	DefaultNativeProjectTasks withArchitecture(String architecture) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType)
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

	DefaultNativeProjectTasks withOperatingSystemFamily(String operatingSystemFamily) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType)
	}

	// TODO: Return a specialized type for shared library binary
	DefaultNativeProjectTasks getForSharedLibrary() {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, 'sharedLibrary')
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
