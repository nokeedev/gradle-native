package dev.nokee.language.objectivec.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSet;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class ObjectiveCSourceSet extends BaseSourceSet implements HeaderExportingSourceSet {
	@Inject
	public ObjectiveCSourceSet(String name, ObjectFactory objectFactory) {
		super(name, UTTypeObjectiveCSource.INSTANCE, objectFactory);
	}
}
