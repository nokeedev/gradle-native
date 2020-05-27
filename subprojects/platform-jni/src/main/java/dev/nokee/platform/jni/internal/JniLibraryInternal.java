package dev.nokee.platform.jni.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.nativebase.internal.UTTypeObjectCode;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JniLibraryNativeDependencies;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.Cast;
import org.gradle.jvm.tasks.Jar;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static dev.nokee.platform.base.internal.TaskUtils.dependsOn;

public abstract class JniLibraryInternal implements JniLibrary, Named {
	private final NamingScheme names;
	private final JniLibraryNativeDependenciesInternal dependencies;
	private final DomainObjectSet<BinaryInternal> binaryCollection;
	private final DomainObjectSet<LanguageSourceSetInternal> sources;
	private final Configuration implementation;
	private final DefaultTargetMachine targetMachine;
	private final GroupId groupId;
	private final Configuration nativeRuntime;
	private AbstractJarBinary jarBinary;
	private SharedLibraryBinaryInternal sharedLibraryBinary;
	private final String name;

	@Inject
	public JniLibraryInternal(String name, NamingScheme names, DomainObjectSet<LanguageSourceSetInternal> parentSources, Configuration implementation, BuildVariant buildVariant, GroupId groupId, DomainObjectSet<BinaryInternal> parentBinaries, JniLibraryNativeDependenciesInternal dependencies) {
		this.name = name;
		this.names = names;
		this.dependencies = dependencies;
		binaryCollection = getObjects().domainObjectSet(BinaryInternal.class);
		this.sources = getObjects().domainObjectSet(LanguageSourceSetInternal.class);
		this.implementation = implementation;
		this.targetMachine = new DefaultTargetMachine((DefaultOperatingSystemFamily)buildVariant.getDimensions().get(0), (DefaultMachineArchitecture)buildVariant.getDimensions().get(1));
		this.groupId = groupId;

		parentSources.all(sources::add);

		binaryCollection.configureEach( binary -> {
			parentBinaries.add(binary);
		});

		ConfigurationUtils configurationUtils = getObjects().newInstance(ConfigurationUtils.class);
		nativeRuntime = getConfigurations().create(names.getConfigurationName("nativeRuntimeLibraries"),
			configurationUtils.asIncomingRuntimeLibrariesFrom(dependencies.getNativeDependencies(), dependencies.getNativeRuntimeOnlyDependencies())
				.forTargetMachine(targetMachine)
				.asDebug()
				.withDescription("Runtime libraries for JNI shared library."));

		getNativeRuntimeFiles().from(nativeRuntime);
		getResourcePath().convention(getProviders().provider(() -> names.getResourcePath(groupId)));
	}

	// CAUTION: Never rely on the name of the variant, it isn't exposed on the public type!
	@Override
	public String getName() {
		return name;
	}

	public DomainObjectSet<LanguageSourceSetInternal> getSources() {
		return sources;
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Inject
	protected abstract TaskContainer getTasks();

	public BinaryView<Binary> getBinaries() {
		return Cast.uncheckedCast(getObjects().newInstance(DefaultBinaryView.class, binaryCollection, (Realizable)() -> {}));
	}

	public void registerSharedLibraryBinary(List<GeneratedSourceSet<UTTypeObjectCode>> objectSourceSets, TaskProvider<LinkSharedLibraryTask> linkTask, boolean multipleVariants) {
		SharedLibraryBinaryInternal sharedLibraryBinary = getObjects().newInstance(SharedLibraryBinaryInternal.class, names, sources, implementation, targetMachine, objectSourceSets, linkTask, dependencies.getNativeLinkOnlyDependencies());
		getNativeRuntimeFiles().from(linkTask.flatMap(AbstractLinkTask::getLinkedFile));
		this.sharedLibraryBinary = sharedLibraryBinary;
		binaryCollection.add(sharedLibraryBinary);
		getAssembleTask().configure(dependsOn(sharedLibraryBinary.getLinkTask()));
	}

	public void registerJniJarBinary() {
		TaskProvider<Jar> jarTask = getTasks().named(names.getTaskName("jar"), Jar.class);
		addJniJarBinary(getObjects().newInstance(DefaultJniJarBinary.class, jarTask));
	}

	public AbstractJarBinary getJar() {
		return jarBinary;
	}

	public SharedLibraryBinaryInternal getSharedLibrary() {
		return sharedLibraryBinary;
	}

	@Override
	public void sharedLibrary(Action<? super SharedLibraryBinary> action) {
		action.execute(sharedLibraryBinary);
	}

	public FileCollection getNativeRuntimeDependencies() {
		return nativeRuntime;
	}

	public void addJniJarBinary(AbstractJarBinary jniJarBinary) {
		jarBinary = jniJarBinary;
		binaryCollection.add(jniJarBinary);
		getAssembleTask().configure(dependsOn(jniJarBinary.getJarTask()));
	}

	public void addJvmJarBinary(DefaultJvmJarBinary jvmJarBinary) {
		binaryCollection.add(jvmJarBinary);
		getAssembleTask().configure(dependsOn(jvmJarBinary.getJarTask()));
	}

	public DefaultTargetMachine getTargetMachine() {
		return targetMachine;
	}

	public TaskProvider<Task> getAssembleTask() {
		return getTasks().named(names.getTaskName("assemble"));
	}

	@Override
	public JniLibraryNativeDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public void dependencies(Action<? super JniLibraryNativeDependencies> action) {
		action.execute(dependencies);
	}
}
