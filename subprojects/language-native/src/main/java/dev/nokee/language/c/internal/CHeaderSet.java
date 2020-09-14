package dev.nokee.language.c.internal;

import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSet;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.io.File;

public class CHeaderSet extends BaseSourceSet implements HeaderExportingSourceSet {
	@Inject
	public CHeaderSet(String name, ObjectFactory objectFactory) {
		super(name, UTTypeCHeader.INSTANCE, objectFactory);
		sources.setFrom(getSourceDirectorySet().getSourceDirectories());
	}

	public File getHeaderDirectory() {
		return sources.getSingleFile();
	}
}
