package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.xcode.XcodeIdeGroup;
import lombok.Getter;

import javax.inject.Inject;

public abstract class DefaultXcodeIdeGroup implements XcodeIdeGroup {
	@Getter private final String name;

	@Inject
	public DefaultXcodeIdeGroup(String name) {
		this.name = name;
	}
}
