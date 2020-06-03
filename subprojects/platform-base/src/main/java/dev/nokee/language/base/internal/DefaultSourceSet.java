package dev.nokee.language.base.internal;

import javax.inject.Inject;

public abstract class DefaultSourceSet extends BaseSourceSet {
	@Inject
	public DefaultSourceSet(UTType type) {
		super(type);
	}
}
