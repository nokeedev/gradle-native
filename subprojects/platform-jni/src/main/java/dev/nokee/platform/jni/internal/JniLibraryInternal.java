package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.model.internal.DomainObjectCreated;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import dev.nokee.utils.ConfigureUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import javax.inject.Inject;

public class JniLibraryInternal extends BaseVariant implements JniLibrary, VariantInternal {
	private final DefaultJavaNativeInterfaceNativeComponentDependencies dependencies;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	private final DomainObjectSet<LanguageSourceSetInternal> sources;
	private final TaskProvider<Task> assembleTask;
	private final DomainObjectEventPublisher eventPublisher;
	private final TaskViewFactory taskViewFactory;
	private final DefaultTargetMachine targetMachine;
	private final GroupId groupId;
	private final TaskRegistry taskRegistry;
	private AbstractJarBinary jarBinary;
	private SharedLibraryBinaryInternal sharedLibraryBinary;
	@Getter private final Property<String> resourcePath;
	@Getter private final ConfigurableFileCollection nativeRuntimeFiles;
	@Getter private final ResolvableComponentDependencies resolvableDependencies;

	@Inject
	public JniLibraryInternal(VariantIdentifier<JniLibraryInternal> identifier, DomainObjectSet<LanguageSourceSetInternal> parentSources, GroupId groupId, VariantComponentDependencies dependencies, ObjectFactory objects, ConfigurationContainer configurations, ProviderFactory providers, TaskRegistry taskRegistry, TaskProvider<Task> assembleTask, DomainObjectEventPublisher eventPublisher, BinaryViewFactory binaryViewFactory, TaskViewFactory taskViewFactory) {
		super(identifier, objects, binaryViewFactory);
		this.dependencies = dependencies.getDependencies();
		this.configurations = configurations;
		this.providers = providers;
		this.sources = objects.domainObjectSet(LanguageSourceSetInternal.class);
		this.assembleTask = assembleTask;
		this.eventPublisher = eventPublisher;
		this.taskViewFactory = taskViewFactory;
		this.targetMachine = new DefaultTargetMachine((DefaultOperatingSystemFamily)getBuildVariant().getDimensions().get(0), (DefaultMachineArchitecture)getBuildVariant().getDimensions().get(1));
		this.groupId = groupId;
		this.resourcePath = objects.property(String.class);
		this.nativeRuntimeFiles = objects.fileCollection();
		this.resolvableDependencies = dependencies.getIncoming();
		this.taskRegistry = taskRegistry;

		parentSources.all(sources::add);

		getResourcePath().convention(getProviders().provider(() -> getResourcePath(groupId)));
	}

	private String getResourcePath(GroupId groupId) {
		return groupId.get().map(it -> it.replace('.', '/') + '/').orElse("") + getIdentifier().getAmbiguousDimensions().getAsKebabCase().orElse("");
	}

	public void setResourcePath(Object value) {
		ConfigureUtils.setPropertyValue(resourcePath, value);
	}

	public DomainObjectSet<LanguageSourceSetInternal> getSources() {
		return sources;
	}

	public void registerSharedLibraryBinary(DomainObjectSet<GeneratedSourceSet> objectSourceSets, TaskProvider<LinkSharedLibraryTask> linkTask, NativeIncomingDependencies dependencies) {
		val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("sharedLibrary"), SharedLibraryBinaryInternal.class, getIdentifier());

		val sharedLibraryBinary = getObjects().newInstance(SharedLibraryBinaryInternal.class, binaryIdentifier, sources, targetMachine, objectSourceSets, linkTask, dependencies, taskViewFactory);
		eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, sharedLibraryBinary));

		getNativeRuntimeFiles().from(linkTask.flatMap(AbstractLinkTask::getLinkedFile));
		getNativeRuntimeFiles().from(sharedLibraryBinary.getRuntimeLibrariesDependencies());
		this.sharedLibraryBinary = sharedLibraryBinary;
		sharedLibraryBinary.getBaseName().convention(BaseNameUtils.from(getIdentifier()).getAsString());
	}

	public void registerJniJarBinary() {
		TaskProvider<Jar> jarTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of("jar"), Jar.class, getIdentifier()));
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
		Class<? extends AbstractJarBinary> type = DefaultJniJarBinary.class;
		if (jniJarBinary instanceof DefaultJvmJarBinary) {
			type = DefaultJvmJarBinary.class;
		}
		val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("jniJar"), type, getIdentifier());
		eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, jniJarBinary));
	}

	public void addJvmJarBinary(DefaultJvmJarBinary jvmJarBinary) {
		val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("jvmJar"), DefaultJvmJarBinary.class, getIdentifier());
		eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, jvmJarBinary));
	}

	public DefaultTargetMachine getTargetMachine() {
		return targetMachine;
	}

	public TaskProvider<Task> getAssembleTask() {
		return assembleTask;
	}

	@Override
	public DefaultJavaNativeInterfaceNativeComponentDependencies getDependencies() {
		return dependencies;
	}
}
