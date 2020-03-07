package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.internal.BinaryInternal;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.cpp.CppBinary;

import javax.inject.Inject;

public abstract class JniLibraryInternal {
	private final DomainObjectSet<? super BinaryInternal> binaries;
	private final DomainObjectSet<? super LanguageSourceSetInternal> sources;
	private final Configuration implementation;
	private final ConfigurationContainer configurations;
	private final Configuration nativeRuntime;
	private JniJarBinaryInternal jarBinary;
	private SharedLibraryBinaryInternal sharedLibraryBinary;

	@Inject
	public JniLibraryInternal(ObjectFactory objectFactory, ConfigurationContainer configurations, DomainObjectSet<? super LanguageSourceSetInternal> sources, Configuration implementation) {
		binaries = objectFactory.domainObjectSet(BinaryInternal.class);
		this.configurations = configurations;
		this.sources = sources;
		this.implementation = implementation;

		Usage runtimeUsage = getObjectFactory().named(Usage.class, Usage.NATIVE_RUNTIME);
		// incoming runtime libraries (i.e. shared libraries) - this represents the libraries we consume
		nativeRuntime = configurations.create("nativeRuntime", it -> {
			it.setCanBeConsumed(false);
			it.extendsFrom(implementation);
			it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, runtimeUsage);
			it.getAttributes().attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, true);
			it.getAttributes().attribute(CppBinary.OPTIMIZED_ATTRIBUTE, false);
		});
		getNativeRuntimeFiles().from(nativeRuntime);
	}

	@Inject
	protected abstract ObjectFactory getObjectFactory();

	@Inject
	protected abstract TaskContainer getTasks();

	public DomainObjectSet<? super BinaryInternal> getBinaries() {
		return binaries;
	}

	public void registerSharedLibraryBinary() {
		sharedLibraryBinary = getObjectFactory().newInstance(SharedLibraryBinaryInternal.class, configurations, sources, implementation);
		getNativeRuntimeFiles().from(sharedLibraryBinary.getLinkedFile());
		binaries.add(sharedLibraryBinary);
	}

	public void registerJniJarBinary() {
		TaskProvider<Jar> jarTask = getTasks().register("jar", Jar.class);
		registerJniJarBinary(jarTask);
	}

	public JniJarBinaryInternal getJar() {
		return jarBinary;
	}

	public SharedLibraryBinaryInternal getSharedLibrary() {
		return sharedLibraryBinary;
	}

	public FileCollection getNativeRuntimeDependencies() {
		return nativeRuntime;
	}

	public abstract ConfigurableFileCollection getNativeRuntimeFiles();

	public void registerJniJarBinary(TaskProvider<Jar> jarTask) {
		jarBinary = getObjectFactory().newInstance(JniJarBinaryInternal.class, jarTask);
		binaries.add(jarBinary);
	}
}
