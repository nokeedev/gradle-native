package dev.nokee.language.cpp.internal;

import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.nativebase.internal.NativeSourceSetTransform;
import dev.nokee.platform.base.internal.NamingScheme;
import org.gradle.api.artifacts.Configuration;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;

import javax.inject.Inject;

public abstract class CppSourceSetTransform extends NativeSourceSetTransform<UTTypeCppSource> {
	@Inject
	public CppSourceSetTransform(NamingScheme names, Configuration compileConfiguration) {
		super(names, compileConfiguration);
	}

	@Override
	protected Class<? extends AbstractNativeSourceCompileTask> getCompileTaskType() {
		return CppCompileTask.class;
	}

	@Override
	protected String getLanguageName() {
		return "Cpp";
	}
}
