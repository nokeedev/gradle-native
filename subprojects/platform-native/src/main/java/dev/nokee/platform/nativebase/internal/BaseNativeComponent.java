package dev.nokee.platform.nativebase.internal;

import com.google.common.base.Preconditions;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.LanguageSourceSetView;
import dev.nokee.language.base.internal.LanguageSourceSetRepository;
import dev.nokee.language.base.internal.LanguageSourceSetViewFactory;
import dev.nokee.language.base.internal.LanguageSourceSetViewInternal;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.model.internal.DomainObjectCreated;
import dev.nokee.model.internal.DomainObjectDiscovered;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.KnownVariant;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkBundleTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import dev.nokee.utils.ProviderUtils;
import lombok.Getter;
import lombok.val;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

public abstract class BaseNativeComponent<T extends VariantInternal> extends BaseComponent<T> implements VariantAwareComponentInternal<T> {
	private final Class<T> variantType;
	private final TaskRegistry taskRegistry;
	private final ObjectFactory objects;
	private final DomainObjectEventPublisher eventPublisher;
	private final TaskViewFactory taskViewFactory;
	private final LanguageSourceSetRepository languageSourceSetRepository;
	@Getter private final LanguageSourceSetViewInternal<LanguageSourceSet> sources;

	public BaseNativeComponent(ComponentIdentifier<?> identifier, Class<T> variantType, ObjectFactory objects, TaskContainer tasks, DomainObjectEventPublisher eventPublisher, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory, LanguageSourceSetRepository languageSourceSetRepository, LanguageSourceSetViewFactory languageSourceSetViewFactory) {
		super(identifier, objects);
		this.objects = objects;
		this.eventPublisher = eventPublisher;
		this.taskViewFactory = taskViewFactory;
		this.languageSourceSetRepository = languageSourceSetRepository;
		Preconditions.checkArgument(BaseNativeVariant.class.isAssignableFrom(variantType));
		this.variantType = variantType;
		this.taskRegistry = taskRegistry;
		this.sources = languageSourceSetViewFactory.create(identifier);
	}

	public abstract NativeComponentDependencies getDependencies();

	public VariantView<T> getVariants() {
		return getVariantCollection().getAsView(variantType);
	}

	protected void createBinaries(KnownVariant<T> knownVariant) {
		val variantIdentifier = knownVariant.getIdentifier();
		val buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
		final DefaultTargetMachine targetMachineInternal = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));

		if (buildVariant.hasAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)) {
			DefaultBinaryLinkage linkage = buildVariant.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
			if (linkage.equals(DefaultBinaryLinkage.EXECUTABLE)) {
				val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("executable"), ExecutableBinaryInternal.class, variantIdentifier);
				eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifier));
			} else if (linkage.equals(DefaultBinaryLinkage.SHARED)) {
				val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("sharedLibrary"), SharedLibraryBinaryInternal.class, variantIdentifier);
				eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifier));
			} else if (linkage.equals(DefaultBinaryLinkage.BUNDLE)) {
				val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("bundle"), BundleBinaryInternal.class, variantIdentifier);
				eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifier));
			} else if (linkage.equals(DefaultBinaryLinkage.STATIC)) {
				val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("staticLibrary"), StaticLibraryBinaryInternal.class, variantIdentifier);
				eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifier));
			}
		}

		knownVariant.configure(it -> {
			val incomingDependencies = (NativeIncomingDependencies) it.getResolvableDependencies();
			val objectSourceSets = new NativeLanguageRules(taskRegistry, objects, variantIdentifier).apply(getSources());
			BaseNativeVariant variantInternal = (BaseNativeVariant)it;
			if (buildVariant.hasAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)) {
				DefaultBinaryLinkage linkage = buildVariant.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
				if (linkage.equals(DefaultBinaryLinkage.EXECUTABLE)) {
					val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("executable"), ExecutableBinaryInternal.class, variantIdentifier);

					// Binary factory
					val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkExecutableTask.class, variantIdentifier));
					val binary = objects.newInstance(ExecutableBinaryInternal.class, binaryIdentifier, objectSourceSets, targetMachineInternal, linkTask, incomingDependencies, taskViewFactory);
					eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, binary));

					binary.getBaseName().convention(getBaseName());
				} else if (linkage.equals(DefaultBinaryLinkage.SHARED)) {
					val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("sharedLibrary"), SharedLibraryBinaryInternal.class, variantIdentifier);

					// Binary factory
					val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkSharedLibraryTask.class, variantIdentifier));
					val binary = objects.newInstance(SharedLibraryBinaryInternal.class, binaryIdentifier, targetMachineInternal, objectSourceSets, linkTask, incomingDependencies, taskViewFactory);
					eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, binary));

					binary.getBaseName().convention(getBaseName());
				} else if (linkage.equals(DefaultBinaryLinkage.BUNDLE)) {
					val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("bundle"), BundleBinaryInternal.class, variantIdentifier);

					// Binary factory
					val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkBundleTask.class, variantIdentifier));
					val binary = objects.newInstance(BundleBinaryInternal.class, binaryIdentifier, targetMachineInternal, objectSourceSets, linkTask, incomingDependencies, taskViewFactory);
					eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, binary));

					binary.getBaseName().convention(getBaseName());
				} else if (linkage.equals(DefaultBinaryLinkage.STATIC)) {
					val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("staticLibrary"), StaticLibraryBinaryInternal.class, variantIdentifier);

					// Binary factory
					val createTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("create"), CreateStaticLibraryTask.class, variantIdentifier));
					val binary = objects.newInstance(StaticLibraryBinaryInternal.class, binaryIdentifier, objectSourceSets, targetMachineInternal, createTask, incomingDependencies, taskViewFactory);
					eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, binary));

					binary.getBaseName().convention(getBaseName());
				}
			}
			it.getBinaries().configureEach(NativeBinary.class, binary -> {
				binary.getCompileTasks().configureEach(NativeSourceCompile.class, task -> {
					val taskInternal = (AbstractNativeCompileTask) task;
					taskInternal.getIncludes().from(languageSourceSetRepository.filtered(this::isHeaderSourceSet).map(ProviderUtils.map(LanguageSourceSet::getSourceDirectories)));
				});
			});
		});
	}

	private boolean isHeaderSourceSet(TypeAwareDomainObjectIdentifier<?> identifier) {
		return CHeaderSet.class.isAssignableFrom(identifier.getType()) || CppHeaderSet.class.isAssignableFrom(identifier.getType());
	}
}
