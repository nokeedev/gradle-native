package dev.nokee.language.swift.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.language.base.internal.SourceSetTransform;
import dev.nokee.language.nativebase.internal.NativeSourceSetTransform;
import dev.nokee.language.nativebase.internal.UTTypeObjectCode;
import dev.nokee.language.objectivecpp.internal.UTTypeObjectiveCppSource;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.platform.base.internal.NamingScheme;
import lombok.Value;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.Cast;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.language.swift.SwiftVersion;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.runtime.nativebase.internal.DependencyUtils.isFrameworkDependency;

public abstract class SwiftSourceSetTransform implements SourceSetTransform<UTTypeSwiftSource, UTTypeObjectCode> {
	private final NamingScheme names;
	private final Configuration compileConfiguration;

	@Inject
	public SwiftSourceSetTransform(NamingScheme names, Configuration compileConfiguration) {
		this.names = names;
		this.compileConfiguration = compileConfiguration;
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public SourceSet<UTTypeObjectCode> transform(SourceSet<UTTypeSwiftSource> sourceSet) {
		TaskProvider<SwiftCompileTask> compileTask = getTasks().register(names.getTaskName("compile", "Swift"), SwiftCompileTask.class, task -> {
			task.getObjectFileDir().convention(getLayout().getBuildDirectory().dir(names.getOutputDirectoryBase("objs") + "/mainSwift"));

			// TODO: Select the right value based on the build type dimension, once modeled
			task.getDebuggable().set(false);
			task.getOptimized().set(false);

			task.getSourceCompatibility().set(SwiftVersion.SWIFT5);

			task.dependsOn(compileConfiguration);
			task.getCompilerArgs().addAll(getCompilerInputs().map(this::toFrameworkSearchPathFlags));

			task.getSource().from(sourceSet.getAsFileTree());
		});

		return Cast.uncheckedCast(getObjects().newInstance(GeneratedSourceSet.class, new UTTypeObjectCode(), compileTask.flatMap(SwiftCompileTask::getObjectFileDir), compileTask));
	}

	//region Compiler inputs
	public abstract ListProperty<NativeSourceSetTransform.CompilerInput> getCompilerInputs();

	private Provider<List<NativeSourceSetTransform.CompilerInput>> fromCompileConfiguration() {
		return getProviders().provider(() -> compileConfiguration.getIncoming().getArtifacts().getArtifacts().stream().map(NativeSourceSetTransform.CompilerInput::of).collect(Collectors.toList()));
	}

	private List<String> toFrameworkSearchPathFlags(List<NativeSourceSetTransform.CompilerInput> inputs) {
		return inputs.stream().filter(NativeSourceSetTransform.CompilerInput::isFramework).flatMap(it -> ImmutableList.of("-F", it.getFile().getParent()).stream()).collect(Collectors.toList());
	}

	private List<File> toHeaderSearchPaths(List<NativeSourceSetTransform.CompilerInput> inputs) {
		return inputs.stream().filter(it -> !it.isFramework()).map(NativeSourceSetTransform.CompilerInput::getFile).collect(Collectors.toList());
	}

	@Value
	public static class CompilerInput {
		boolean framework;
		File file;

		public static NativeSourceSetTransform.CompilerInput of(ResolvedArtifactResult result) {
			return new NativeSourceSetTransform.CompilerInput(isFrameworkDependency(result), result.getFile());
		}
	}
	//endregion
}
