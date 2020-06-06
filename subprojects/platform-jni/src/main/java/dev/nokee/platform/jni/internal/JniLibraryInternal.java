package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JniLibraryNativeDependencies;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
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

	public void registerSharedLibraryBinary(DomainObjectSet<GeneratedSourceSet> objectSourceSets, TaskProvider<LinkSharedLibraryTask> linkTask, boolean multipleVariants, NativeIncomingDependencies dependencies) {
		SharedLibraryBinaryInternal sharedLibraryBinary = getObjects().newInstance(SharedLibraryBinaryInternal.class, names, sources, targetMachine, objectSourceSets, linkTask, dependencies);
		getNativeRuntimeFiles().from(linkTask.flatMap(AbstractLinkTask::getLinkedFile));
		getNativeRuntimeFiles().from(sharedLibraryBinary.getRuntimeLibrariesDependencies());
		this.sharedLibraryBinary = sharedLibraryBinary;
		sharedLibraryBinary.getBaseName().convention(names.getBaseName().getAsString());
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
