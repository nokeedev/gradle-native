package dev.nokee.language.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.language.base.internal.SourceSetTransform;
import dev.nokee.language.base.internal.UTType;
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

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.runtime.nativebase.internal.DependencyUtils.isFrameworkDependency;

public abstract class NativeSourceSetTransform<T extends UTType> implements SourceSetTransform<T, UTTypeObjectCode> {
	private final NamingScheme names;
	private final Configuration compileConfiguration;

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Inject
	public NativeSourceSetTransform(NamingScheme names, Configuration compileConfiguration) {
		this.names = names;
		this.compileConfiguration = compileConfiguration;
	}

	protected abstract Class<? extends AbstractNativeSourceCompileTask> getCompileTaskType();

	protected abstract String getLanguageName();

	@Override
	public SourceSet<UTTypeObjectCode> transform(SourceSet<T> sourceSet) {
		getCompilerInputs().value(fromCompileConfiguration()).finalizeValueOnRead();
		getCompilerInputs().disallowChanges();

		TaskProvider<? extends AbstractNativeSourceCompileTask> compileTask = getTasks().register(names.getTaskName("compile", getLanguageName()), getCompileTaskType(), task -> {
			task.getObjectFileDir().convention(getLayout().getBuildDirectory().dir(names.getOutputDirectoryBase("objs") + "/main" + getLanguageName()));

			// TODO: Select the right value based on the build type dimension, once modeled
			task.setDebuggable(false);
			task.setOptimized(false);
			task.setPositionIndependentCode(true);

			task.dependsOn(compileConfiguration);
			task.includes(getCompilerInputs().map(this::toHeaderSearchPaths));
			task.getCompilerArgs().addAll(getCompilerInputs().map(this::toFrameworkSearchPathFlags));

			task.getSource().from(sourceSet.getAsFileTree());
		});

		return Cast.uncheckedCast(getObjects().newInstance(GeneratedSourceSet.class, new UTTypeObjectCode(), compileTask.flatMap(AbstractNativeSourceCompileTask::getObjectFileDir), compileTask));
	}

	//region Compiler inputs
	public abstract ListProperty<CompilerInput> getCompilerInputs();

	private Provider<List<CompilerInput>> fromCompileConfiguration() {
		return getProviders().provider(() -> compileConfiguration.getIncoming().getArtifacts().getArtifacts().stream().map(CompilerInput::of).collect(Collectors.toList()));
	}

	private List<String> toFrameworkSearchPathFlags(List<CompilerInput> inputs) {
		return inputs.stream().filter(CompilerInput::isFramework).flatMap(it -> ImmutableList.of("-F", it.getFile().getParent()).stream()).collect(Collectors.toList());
	}

	private List<File> toHeaderSearchPaths(List<CompilerInput> inputs) {
		return inputs.stream().filter(it -> !it.isFramework()).map(CompilerInput::getFile).collect(Collectors.toList());
	}

	@Value
	public static class CompilerInput {
		boolean framework;
		File file;

		public static CompilerInput of(ResolvedArtifactResult result) {
			return new CompilerInput(isFrameworkDependency(result), result.getFile());
		}
	}
	//endregion
}
