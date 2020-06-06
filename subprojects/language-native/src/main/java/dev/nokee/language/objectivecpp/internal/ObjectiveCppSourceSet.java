package dev.nokee.language.objectivecpp.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSet;

import javax.inject.Inject;

public abstract class ObjectiveCppSourceSet extends BaseSourceSet implements HeaderExportingSourceSet {
	@Inject
	public ObjectiveCppSourceSet() {
		super(UTTypeObjectiveCppSource.INSTANCE);
	}
}
