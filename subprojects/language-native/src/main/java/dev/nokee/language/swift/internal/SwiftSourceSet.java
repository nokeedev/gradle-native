package dev.nokee.language.swift.internal;

import dev.nokee.language.base.internal.BaseSourceSet;

import javax.inject.Inject;

public abstract class SwiftSourceSet extends BaseSourceSet {
	@Inject
	public SwiftSourceSet() {
		super(UTTypeSwiftSource.INSTANCE);
	}
}
