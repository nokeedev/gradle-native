package dev.nokee.language.swift.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class SwiftSourceSet extends BaseSourceSet {
	@Inject
	public SwiftSourceSet(String name, ObjectFactory objectFactory) {
		super(name, UTTypeSwiftSource.INSTANCE, objectFactory);
	}
}
