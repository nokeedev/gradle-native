package dev.nokee.language

class DefaultNativeProjectTasks implements NativeProjectTasks {
	private final String project
	private final String languageTaskSuffix
	private final String architecture
	private final String operatingSystemFamily
	private final String buildType
	private final String binaryType
	private final String linkage

	DefaultNativeProjectTasks(String project, String languageTaskSuffix) {
		this(project, languageTaskSuffix, null, null, '', null, '')
	}

	DefaultNativeProjectTasks(String project, String languageTaskSuffix, String architecture, String operatingSystemFamily, String binaryType, String linkage, String buildType) {
		this.project = project
		this.operatingSystemFamily = operatingSystemFamily
		this.architecture = architecture
		this.languageTaskSuffix = languageTaskSuffix
		this.binaryType = binaryType
		this.linkage = linkage
		this.buildType = buildType
	}

	DefaultNativeProjectTasks withArchitecture(String architecture) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType, linkage, buildType)
	}

	DefaultNativeProjectTasks withLinkage(String linkage) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType, linkage, buildType)
	}

	DefaultNativeProjectTasks withProjectPath(String project) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType, linkage, buildType)
	}

	DefaultNativeProjectTasks withComponentName(String componentName) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType, linkage, componentName)
	}

	// TODO: This is part of the public API but shouldn't really... used when composing
	String withProject(String t) {
		project + ":" + t
	}

	// TODO: This is part of the public API but shouldn't really... used when composing
	String withVariant(String prefix) {
		return "${prefix}${variant.capitalize()}"
	}

	// TODO: This is part of the public API but shouldn't really... used when composing
	String withVariant(String prefix, String suffix) {
		return "${prefix}${variant.capitalize()}${suffix.capitalize()}"
	}

	DefaultNativeProjectTasks withOperatingSystemFamily(String operatingSystemFamily) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType, linkage, buildType)
	}

	// TODO: Return a specialized type for shared library binary
	DefaultNativeProjectTasks getForSharedLibrary() {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, 'sharedLibrary', linkage, buildType)
	}
	DefaultNativeProjectTasks getForStaticLibrary() {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, 'staticLibrary', linkage, buildType)
	}
	String getBinary() {
		return withProject(withVariant("${binaryType}${buildType.capitalize()}"))
	}
	List<String> getAllToBinary() {
		return allToLink + [binary]
	}

	String getCompile() {
		return withProject(withVariant("compile", languageTaskSuffix))
	}

	String getObjects() {
		return withProject(withVariant("objects"))
	}

	String getLink() {
		return withProject(withVariant("link"))
	}

	String getCreate() {
		return withProject(withVariant("create"))
	}

	String getLinkOrCreate() {
		if (binaryType == 'staticLibrary') {
			return create
		}
		return link
	}

	String getInstall() {
		return withProject(withVariant("install"))
	}

	String getAssemble() {
		return withProject(withVariant("assemble"))
	}

	List<String> getAllToObjects() {
		return [compile, objects]
	}

	List<String> getAllToLifecycleObjects() {
		return [compile, withProject('objects')]
	}

	List<String> getAllToCreate() {
		return [compile, create]
	}

	List<String> getAllToLink() {
		return [compile, link]
	}

	List<String> getAllToLinkOrCreate() {
		return [compile, linkOrCreate]
	}

	List<String> getAllToInstall() {
		return allToLink + [install]
	}

	List<String> getAllToLifecycleAssemble() {
		return allToLinkOrCreate + [withProject('assemble')]
	}

	List<String> getAllToAssemble() {
		return allToLinkOrCreate + [assemble]
	}

	List<String> getAllToAssembleWithInstall() {
		return allToInstall + [assemble]
	}

	List<String> getAllToTest() {
		return allToLink + [withProject('relocateMainSymbolForTest'), withProject('test')]
	}

	protected String getVariant() {
		String result = "${buildType.capitalize()}"
		if (linkage != null) {
			result += linkage.toLowerCase().capitalize()
		}
		if (operatingSystemFamily != null) {
			result += operatingSystemFamily.toLowerCase().capitalize()
		}
		if (architecture != null) {
			result += architecture.toLowerCase().capitalize()
		}
		return result
	}
}
