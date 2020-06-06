package dev.nokee.language.cpp.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSet;

import javax.inject.Inject;

public abstract class CppSourceSet extends BaseSourceSet implements HeaderExportingSourceSet {
	@Inject
	public CppSourceSet() {
		super(new UTTypeCppSource());
	}
}
