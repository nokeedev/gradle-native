package dev.nokee.language.cpp.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSet;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.io.File;

public class CppHeaderSet extends BaseSourceSet implements HeaderExportingSourceSet {
	@Inject
	public CppHeaderSet(String name, ObjectFactory objectFactory) {
		super(name, UTTypeCppHeader.INSTANCE, objectFactory);
		sources.setFrom(getSourceDirectorySet().getSourceDirectories());
	}

	public File getHeaderDirectory() {
		return sources.getSingleFile();
	}
}
