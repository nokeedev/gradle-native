package dev.nokee.language.c.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSet;

import javax.inject.Inject;

public abstract class CSourceSet extends BaseSourceSet implements HeaderExportingSourceSet {
	@Inject
	public CSourceSet() {
		super(UTTypeCSource.INSTANCE);
	}
}
