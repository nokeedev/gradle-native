package dev.nokee.language.c.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSet;

import javax.inject.Inject;

public abstract class CHeaderSet extends BaseSourceSet implements HeaderExportingSourceSet {
	@Inject
	public CHeaderSet(String name) {
		super(name, UTTypeCHeader.INSTANCE);
	}
}
