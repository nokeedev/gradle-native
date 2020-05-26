package dev.nokee.language.c.internal;

import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.nativebase.internal.NativeSourceSetTransform;
import dev.nokee.platform.base.internal.NamingScheme;
import org.gradle.api.artifacts.Configuration;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;

import javax.inject.Inject;

public abstract class CSourceSetTransform extends NativeSourceSetTransform<UTTypeCSource> {
	@Inject
	public CSourceSetTransform(NamingScheme names, Configuration compileConfiguration) {
		super(names, compileConfiguration);
	}

	@Override
	protected Class<? extends AbstractNativeSourceCompileTask> getCompileTaskType() {
		return CCompileTask.class;
	}

	@Override
	protected String getLanguageName() {
		return "C";
	}

}
