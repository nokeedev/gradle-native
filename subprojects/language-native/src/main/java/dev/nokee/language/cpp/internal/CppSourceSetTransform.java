package dev.nokee.language.cpp.internal;

import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.nativebase.internal.NativeSourceSetTransform;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.DefaultTargetMachine;
import dev.nokee.platform.nativebase.internal.ToolChainSelectorInternal;
import org.gradle.api.artifacts.Configuration;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.nativeplatform.toolchain.internal.ToolType;

import javax.inject.Inject;

public abstract class CppSourceSetTransform extends NativeSourceSetTransform<UTTypeCppSource> {
	@Inject
	public CppSourceSetTransform(NamingScheme names, DefaultTargetMachine targetMachine, ToolChainSelectorInternal toolChainSelector, Configuration compileConfiguration) {
		super(names, targetMachine, toolChainSelector, compileConfiguration);
	}

	@Override
	protected Class<? extends AbstractNativeSourceCompileTask> getCompileTaskType() {
		return CppCompileTask.class;
	}

	@Override
	protected String getLanguageName() {
		return "Cpp";
	}

	@Override
	protected ToolType getToolType() {
		return ToolType.CPP_COMPILER;
	}
}
