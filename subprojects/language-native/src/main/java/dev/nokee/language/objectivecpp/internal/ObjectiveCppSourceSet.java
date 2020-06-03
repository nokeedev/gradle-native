package dev.nokee.language.objectivecpp.internal;

import dev.nokee.language.base.internal.BaseSourceSet;

import javax.inject.Inject;

public abstract class ObjectiveCppSourceSet extends BaseSourceSet {
	@Inject
	public ObjectiveCppSourceSet() {
		super(UTTypeObjectiveCppSource.INSTANCE);
	}
}
