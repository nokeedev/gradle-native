package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.xcode.XcodeIdeBuildConfiguration;
import dev.nokee.ide.xcode.XcodeIdeTarget;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class DefaultXcodeIdeTarget implements XcodeIdeTarget {
	@Getter private final String name;
	@Getter private final NamedDomainObjectContainer<XcodeIdeBuildConfiguration> buildConfigurations;

	@Inject
	public DefaultXcodeIdeTarget(String name) {
		this.name = name;
		this.buildConfigurations = getObjects().domainObjectContainer(XcodeIdeBuildConfiguration.class, this::newBuildConfiguration);
		getProductName().convention(getProductReference().map(this::toProductName));
	}

	// TODO: Use product type to better guide the conversion
	private String toProductName(String filename) {
		String result = FilenameUtils.removeExtension(filename);
		if (filename.startsWith("lib")) {
			result = result.substring(3);
		}
		return result;
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void buildConfigurations(@NonNull Action<? super NamedDomainObjectContainer<XcodeIdeBuildConfiguration>> action) {
		action.execute(buildConfigurations);
	}

	private XcodeIdeBuildConfiguration newBuildConfiguration(String name) {
		return getObjects().newInstance(DefaultXcodeIdeBuildConfiguration.class, name);
	}

	@Override
	public XcodeIdeTarget getIdeTarget() {
		return this;
	}
}
