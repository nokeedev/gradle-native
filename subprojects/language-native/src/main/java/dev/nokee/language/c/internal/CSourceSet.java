package dev.nokee.language.c.internal;

import dev.nokee.language.base.internal.BaseSourceSet;

import javax.inject.Inject;

public abstract class CSourceSet extends BaseSourceSet<UTTypeCSource> {
	@Inject
	public CSourceSet() {
		super(new UTTypeCSource());
	}
}
