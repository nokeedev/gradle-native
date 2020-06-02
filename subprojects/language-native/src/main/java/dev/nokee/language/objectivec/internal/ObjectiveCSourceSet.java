package dev.nokee.language.objectivec.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.base.internal.UTType;

import javax.inject.Inject;

public abstract class ObjectiveCSourceSet extends BaseSourceSet<UTTypeObjectiveCSource> {
	@Inject
	public ObjectiveCSourceSet() {
		super(UTTypeObjectiveCSource.INSTANCE);
	}
}
