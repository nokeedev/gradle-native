package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSetInternal;
import dev.nokee.platform.base.internal.BinaryInternal;
import dev.nokee.platform.jni.internal.NamingScheme;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.language.c.tasks.CCompile;
import org.gradle.language.cpp.CppBinary;
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

public abstract class SharedLibraryBinaryInternal extends BinaryInternal {
	private final Configuration cppCompile;
	private final Configuration cppLinkDebug;
	private final TaskContainer tasks;
	private TaskProvider<LinkSharedLibrary> linkTask;
	private final DomainObjectSet<? super LanguageSourceSetInternal> sources;

	@Inject
	public SharedLibraryBinaryInternal(NamingScheme names, TaskContainer tasks, ConfigurationContainer configurations, ObjectFactory objectFactory, DomainObjectSet<LanguageSourceSetInternal> parentSources, Configuration implementation) {
		this.tasks = tasks;
		sources = objectFactory.domainObjectSet(LanguageSourceSetInternal.class);
		parentSources.all(it -> sources.add(it));

		/*
		 * Define some configurations to present the outputs of this build
		 * to other Gradle projects.
		 */
		final Usage cppApiUsage = objectFactory.named(Usage.class, Usage.C_PLUS_PLUS_API);
		final Usage linkUsage = objectFactory.named(Usage.class, Usage.NATIVE_LINK);

		// incoming compile time headers - this represents the headers we consume
		this.cppCompile = configurations.create(names.getConfigurationName("headerSearchPaths"), it -> {
			it.setCanBeConsumed(false);
			it.extendsFrom(implementation);
			it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, cppApiUsage);
		});

		// incoming linktime libraries (i.e. static libraries) - this represents the libraries we consume
		this.cppLinkDebug = configurations.create(names.getConfigurationName("nativeLink"), it -> {
			it.setCanBeConsumed(false);
			it.extendsFrom(implementation);
			it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, linkUsage);
			it.getAttributes().attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, true);
			it.getAttributes().attribute(CppBinary.OPTIMIZED_ATTRIBUTE, false);
		});
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
			task.getLibs().from(cppLinkDebug);
			linkTask = tasks.named(task.getName(), LinkSharedLibrary.class);
		});
	}

	public abstract RegularFileProperty getLinkedFile();

	private void configureCompileTask(AbstractNativeCompileTask task) {
		// configure includes using the native incoming compile configuration
		task.setDebuggable(true);
		task.setOptimized(false);
		task.includes(cppCompile);

		sources.withType(HeaderExportingSourceSetInternal.class, sourceSet -> task.getIncludes().from(sourceSet.getSource()));

		task.getIncludes().from(getJvmIncludes());
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
}
