package dev.nokee.language.base.internal;

import javax.inject.Inject;

public abstract class DefaultSourceSet<T extends UTType> extends BaseSourceSet<T> {
	@Inject
	public DefaultSourceSet(UTType type) {
		super(type);
	}
}
