package dev.nokee.buildadapter.cmake.internal.tasks;

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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class CMakeMSBuildAdapterTask extends DefaultTask {
	@Input
	public abstract Property<String> getTargetName();

	@Input
	public abstract Property<String> getConfigurationName();

	@Internal
	public abstract DirectoryProperty getWorkingDirectory();

	@OutputFile
	public abstract RegularFileProperty getBuiltFile();

	@TaskAction
	private void doExec() throws IOException {
		CommandLineTool.of(new File("C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\Community\\MSBuild\\Current\\Bin\\MSBuild.exe")).withArguments("/p:Configuration=" + getConfigurationName().get(), findVcxproj()).newInvocation().workingDirectory(getWorkingDirectory().get().getAsFile()).buildAndSubmit(new ProcessBuilderEngine()).waitFor().assertNormalExitValue();
	}

	private String findVcxproj() throws IOException {
		Path result = Files.walk(getWorkingDirectory().get().getAsFile().toPath()).filter(it -> it.toFile().getName().equals(getTargetName().get() + ".vcxproj")).findFirst().get();
		return getWorkingDirectory().getAsFile().get().toPath().relativize(result).toString();
	}
}
