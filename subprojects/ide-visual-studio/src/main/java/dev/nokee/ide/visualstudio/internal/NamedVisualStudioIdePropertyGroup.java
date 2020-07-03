package dev.nokee.ide.visualstudio.internal;

import lombok.Getter;
import org.gradle.api.Named;

import javax.inject.Inject;

public abstract class NamedVisualStudioIdePropertyGroup extends DefaultVisualStudioIdePropertyGroup implements Named {
	@Getter private final String name;

	@Inject
	public NamedVisualStudioIdePropertyGroup(String name) {
		this.name = name;
	}
}
