package dev.nokee.language.objectivec.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSet;

import javax.inject.Inject;

public abstract class ObjectiveCSourceSet extends BaseSourceSet implements HeaderExportingSourceSet {
	@Inject
	public ObjectiveCSourceSet() {
		super(UTTypeObjectiveCSource.INSTANCE);
	}
}
