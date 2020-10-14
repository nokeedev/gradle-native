package dev.nokee.language.base.internal;

import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class DefaultSourceSet extends BaseSourceSet {
	@Inject
	public DefaultSourceSet(String name, UTType type, ObjectFactory objectFactory) {
		super(name, type, objectFactory);
	}
}
