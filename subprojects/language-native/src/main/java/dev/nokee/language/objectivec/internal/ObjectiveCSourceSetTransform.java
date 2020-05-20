package dev.nokee.language.objectivec.internal;

import dev.nokee.language.nativebase.internal.NativeSourceSetTransform;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.DefaultTargetMachine;
import dev.nokee.platform.nativebase.internal.ToolChainSelectorInternal;
import org.gradle.api.artifacts.Configuration;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.nativeplatform.toolchain.internal.ToolType;

import javax.inject.Inject;

public abstract class ObjectiveCSourceSetTransform extends NativeSourceSetTransform<UTTypeObjectiveCSource> {
	@Inject
	public ObjectiveCSourceSetTransform(NamingScheme names, DefaultTargetMachine targetMachine, ToolChainSelectorInternal toolChainSelector, Configuration compileConfiguration) {
		super(names, targetMachine, toolChainSelector, compileConfiguration);
	}

	@Override
	protected Class<? extends AbstractNativeSourceCompileTask> getCompileTaskType() {
		return ObjectiveCCompileTask.class;
	}

	@Override
	protected String getLanguageName() {
		return "ObjectiveC";
	}

	@Override
	protected ToolType getToolType() {
		return ToolType.OBJECTIVEC_COMPILER;
	}
}
