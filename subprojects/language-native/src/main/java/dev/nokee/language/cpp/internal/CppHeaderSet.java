package dev.nokee.language.cpp.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSet;

import javax.inject.Inject;

public abstract class CppHeaderSet extends BaseSourceSet implements HeaderExportingSourceSet {
	@Inject
	public CppHeaderSet(String name) {
		super(name, UTTypeCppHeader.INSTANCE);
	}
}
