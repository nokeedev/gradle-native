package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.nativebase.internal.UTTypeObjectCode;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JniLibraryNativeDependencies;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import javax.inject.Inject;

public abstract class JniLibraryInternal extends BaseVariant implements JniLibrary {
	private final NamingScheme names;
	private final JniLibraryNativeDependenciesInternal dependencies;
	private final DomainObjectSet<LanguageSourceSetInternal> sources;
	private final DefaultTargetMachine targetMachine;
	private final GroupId groupId;
	private final Configuration nativeRuntime;
	private AbstractJarBinary jarBinary;
	private SharedLibraryBinaryInternal sharedLibraryBinary;

	@Inject
	public JniLibraryInternal(String name, NamingScheme names, DomainObjectSet<LanguageSourceSetInternal> parentSources, BuildVariant buildVariant, GroupId groupId, DomainObjectSet<Binary> parentBinaries, JniLibraryNativeDependenciesInternal dependencies) {
		super(name, buildVariant);
		this.names = names;
		this.dependencies = dependencies;
		this.sources = getObjects().domainObjectSet(LanguageSourceSetInternal.class);
		this.targetMachine = new DefaultTargetMachine((DefaultOperatingSystemFamily)buildVariant.getDimensions().get(0), (DefaultMachineArchitecture)buildVariant.getDimensions().get(1));
		this.groupId = groupId;

		parentSources.all(sources::add);

		getBinaryCollection().configureEach(parentBinaries::add);

		ConfigurationUtils configurationUtils = getObjects().newInstance(ConfigurationUtils.class);
		nativeRuntime = getConfigurations().create(names.getConfigurationName("nativeRuntimeLibraries"),
			configurationUtils.asIncomingRuntimeLibrariesFrom(dependencies.getNativeImplementationDependencies(), dependencies.getNativeRuntimeOnlyDependencies())
				.forTargetMachine(targetMachine)
				.asDebug()
				.withDescription("Runtime libraries for JNI shared library."));

		getNativeRuntimeFiles().from(nativeRuntime);
		getResourcePath().convention(getProviders().provider(() -> names.getResourcePath(groupId)));
	}

	public DomainObjectSet<LanguageSourceSetInternal> getSources() {
		return sources;
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Inject
	protected abstract TaskContainer getTasks();

	public void registerSharedLibraryBinary(DomainObjectSet<GeneratedSourceSet<UTTypeObjectCode>> objectSourceSets, TaskProvider<LinkSharedLibraryTask> linkTask, boolean multipleVariants) {
		SharedLibraryBinaryInternal sharedLibraryBinary = getObjects().newInstance(SharedLibraryBinaryInternal.class, names, sources, dependencies.getNativeImplementationDependencies(), targetMachine, objectSourceSets, linkTask, dependencies.getNativeLinkOnlyDependencies());
		getNativeRuntimeFiles().from(linkTask.flatMap(AbstractLinkTask::getLinkedFile));
		this.sharedLibraryBinary = sharedLibraryBinary;
		getBinaryCollection().add(sharedLibraryBinary);
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
		getBinaryCollection().add(jniJarBinary);
	}

	public void addJvmJarBinary(DefaultJvmJarBinary jvmJarBinary) {
		getBinaryCollection().add(jvmJarBinary);
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
