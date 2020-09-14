package dev.nokee.language.cpp.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSet;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class CppSourceSet extends BaseSourceSet implements HeaderExportingSourceSet {
	@Inject
	public CppSourceSet(String name, ObjectFactory objectFactory) {
		super(name, UTTypeCppSource.INSTANCE, objectFactory);
	}
}
