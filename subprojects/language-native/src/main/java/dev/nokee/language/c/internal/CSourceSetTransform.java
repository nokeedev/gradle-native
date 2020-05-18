package dev.nokee.language.c.internal;

import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.nativebase.internal.NativeSourceSetTransform;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.DefaultTargetMachine;
import dev.nokee.platform.nativebase.internal.ToolChainSelectorInternal;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.nativeplatform.toolchain.internal.ToolType;

import javax.inject.Inject;

public abstract class CSourceSetTransform extends NativeSourceSetTransform<UTTypeCSource> {
	@Inject
	public CSourceSetTransform(NamingScheme names, DefaultTargetMachine targetMachine, ToolChainSelectorInternal toolChainSelector) {
		super(names, targetMachine, toolChainSelector);
	}

	@Override
	protected Class<? extends AbstractNativeSourceCompileTask> getCompileTaskType() {
		return CCompileTask.class;
	}

	@Override
	protected String getLanguageName() {
		return "C";
	}

	@Override
	protected ToolType getToolType() {
		return ToolType.C_COMPILER;
	}
}
