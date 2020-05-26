package dev.nokee.language.objectivec.internal;

import dev.nokee.language.nativebase.internal.NativeSourceSetTransform;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;
import dev.nokee.platform.base.internal.NamingScheme;
import org.gradle.api.artifacts.Configuration;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;

import javax.inject.Inject;

public abstract class ObjectiveCSourceSetTransform extends NativeSourceSetTransform<UTTypeObjectiveCSource> {
	@Inject
	public ObjectiveCSourceSetTransform(NamingScheme names, Configuration compileConfiguration) {
		super(names, compileConfiguration);
	}

	@Override
	protected Class<? extends AbstractNativeSourceCompileTask> getCompileTaskType() {
		return ObjectiveCCompileTask.class;
	}

	@Override
	protected String getLanguageName() {
		return "ObjectiveC";
	}

}
