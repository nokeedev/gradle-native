package dev.nokee.language.objectivecpp.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSet;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class ObjectiveCppSourceSet extends BaseSourceSet implements HeaderExportingSourceSet {
	@Inject
	public ObjectiveCppSourceSet(String name, ObjectFactory objectFactory) {
		super(name, UTTypeObjectiveCppSource.INSTANCE, objectFactory);
	}
}
