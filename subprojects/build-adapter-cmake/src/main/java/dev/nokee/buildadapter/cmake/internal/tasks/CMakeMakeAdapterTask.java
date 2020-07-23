package dev.nokee.buildadapter.cmake.internal.tasks;

import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.ProcessBuilderEngine;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class CMakeMakeAdapterTask extends DefaultTask {
	@Input
	public abstract Property<String> getTargetName();

	@Internal
	public abstract Property<CommandLineTool> getMakeTool();

	@Internal
	public abstract DirectoryProperty getWorkingDirectory();

	@OutputFile
	public abstract RegularFileProperty getBuiltFile();

	@TaskAction
	private void doExec() {
		getMakeTool().get().withArguments(getTargetName().get()).newInvocation().workingDirectory(getWorkingDirectory().get().getAsFile()).buildAndSubmit(new ProcessBuilderEngine()).waitFor().assertNormalExitValue();
	}
}
