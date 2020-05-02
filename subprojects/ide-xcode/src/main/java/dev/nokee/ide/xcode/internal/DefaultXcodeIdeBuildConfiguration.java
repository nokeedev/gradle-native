package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.xcode.XcodeIdeBuildConfiguration;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class DefaultXcodeIdeBuildConfiguration implements XcodeIdeBuildConfiguration {
	@Getter private final String name;
	@Getter private final DefaultXcodeIdeBuildSettings buildSettings;

	@Inject
	public DefaultXcodeIdeBuildConfiguration(String name) {
		this.name = name;
		this.buildSettings = getObjects().newInstance(DefaultXcodeIdeBuildSettings.class);
	}

	@Inject
	protected abstract ObjectFactory getObjects();
}
