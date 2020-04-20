package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSetInternal;
import dev.nokee.platform.base.internal.BinaryInternal;
import dev.nokee.platform.jni.internal.NamingScheme;
import lombok.Value;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.language.c.tasks.CCompile;
import org.gradle.language.cpp.tasks.CppCompile;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.objectivec.tasks.ObjectiveCCompile;
import org.gradle.language.objectivecpp.tasks.ObjectiveCppCompile;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;
import org.gradle.nativeplatform.tasks.LinkSharedLibrary;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class SharedLibraryBinaryInternal extends BinaryInternal {
	private static final Logger LOGGER = Logger.getLogger(SharedLibraryBinaryInternal.class.getName());
	private final Configuration compileConfiguration;
	private final Configuration linkConfiguration;
	private final TaskContainer tasks;
	private TaskProvider<LinkSharedLibrary> linkTask;
	private final DomainObjectSet<? super LanguageSourceSetInternal> sources;
	private final DefaultTargetMachine targetMachine;

	@Inject
	public SharedLibraryBinaryInternal(NamingScheme names, TaskContainer tasks, ConfigurationContainer configurations, ObjectFactory objectFactory, DomainObjectSet<LanguageSourceSetInternal> parentSources, Configuration implementation, DefaultTargetMachine targetMachine) {
		this.tasks = tasks;
		sources = objectFactory.domainObjectSet(LanguageSourceSetInternal.class);
		this.targetMachine = targetMachine;
		parentSources.all(it -> sources.add(it));

		getCompilerInputs().value(fromCompileConfiguration()).finalizeValueOnRead();
		getCompilerInputs().disallowChanges();
		getLinkerInputs().value(fromLinkConfiguration()).finalizeValueOnRead();
		getLinkerInputs().disallowChanges();

		ConfigurationUtils configurationUtils = objectFactory.newInstance(ConfigurationUtils.class);
		this.compileConfiguration = configurations.create(names.getConfigurationName("headerSearchPaths"), configurationUtils.asIncomingHeaderSearchPathFrom(implementation));
		this.linkConfiguration = configurations.create(names.getConfigurationName("nativeLink"), configurationUtils.asIncomingLinkLibrariesFrom(implementation).forTargetMachine(targetMachine).asDebug());
	}

	public TaskProvider<LinkSharedLibrary> getLinkTask() {
		return linkTask;
	}

	@Inject
	protected abstract ProviderFactory getProviderFactory();

	public void configureSoftwareModelBinary(SharedLibraryBinarySpec binary) {
		binary.getTasks().withType(CppCompile.class, this::configureCompileTask);
		binary.getTasks().withType(CCompile.class, this::configureCompileTask);
		binary.getTasks().withType(ObjectiveCCompile.class, this::configureCompileTask);
		binary.getTasks().withType(ObjectiveCppCompile.class, this::configureCompileTask);

		binary.getTasks().withType(LinkSharedLibrary.class, task -> {
			linkTask = tasks.named(task.getName(), LinkSharedLibrary.class);

			task.dependsOn(linkConfiguration);
			task.getLibs().from(getLinkerInputs().map(this::toLinkLibraries));
			task.getLinkerArgs().addAll(getLinkerInputs().map(this::toFrameworkFlags));
		});
	}

	public abstract RegularFileProperty getLinkedFile();

	private void configureCompileTask(AbstractNativeCompileTask task) {
		// configure includes using the native incoming compile configuration
		task.dependsOn(compileConfiguration);
		task.setDebuggable(true);
		task.setOptimized(false);
		task.includes(getCompilerInputs().map(this::toHeaderSearchPaths));

		sources.withType(HeaderExportingSourceSetInternal.class, sourceSet -> task.getIncludes().from(sourceSet.getSource()));

		task.getIncludes().from(getJvmIncludes());
		task.getCompilerArgs().addAll(getCompilerInputs().map(this::toFrameworkSearchPathFlags));
	}

	private Provider<List<File>> getJvmIncludes() {
		return getProviderFactory().provider(() -> {
			List<File> result = new ArrayList<>();
			result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include"));

			if (OperatingSystem.current().isMacOsX()) {
				result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/darwin"));
			} else if (OperatingSystem.current().isLinux()) {
				result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/linux"));
			} else if (OperatingSystem.current().isWindows()) {
				result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/win32"));
			}
			return result;
		});
	}

	private static boolean isFrameworkDependency(ResolvedArtifactResult result) {
		Optional<Attribute<?>> attribute = result.getVariant().getAttributes().keySet().stream().filter(it -> it.getName().equals(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE.getName())).findFirst();
		if (attribute.isPresent()) {
			String v = result.getVariant().getAttributes().getAttribute(attribute.get()).toString();
			if (v.equals(LibraryElements.FRAMEWORK_BUNDLE)) {
				return true;
			}
			return false;
		}
		LOGGER.finest(() -> "No library elements on dependency\n" + result.getVariant().getAttributes().keySet().stream().map(Attribute::getName).collect(Collectors.joining(", ")));
		return false;
	}

	//region Compiler inputs
	public abstract ListProperty<CompilerInput> getCompilerInputs();

	private Provider<List<CompilerInput>> fromCompileConfiguration() {
		return getProviderFactory().provider(() -> compileConfiguration.getIncoming().getArtifacts().getArtifacts().stream().map(CompilerInput::of).collect(Collectors.toList()));
	}

	private List<String> toFrameworkSearchPathFlags(List<CompilerInput> inputs) {
		return inputs.stream().filter(CompilerInput::isFramework).flatMap(it -> ImmutableList.of("-F", it.getFile().getParent()).stream()).collect(Collectors.toList());
	}

	private List<File> toHeaderSearchPaths(List<CompilerInput> inputs) {
		return inputs.stream().filter(it -> !it.isFramework()).map(CompilerInput::getFile).collect(Collectors.toList());
	}

	@Value
	static class CompilerInput {
		boolean framework;
		File file;

		public static CompilerInput of(ResolvedArtifactResult result) {
			return new CompilerInput(isFrameworkDependency(result), result.getFile());
		}
	}
	//endregion

	//region Linker inputs
	private Provider<List<LinkerInput>> fromLinkConfiguration() {
		return getProviderFactory().provider(() -> linkConfiguration.getIncoming().getArtifacts().getArtifacts().stream().map(LinkerInput::of).collect(Collectors.toList()));
	}

	public abstract ListProperty<LinkerInput> getLinkerInputs();

	private List<String> toFrameworkFlags(List<LinkerInput> inputs) {
		return inputs.stream().filter(LinkerInput::isFramework).flatMap(it -> ImmutableList.of("-F", it.getFile().getParent(), "-framework", it.getFile().getName().substring(0, it.getFile().getName().lastIndexOf("."))).stream()).collect(Collectors.toList());
	}

	private List<File> toLinkLibraries(List<LinkerInput> inputs) {
		return inputs.stream().filter(it -> !it.isFramework()).map(LinkerInput::getFile).collect(Collectors.toList());
	}

	@Value
	static class LinkerInput {
		boolean framework;
		File file;

		public static LinkerInput of(ResolvedArtifactResult result) {
			return new LinkerInput(isFrameworkDependency(result), result.getFile());
		}
	}
	//endregion
}
