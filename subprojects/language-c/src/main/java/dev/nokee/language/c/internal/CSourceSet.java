package dev.nokee.language.c.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSet;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class CSourceSet extends BaseSourceSet implements HeaderExportingSourceSet {
	@Inject
	public CSourceSet(String name, ObjectFactory objectFactory) {
		super(name, UTTypeCSource.INSTANCE, objectFactory);
	}
}
