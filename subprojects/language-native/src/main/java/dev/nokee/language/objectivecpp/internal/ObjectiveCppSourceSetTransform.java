package dev.nokee.language.objectivecpp.internal;

import dev.nokee.language.nativebase.internal.NativeSourceSetTransform;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.platform.base.internal.NamingScheme;
import org.gradle.api.artifacts.Configuration;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;

import javax.inject.Inject;

public abstract class ObjectiveCppSourceSetTransform extends NativeSourceSetTransform<UTTypeObjectiveCppSource> {
	@Inject
	public ObjectiveCppSourceSetTransform(NamingScheme names, Configuration compileConfiguration) {
		super(names, compileConfiguration);
	}

	@Override
	protected Class<? extends AbstractNativeSourceCompileTask> getCompileTaskType() {
		return ObjectiveCppCompileTask.class;
	}

	@Override
	protected String getLanguageName() {
		return "ObjectiveCpp";
	}

}
