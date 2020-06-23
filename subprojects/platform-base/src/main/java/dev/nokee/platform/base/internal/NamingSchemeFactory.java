package dev.nokee.platform.base.internal;

import org.apache.commons.lang3.StringUtils;

public class NamingSchemeFactory {
	private final String projectName;

	public NamingSchemeFactory(String projectName) {
		this.projectName = projectName;
	}

	public NamingScheme forMainComponent() {
		return NamingScheme.asMainComponent(projectName);
	}

	public NamingScheme forMainComponent(String suffix) {
		return NamingScheme.asComponent(projectName + StringUtils.capitalize(suffix), suffix);
	}
}
