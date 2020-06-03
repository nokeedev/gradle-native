package dev.nokee.language.cpp.internal;

import dev.nokee.language.base.internal.BaseSourceSet;

import javax.inject.Inject;

public abstract class CppSourceSet extends BaseSourceSet {
	@Inject
	public CppSourceSet() {
		super(new UTTypeCppSource());
	}
}
