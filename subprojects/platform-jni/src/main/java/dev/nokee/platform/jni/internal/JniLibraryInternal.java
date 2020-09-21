package dev.nokee.platform.jni.internal;

import com.google.common.collect.Iterables;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JvmJarBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import dev.nokee.utils.ProviderUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public class JniLibraryInternal extends BaseVariant implements JniLibrary, VariantInternal {
	@Getter private final NamingScheme names;
	private final DefaultJavaNativeInterfaceNativeComponentDependencies dependencies;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;
	private final DomainObjectSet<LanguageSourceSetInternal> sources;
	private final DefaultTargetMachine targetMachine;
	private final GroupId groupId;
	private final Provider<? extends SharedLibraryBinaryInternal> sharedLibraryBinary;
	private final Provider<AbstractJarBinary> jniJarBinary;
	private final Provider<AbstractJarBinary> jvmJarBinary;
	@Getter private final Property<String> resourcePath;
	@Getter private final ConfigurableFileCollection nativeRuntimeFiles;
	@Getter private final ResolvableComponentDependencies resolvableDependencies;

	@Inject
	public JniLibraryInternal(VariantIdentifier<JniLibraryInternal> identifier, NamingScheme names, DomainObjectSet<LanguageSourceSetInternal> parentSources, GroupId groupId,VariantComponentDependencies dependencies, ObjectFactory objects, ConfigurationContainer configurations, ProviderFactory providers, TaskContainer tasks, BinaryView<Binary> binaryView) {
		super(identifier, objects, binaryView);
		this.names = names;
		this.dependencies = dependencies.getDependencies();
		this.configurations = configurations;
		this.providers = providers;
		this.tasks = tasks;
		this.sources = objects.domainObjectSet(LanguageSourceSetInternal.class);
		this.targetMachine = new DefaultTargetMachine((DefaultOperatingSystemFamily)getBuildVariant().getDimensions().get(0), (DefaultMachineArchitecture)getBuildVariant().getDimensions().get(1));
		this.groupId = groupId;
		this.resourcePath = objects.property(String.class);
		this.nativeRuntimeFiles = objects.fileCollection();
		this.resolvableDependencies = dependencies.getIncoming();
		this.sharedLibraryBinary = (Provider<? extends SharedLibraryBinaryInternal>) binaryView.filter(it -> it instanceof SharedLibraryBinaryInternal).flatMap(it -> ProviderUtils.fixed(Iterables.getOnlyElement(it)));
		this.jniJarBinary = (Provider<AbstractJarBinary>) binaryView.filter(it -> it instanceof JniJarBinary).flatMap(it -> ProviderUtils.fixed(Iterables.getOnlyElement(it)));
		this.jvmJarBinary = (Provider<AbstractJarBinary>) binaryView.filter(it -> it instanceof JvmJarBinary).flatMap(it -> ProviderUtils.fixed(Iterables.getOnlyElement(it)));

		getNativeRuntimeFiles().from(sharedLibraryBinary.flatMap(it -> it.getLinkedFile()));
		getNativeRuntimeFiles().from(sharedLibraryBinary.map(it -> it.getRuntimeLibrariesDependencies()));

		parentSources.all(sources::add);

		getResourcePath().convention(getProviders().provider(() -> names.getResourcePath(groupId)));
	}

	public DomainObjectSet<LanguageSourceSetInternal> getSources() {
		return sources;
	}

	public AbstractJarBinary getJar() {
		return (jniJarBinary).orElse(jvmJarBinary).get();
	}

	public SharedLibraryBinaryInternal getSharedLibrary() {
		return sharedLibraryBinary.get();
	}

	@Override
	public void sharedLibrary(Action<? super SharedLibraryBinary> action) {
		action.execute(sharedLibraryBinary.get());
	}

	public DefaultTargetMachine getTargetMachine() {
		return targetMachine;
	}

	public TaskProvider<Task> getAssembleTask() {
		return getTasks().named(names.getTaskName("assemble"));
	}

	@Override
	public DefaultJavaNativeInterfaceNativeComponentDependencies getDependencies() {
		return dependencies;
	}
}
