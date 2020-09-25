package dev.nokee.testing.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.internal.BaseSourceSet;
import dev.nokee.language.base.internal.UTTypeUtils;
import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.cpp.internal.CppHeaderSet;
import dev.nokee.language.nativebase.internal.UTTypeObjectCode;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskRegistryImpl;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAssembleLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAwareComponentAssembleLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAwareComponentObjectsLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantObjectsLifecycleTaskRule;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.nativebase.NativeTestSuite;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.Cast;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.language.nativeplatform.tasks.UnexportMainSymbol;
import org.gradle.language.swift.tasks.SwiftCompile;
import org.gradle.nativeplatform.test.tasks.RunTestExecutable;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class DefaultNativeTestSuiteComponent extends BaseNativeComponent<DefaultNativeTestSuiteVariant> implements NativeTestSuite {
	private final DefaultNativeComponentDependencies dependencies;
	private final ObjectFactory objects;
	private final ProviderFactory providers;
	@Getter Property<BaseComponent<?>> testedComponent;
	private final TaskRegistry taskRegistry;
	private final TaskContainer tasks;
	private final NativeTestSuiteComponentVariants componentVariants;
	private final BinaryView<Binary> binaries;

	@Inject
	public DefaultNativeTestSuiteComponent(ComponentIdentifier<DefaultNativeTestSuiteComponent> identifier, NamingScheme names, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler) {
		super(identifier, names, DefaultNativeTestSuiteVariant.class, objects, providers, tasks, layout, configurations);
		this.objects = objects;
		this.providers = providers;
		this.componentVariants = new NativeTestSuiteComponentVariants(objects, this, dependencyHandler, configurations, providers);
		this.binaries = Cast.uncheckedCast(objects.newInstance(VariantAwareBinaryView.class, new DefaultMappingView<>(getVariantCollection().getAsView(DefaultNativeTestSuiteVariant.class), Variant::getBinaries)));
		this.tasks = tasks;

		val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, identifier, new FrameworkAwareDependencyBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurations), dependencyHandler)));
		this.dependencies = objects.newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);
		this.testedComponent = Cast.uncheckedCast(objects.property(BaseComponent.class));
		this.getDimensions().convention(ImmutableList.of(DefaultBinaryLinkage.DIMENSION_TYPE, DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
		this.getBaseName().convention(names.getBaseName().getAsString());

		this.getBuildVariants().convention(providers.provider(this::createBuildVariants));
		this.getBuildVariants().finalizeValueOnRead();
		this.getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		this.getDimensions().disallowChanges(); // Let's disallow changing them for now.

		this.taskRegistry = new TaskRegistryImpl(tasks);
	}

	@Override
	public String getName() {
		return getNames().getComponentName();
	}

	protected Iterable<BuildVariantInternal> createBuildVariants() {
		if (getTestedComponent().isPresent()) {
			val buildVariantBuilder = new LinkedHashSet<BuildVariantInternal>();
			for (val buildVariant : getTestedComponent().get().getBuildVariants().get()) {
				val dimensionValues = buildVariant.getDimensions().stream().map(it -> {
					if (it instanceof DefaultBinaryLinkage) {
						return DefaultBinaryLinkage.EXECUTABLE;
					}
					return it;
				}).collect(Collectors.toList());

				buildVariantBuilder.add(DefaultBuildVariant.of(dimensionValues));
			}
			return ImmutableList.copyOf(buildVariantBuilder);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public DefaultNativeComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return componentVariants.getBuildVariants();
	}

	@Override
	public Provider<DefaultNativeTestSuiteVariant> getDevelopmentVariant() {
		return componentVariants.getDevelopmentVariant();
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return binaries;
	}

	@Override
	public VariantCollection<DefaultNativeTestSuiteVariant> getVariantCollection() {
		return componentVariants.getVariantCollection();
	}

	@Override
	public TestSuiteComponent testedComponent(Object component) {
		if (component instanceof BaseNativeExtension) {
			getTestedComponent().set(((BaseNativeExtension) component).getComponent());
		} else if (component instanceof BaseComponent) {
			getTestedComponent().set((BaseComponent) component);
		} else {
			throw new UnsupportedOperationException();
		}
		return this;
	}

	public void finalizeExtension(Project project) {
		getVariantCollection().whenElementKnown(this::createBinaries);
		getVariantCollection().whenElementKnown(new CreateVariantObjectsLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		getVariantCollection().whenElementKnown(new CreateVariantAssembleLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry).execute(this);

		componentVariants.calculateVariants();

		getVariantCollection().disallowChanges();

		// HACK: This should really be solve using the variant whenElementKnown API
		getBuildVariants().get().forEach(buildVariant -> {
			final NamingScheme names = this.getNames().forBuildVariant(buildVariant, getBuildVariants().get());
			val variantIdentifier = VariantIdentifier.builder().withType(DefaultNativeTestSuiteVariant.class).withComponentIdentifier(getIdentifier()).withUnambiguousNameFromBuildVariants(buildVariant, getBuildVariants().get()).build();

			// TODO: The variant should have give access to the testTask
			val runTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("run"), RunTestExecutable.class, variantIdentifier), task -> {
				// TODO: Use a provider of the variant here
				task.dependsOn((Callable) () -> getVariantCollection().get().stream().filter(it -> it.getBuildVariant().equals(buildVariant)).findFirst().get().getDevelopmentBinary());
				task.setOutputDir(task.getTemporaryDir());
				task.commandLine(new Object() {
					@Override
					public String toString() {
						val binary = (ExecutableBinaryInternal) getVariantCollection().get().stream().filter(it -> it.getBuildVariant().equals(buildVariant)).findFirst().get().getDevelopmentBinary().get();
						return binary.getLinkTask().flatMap(LinkExecutable::getLinkedFile).get().getAsFile().getAbsolutePath();
					}
				});
			});
			// TODO: The following is a gap is how we declare task, it should be possible to register a lifecycle task for a entity
			val testTask = taskRegistry.register(names.getBaseName().withCamelDimensions(), task -> {
				task.dependsOn(runTask);
			});
		});

		// Ensure the task is registered before configuring
		taskRegistry.registerIfAbsent("check").configure(task -> {
			// TODO: To eliminate access to the TaskContainer, we should have a getter on the variant for the relevant task in question
			task.dependsOn(getDevelopmentVariant().flatMap(it -> tasks.named(it.getNames().getBaseName().withCamelDimensions())));
		});


		getTestedComponent().disallowChanges();
		if (getTestedComponent().isPresent()) {
			val component = getTestedComponent().get();

			// TODO: Map name to something close to what is expected
			getBaseName().convention(component.getBaseName().map(it -> {
				if (component.getSourceCollection().withType(SwiftSourceSet.class).isEmpty()) {
					return it + "-" + getName();
				}
				return it + StringUtils.capitalize(getName());
			}));

			component.getSourceCollection().withType(BaseSourceSet.class).configureEach(sourceSet -> {
				if (getSourceCollection().withType(sourceSet.getClass()).isEmpty()) {
					// HACK: SourceSet in this world are quite messed up, the refactor around the source management that will be coming soon don't have this problem.
					if (sourceSet instanceof CHeaderSet || sourceSet instanceof CppHeaderSet) {
						// NOTE: Ensure we are using the "headers" name as the tested component may also contains "public"
						getSourceCollection().add(objects.newInstance(sourceSet.getClass(), "headers").srcDir(getNames().getSourceSetPath("headers")));
					} else {
						getSourceCollection().add(objects.newInstance(sourceSet.getClass(), sourceSet.getName()).from(getNames().getSourceSetPath(sourceSet.getName())));
					}
				}
			});
			if (component instanceof BaseNativeComponent) {
				val testedComponentDependencies = ((BaseNativeComponent<?>) component).getDependencies();
				getDependencies().getImplementation().getAsConfiguration().extendsFrom(testedComponentDependencies.getImplementation().getAsConfiguration());
				getDependencies().getLinkOnly().getAsConfiguration().extendsFrom(testedComponentDependencies.getLinkOnly().getAsConfiguration());
				getDependencies().getRuntimeOnly().getAsConfiguration().extendsFrom(testedComponentDependencies.getRuntimeOnly().getAsConfiguration());
			}
			getVariants().configureEach(variant -> {
				variant.getBinaries().configureEach(ExecutableBinaryInternal.class, binary -> {
					Provider<List<? extends FileTree>> componentObjects = component.getVariantCollection().filter(it -> ((BuildVariantInternal)it.getBuildVariant()).withoutDimension(DefaultBinaryLinkage.DIMENSION_TYPE).equals(variant.getBuildVariant().withoutDimension(DefaultBinaryLinkage.DIMENSION_TYPE))).map(it -> {
						ImmutableList.Builder<FileTree> result = ImmutableList.builder();
						it.stream().flatMap(v -> v.getBinaries().withType(NativeBinary.class).get().stream()).forEach(testedBinary -> {
							result.addAll(testedBinary.getCompileTasks().withType(NativeSourceCompileTask.class).getElements().map(t -> {
								return t.stream().map(a -> {
									return ((AbstractNativeSourceCompileTask) a).getObjectFileDir().getAsFileTree().matching(UTTypeUtils.onlyIf(UTTypeObjectCode.INSTANCE));
								}).collect(Collectors.toList());
							}).get());

							result.addAll(testedBinary.getCompileTasks().withType(SwiftCompileTask.class).getElements().map(t -> {
								return t.stream().map(a -> {
									return ((SwiftCompileTask) a).getObjectFileDir().getAsFileTree().matching(UTTypeUtils.onlyIf(UTTypeObjectCode.INSTANCE));
								}).collect(Collectors.toList());
							}).get());
						});
						return result.build();
					});
//					Provider<List<? extends FileTree>> componentObjects = component.getBinaries().withType(NativeBinary.class).flatMap(it -> {
//						ImmutableList.Builder<FileTree> result = ImmutableList.builder();
//						result.addAll(it.getCompileTasks().withType(NativeSourceCompileTask.class).getElements().map(t -> {
//							return t.stream().map(a -> {
//								return ((AbstractNativeSourceCompileTask) a).getObjectFileDir().getAsFileTree().matching(UTTypeUtils.onlyIf(UTTypeObjectCode.INSTANCE));
//							}).collect(Collectors.toList());
//						}).get());
//
//						result.addAll(it.getCompileTasks().withType(SwiftCompileTask.class).getElements().map(t -> {
//							return t.stream().map(a -> {
//								return ((SwiftCompileTask) a).getObjectFileDir().getAsFileTree().matching(UTTypeUtils.onlyIf(UTTypeObjectCode.INSTANCE));
//							}).collect(Collectors.toList());
//						}).get());
//
//						return result.build();
//					});

					ConfigurableFileCollection objects = this.objects.fileCollection();
					objects.from(componentObjects);
					if (component instanceof DefaultNativeApplicationComponent) {
						val relocateTask = tasks.register(variant.getNames().getTaskName("relocateMainSymbolFor"), UnexportMainSymbol.class, task -> {
							task.getObjects().from(componentObjects);
							task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir(binary.getIdentifier().getOutputDirectoryBase("objs/for-test")));
						});
						objects.setFrom(relocateTask.map(UnexportMainSymbol::getRelocatedObjects));
					}
					binary.getLinkTask().configure(task -> {
						val taskInternal = (LinkExecutableTask) task;
						taskInternal.source(objects);
					});
				});
			});

			getBinaries().configureEach(ExecutableBinary.class, binary -> {
				binary.getCompileTasks().configureEach(SwiftCompileTask.class, task -> {
					task.getModules().from(component.getDevelopmentVariant().map(it -> it.getBinaries().withType(NativeBinary.class).getElements().get().stream().flatMap(b -> b.getCompileTasks().withType(SwiftCompileTask.class).get().stream()).map(SwiftCompile::getModuleFile).collect(Collectors.toList())));
				});
				binary.getCompileTasks().configureEach(NativeSourceCompileTask.class, task -> {
					((AbstractNativeSourceCompileTask)task).getIncludes().from(providers.provider(() -> component.getSourceCollection().withType(CppHeaderSet.class).stream().map(CppHeaderSet::getHeaderDirectory).collect(Collectors.toList())));
					((AbstractNativeSourceCompileTask)task).getIncludes().from(providers.provider(() -> component.getSourceCollection().withType(CHeaderSet.class).stream().map(CHeaderSet::getHeaderDirectory).collect(Collectors.toList())));
				});
			});
		}
	}
}
