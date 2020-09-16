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
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationFactories;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyFactory;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskRegistryImpl;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.nativebase.NativeTestSuite;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import lombok.var;
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
	@Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencyHandler;
	@Getter Property<BaseComponent<?>> testedComponent;
	private final TaskRegistry taskRegistry;
	private final TaskContainer tasks;

	@Inject
	public DefaultNativeTestSuiteComponent(NamingScheme names, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler) {
		super(names, DefaultNativeTestSuiteVariant.class, objects, providers, tasks, layout, configurations);
		this.dependencyHandler = dependencyHandler;
		this.tasks = tasks;

		val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, names.getComponentDisplayName(), new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(getConfigurations()), names::getConfigurationName), new DefaultDependencyFactory(getDependencyHandler()))));
		this.dependencies = objects.newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);
		this.testedComponent = Cast.uncheckedCast(getObjects().property(BaseComponent.class));
		this.getDimensions().convention(ImmutableList.of(DefaultBinaryLinkage.DIMENSION_TYPE, DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
		this.getBaseName().convention(names.getBaseName().getAsString());

		this.getBuildVariants().convention(getProviders().provider(this::createBuildVariants));
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
	protected VariantComponentDependencies<DefaultNativeComponentDependencies> newDependencies(NamingScheme names, BuildVariantInternal buildVariant) {
		var variantDependencies = getDependencies();
		if (getBuildVariants().get().size() > 1) {
			val dependencyContainer = getObjects().newInstance(DefaultComponentDependencies.class, names.getComponentDisplayName(), new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(getConfigurations()), names::getConfigurationName), new DefaultDependencyFactory(getDependencyHandler())));
			variantDependencies = getObjects().newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);
			variantDependencies.configureEach(variantBucket -> {
				getDependencies().findByName(variantBucket.getName()).ifPresent(componentBucket -> {
					variantBucket.getAsConfiguration().extendsFrom(componentBucket.getAsConfiguration());
				});
			});
		}

		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant);
		boolean hasSwift = !getSourceCollection().withType(SwiftSourceSet.class).isEmpty();
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		NativeIncomingDependencies incoming = incomingDependenciesBuilder.buildUsing(getObjects());
		NativeOutgoingDependencies outgoing = getObjects().newInstance(NativeApplicationOutgoingDependencies.class, names, buildVariant, variantDependencies);

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}

	@Override
	protected DefaultNativeTestSuiteVariant createVariant(String name, BuildVariantInternal buildVariant, VariantComponentDependencies<?> variantDependencies) {
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		val result = getObjects().newInstance(DefaultNativeTestSuiteVariant.class, name, names, buildVariant, variantDependencies);
		return result;
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

	@Override
	public void finalizeExtension(Project project) {
		super.finalizeExtension(project);

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
						getSourceCollection().add(getObjects().newInstance(sourceSet.getClass(), "headers").srcDir(getNames().getSourceSetPath("headers")));
					} else {
						getSourceCollection().add(getObjects().newInstance(sourceSet.getClass(), sourceSet.getName()).from(getNames().getSourceSetPath(sourceSet.getName())));
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
				variant.getBinaries().configureEach(ExecutableBinary.class, binary -> {
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

					ConfigurableFileCollection objects = getObjects().fileCollection();
					objects.from(componentObjects);
					if (component instanceof DefaultNativeApplicationComponent) {
						val relocateTask = tasks.register(variant.getNames().getTaskName("relocateMainSymbolFor"), UnexportMainSymbol.class, task -> {
							task.getObjects().from(componentObjects);
							task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir(variant.getNames().getOutputDirectoryBase("objs/for-test")));
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
					((AbstractNativeSourceCompileTask)task).getIncludes().from(getProviders().provider(() -> component.getSourceCollection().withType(CppHeaderSet.class).stream().map(CppHeaderSet::getHeaderDirectory).collect(Collectors.toList())));
					((AbstractNativeSourceCompileTask)task).getIncludes().from(getProviders().provider(() -> component.getSourceCollection().withType(CHeaderSet.class).stream().map(CHeaderSet::getHeaderDirectory).collect(Collectors.toList())));
				});
			});
		}
	}
}
