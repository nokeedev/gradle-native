package dev.nokee.platform.jni.internal;

public class NamingSchemeFactory {
	private final String projectName;

	public NamingSchemeFactory(String projectName) {
		this.projectName = projectName;
	}

	public NamingScheme forMainComponent() {
		return NamingScheme.asMainComponent(projectName);
	}
}
