package dev.nokee.language.objectivecpp.internal;

import dev.nokee.language.c.internal.UTTypeCSource;
import dev.nokee.language.cpp.internal.UTTypeCppSource;
import dev.nokee.language.nativebase.internal.NativeSourceSetTransform;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.DefaultTargetMachine;
import dev.nokee.platform.nativebase.internal.ToolChainSelectorInternal;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.nativeplatform.toolchain.internal.ToolType;

import javax.inject.Inject;

public abstract class ObjectiveCppSourceSetTransform extends NativeSourceSetTransform<UTTypeObjectiveCppSource> {
	@Inject
	public ObjectiveCppSourceSetTransform(NamingScheme names, DefaultTargetMachine targetMachine, ToolChainSelectorInternal toolChainSelector) {
		super(names, targetMachine, toolChainSelector);
	}

	@Override
	protected Class<? extends AbstractNativeSourceCompileTask> getCompileTaskType() {
		return ObjectiveCppCompileTask.class;
	}

	@Override
	protected String getLanguageName() {
		return "ObjectiveCpp";
	}

	@Override
	protected ToolType getToolType() {
		return ToolType.OBJECTIVECPP_COMPILER;
	}
}
