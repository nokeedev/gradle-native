package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.nativebase.internal.plugins.NativePlatformCapabilitiesMarkerPlugin;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.NokeeMap;
import dev.nokee.model.internal.NokeeMapImpl;
import dev.nokee.model.internal.Value;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.jni.JvmJarBinary;
import dev.nokee.platform.nativebase.internal.NativeLanguageRules;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.val;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.jvm.tasks.Jar;

public class JavaNativeInterfaceComponentBinaries implements ComponentBinariesInternal<VariantIdentifier<?>> {
	private final TaskRegistry taskRegistry;
	private final BaseComponent<?> component;
	private final NokeeMap<BinaryIdentifier<? extends Binary>, Binary> store;
	private final ObjectFactory objectFactory;
	private final PluginContainer pluginContainer;
	private final BinaryViewImpl<Binary> binaryView;

	public JavaNativeInterfaceComponentBinaries(TaskRegistry taskRegistry, BaseComponent<?> component, ObjectFactory objectFactory, PluginContainer pluginContainer) {
		this.taskRegistry = taskRegistry;
		this.component = component;
		this.store = new NokeeMapImpl<>(Binary.class, objectFactory);
		this.objectFactory = objectFactory;
		this.pluginContainer = pluginContainer;
		this.binaryView = new BinaryViewImpl<>(store.entrySet());
	}

	@Override
	public BinaryView<Binary> getAsView() {
		return binaryView;
	}

	@Override
	public BinaryView<Binary> getAsViewFor(VariantIdentifier<?> parentIdentifier) {
		return new BinaryViewImpl<>(store.entrySet().filter(entry -> DomainObjectIdentifierUtils.isDescendent(entry.getKey(), parentIdentifier) || JvmJarBinary.class.isAssignableFrom(entry.getValue().getType())));
	}

	@Override
	public void createBinaries(VariantIdentifier<?> identifier) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		val targetMachineInternal = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));
		val names = component.getNames().forBuildVariant(buildVariant, component.getBuildVariants().get());
		val sources = component.getSourceCollection();

		// Build all language source set
		DomainObjectSet<GeneratedSourceSet> objectSourceSets = objectFactory.domainObjectSet(GeneratedSourceSet.class);
		if (pluginContainer.hasPlugin(NativePlatformCapabilitiesMarkerPlugin.class)) {
			objectSourceSets.addAll(objectFactory.newInstance(NativeLanguageRules.class, names).apply(component.getSourceCollection()));
		}

		val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkSharedLibraryTask.class, identifier));
		val sharedLibraryBinary = objectFactory.newInstance(SharedLibraryBinaryInternal.class, names, sources, targetMachineInternal, objectSourceSets, linkTask);
		store.put(BinaryIdentifier.of(BinaryName.of("sharedLibrary"), SharedLibraryBinaryInternal.class, identifier), Value.fixed(sharedLibraryBinary));

		if (pluginContainer.hasPlugin("java") && component.getBuildVariants().get().size() == 1) {
			// binary already registered by component
		} else {
			val jarTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of("jar"), Jar.class, identifier));
			store.put(BinaryIdentifier.of(BinaryName.of("jar"), DefaultJniJarBinary.class, identifier), Value.fixed(objectFactory.newInstance(DefaultJniJarBinary.class, jarTask)));
//					if (proj.getPluginManager().hasPlugin("java")) {
//						library.getAssembleTask().configure(task -> task.dependsOn(project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class)));
//					} else {
//						// FIXME: There is a gap here, if the project doesn't have any JVM plugin applied but specify multiple target machine what is expected?
//						//   Only JNI Jar? or an empty JVM Jar and JNI Jar?... Hmmm....
//					}
		}
	}

	public void createBinaries(KnownVariant<?> knownVariant) {
		createBinaries(knownVariant.getIdentifier());
	}

	@Override
	public <T extends Binary> void put(BinaryIdentifier<T> identifier, T binary) {
		store.put(identifier, Value.fixed(binary));
	}

	public JavaNativeInterfaceComponentBinaries disallowChanges() {
		store.disallowChanges();
		return this;
	}
}
