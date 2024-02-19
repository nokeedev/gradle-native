/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.testing.nativebase.internal.plugins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MoreCollectors;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BaseNameUtils;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.OutputDirectoryPath;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeComponentOf;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.platform.nativebase.internal.NativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.NativeComponentSpecEx;
import dev.nokee.platform.nativebase.internal.NativeExecutableBinarySpec;
import dev.nokee.platform.nativebase.internal.dependencies.NativeApplicationOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.rules.NativeDevelopmentBinaryConvention;
import dev.nokee.platform.nativebase.internal.rules.TargetedNativeComponentDimensionsRule;
import dev.nokee.platform.nativebase.internal.rules.ToBinariesCompileTasksTransformer;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.nativebase.internal.DefaultNativeTestSuiteComponent;
import dev.nokee.testing.nativebase.internal.DefaultNativeTestSuiteVariant;
import dev.nokee.testing.nativebase.internal.NativeExecutableBasedTestSuiteSpec;
import dev.nokee.testing.nativebase.internal.NativeTestSuiteComponentSpec;
import dev.nokee.util.provider.ZipProviderBuilder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.language.nativeplatform.tasks.UnexportMainSymbol;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.runtime.nativebase.BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS;
import static dev.nokee.testing.base.internal.plugins.TestingBasePlugin.testSuites;
import static dev.nokee.util.ProviderOfIterableTransformer.toProviderOfIterable;
import static dev.nokee.utils.CallableUtils.memoizeOnCall;
import static dev.nokee.utils.DeferUtils.asToStringObject;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TransformerUtils.flatTransformEach;
import static dev.nokee.utils.TransformerUtils.flatten;
import static dev.nokee.utils.TransformerUtils.nullSafeProvider;
import static dev.nokee.utils.TransformerUtils.nullSafeValue;
import static dev.nokee.utils.TransformerUtils.to;
import static dev.nokee.utils.TransformerUtils.transformEach;

public class NativeUnitTestingPlugin implements Plugin<Project> {
	private static Action<PatternFilterable> objectFiles() {
		return it -> it.include("**/*.o", "**/*.obj");
	}

	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(TestingBasePlugin.class);

		model(project, mapOf(Variant.class)).configureEach(NativeExecutableBasedTestSuiteSpec.class, variant -> {
			variant.getRunTask().configure(task -> {
				final Provider<RegularFile> executableFile = variant.getExecutable()
					.flatMap(NativeExecutableBinarySpec::getLinkTask)
					.flatMap(LinkExecutable::getLinkedFile);

				task.dependsOn(executableFile);
				task.setOutputDir(task.getTemporaryDir());
				task.commandLine(asToStringObject(executableFile.map(it -> it.getAsFile().getAbsolutePath())::get));
			});
		});
		model(project, mapOf(Variant.class)).configureEach(NativeExecutableBasedTestSuiteSpec.class, variant -> {
			variant.getComponentObjects().from(memoizeOnCall((Callable<?>) () -> {
				final Provider<NativeComponentOf<? extends NativeComponentSpecEx>> testedComponentProvider = model(project, mapOf(TestSuiteComponent.class)).getById(variant.getIdentifier().getParent()).asProvider()
					.flatMap(TestSuiteComponent::getTestedComponent)
					.map(component -> {
						if (component instanceof NativeComponentSpecEx && component instanceof NativeComponentOf) {
							return (NativeComponentOf<? extends NativeComponentSpecEx>) component;
						}
						return nullSafeValue();
					});

				final Provider<NativeComponentSpecEx> testedVariant = testedComponentProvider.flatMap(component -> {
						return component.getVariants().filter(it -> ((BuildVariantInternal) it.getBuildVariant()).withoutDimension(BINARY_LINKAGE_COORDINATE_AXIS).equals(((VariantInternal) variant).getBuildVariant().withoutDimension(BINARY_LINKAGE_COORDINATE_AXIS)));
					})
					.map(testedVariants -> testedVariants.stream().collect(MoreCollectors.onlyElement()))
					.map(NativeComponentSpecEx.class::cast);

				final Provider<Iterable<? extends FileTree>> objectFilesProvider = testedVariant
					.flatMap(it -> it.getBinaries().withType(NativeBinary.class).getElements())
					.map(transformEach(it -> it.getCompileTasks().getElements()))
					.flatMap(toProviderOfIterable(project.getObjects()::listProperty))
					.map(flatten())
					.map(flatTransformEach(task -> {
						if (task instanceof AbstractNativeSourceCompileTask) {
							return Collections.singletonList(((AbstractNativeSourceCompileTask) task).getObjectFileDir());
						} else if (task instanceof SwiftCompileTask) {
							return Collections.singletonList(((SwiftCompileTask) task).getObjectFileDir());
						} else {
							return Collections.emptyList();
						}
					}))
					.map(transformEach(it -> it.getAsFileTree()))
					.map(transformEach(it -> it.matching(objectFiles())));

				val testedComponent = testedComponentProvider.get();
				final ConfigurableFileCollection objects = project.getObjects().fileCollection();
				objects.from(objectFilesProvider);
				if (testedComponent instanceof NativeApplicationComponent) {
					final ModelObjectRegistry<Task> taskRegistry = model(project, registryOf(Task.class));
					final NamedDomainObjectProvider<UnexportMainSymbol> relocateTask = taskRegistry.register(variant.getIdentifier().child(TaskName.of("relocateMainSymbolFor")), UnexportMainSymbol.class)
						.configure(task -> {
							task.getObjects().from(objectFilesProvider);
							task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("objs/for-test/" + OutputDirectoryPath.forIdentifier(variant.getIdentifier())));
						}).asProvider();
					objects.setFrom(relocateTask.map(UnexportMainSymbol::getRelocatedObjects));
				}
				return objects;
			}));

			// Attach object files
			variant.getExecutable().configure(binary -> {
				binary.getLinkTask().configure(task -> {
					task.source(variant.getComponentObjects());
				});
			});
		});

		// Link test suite privateHeaders to testedComponent's private/public headers
		testSuites(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(it -> {
			final ConfigurableFileCollection privateHeaders = (ConfigurableFileCollection) it.getExtensions().findByName("privateHeaders");
			if (privateHeaders != null) {
				privateHeaders.from((Callable<?>) () -> {
					return it.getTestedComponent().map(to(ExtensionAware.class))
						.map(new Transformer<Iterable<Provider<?>>, ExtensionAware>() {
							@Override
							public Iterable<Provider<?>> transform(ExtensionAware ext) {
								final ImmutableList.Builder<Provider<?>> builder = ImmutableList.builder();
								findHeadersExtension(ext, "privateHeaders").ifPresent(builder::add);
								findHeadersExtension(ext, "publicHeaders").ifPresent(builder::add);
								return builder.build();
							}

							private /*static*/ Optional<Provider<Set<FileSystemLocation>>> findHeadersExtension(ExtensionAware self, String extensionName) {
								return Optional.ofNullable((FileCollection) self.getExtensions().findByName(extensionName)).map(FileCollection::getElements);
							}
						})
						.flatMap(toProviderOfIterable(project.getObjects()::listProperty));
				});
			}
		});

		// TODO: Test applying testing plugin before component plugin
		model(project, mapOf(TestSuiteComponent.class)).configureEach(HasBaseName.class, testSuite -> {
			final Provider<Component> componentProvider = ((TestSuiteComponent) testSuite).getTestedComponent();
			final Provider<String> baseNameProvider = componentProvider.flatMap(component -> {
				if (component instanceof HasBaseName) {
					return ((HasBaseName) component).getBaseName();
				}
				return nullSafeProvider();
			});
			testSuite.getBaseName().set(ZipProviderBuilder.newBuilder(project.getObjects())
				.value(componentProvider).value(baseNameProvider).zip((component, baseName) -> {
					// if the tested component has a SwiftSourceSet
					if (model(project, objects()).get(SwiftSourceSet.class, t -> ModelObjectIdentifiers.descendantOf(t.getIdentifier(), ((ModelElement) component).getIdentifier())).get().isEmpty()) {
						return baseName + "-" + ((ModelElement) testSuite).getIdentifier().getName();
					}
					return baseName + StringUtils.capitalize(((ModelElement) testSuite).getIdentifier().getName().toString());
			}));
		});

		// TODO: Should be on testing-base plugin or ComponentModelBasePlugin
		// Configure test suite's baseName convention
		testSuites(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(testSuite -> {
			testSuite.getBaseName().convention(BaseNameUtils.from(testSuite.getIdentifier()).getAsString());
		});

		// TODO: Convert to include NativeTestSuiteOf (may convert NativeTestSuiteComponentSpec)
		testSuites(project).withType(NativeTestSuiteComponentSpec.class)
			.configureEach(new TargetedNativeComponentDimensionsRule(project.getObjects().newInstance(ToolChainSelectorInternal.class)));
		testSuites(project).withType(NativeTestSuiteComponentSpec.class).configureEach(testSuite -> {
			testSuite.getTargetMachines().convention(testSuite.getTestedComponent().flatMap(it -> {
				if (it instanceof TargetMachineAwareComponent) {
					return ((TargetMachineAwareComponent) it).getTargetMachines();
				} else {
					return nullSafeProvider();
				}
			}).orElse(Collections.singleton(TargetMachines.host())));
			testSuite.getTargetBuildTypes().convention(testSuite.getTestedComponent().flatMap(it -> {
				if (it instanceof TargetBuildTypeAwareComponent) {
					return ((TargetBuildTypeAwareComponent) it).getTargetBuildTypes();
				} else {
					return nullSafeProvider();
				}
			}));
		});

		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultNativeTestSuiteVariant.class);

		// TODO: Move to NativeComponentBasePlugin
		model(project, mapOf(Variant.class)).configureEach(DefaultNativeTestSuiteVariant.class, variant -> {
			variant.getDevelopmentBinary().convention(variant.getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(variant.getBuildVariant().getAxisValue(BINARY_LINKAGE_COORDINATE_AXIS))));
		});
		// TODO: Move to ComponentModelBasePlugin
		model(project, mapOf(Variant.class)).configureEach(DefaultNativeTestSuiteVariant.class, variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getAssembleTask().configure(configureDependsOn((Callable<Object>) variant.getDevelopmentBinary()::get));
			}
		});
		// TODO: Move to NativeComponentBasePlugin
		model(project, mapOf(Variant.class)).configureEach(DefaultNativeTestSuiteVariant.class, variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getObjectsTask().configure(configureDependsOn(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)));
			}
		});

		model(project, factoryRegistryOf(TestSuiteComponent.class)).registerFactory(DefaultNativeTestSuiteComponent.class);

		model(project, mapOf(Variant.class)).configureEach(DefaultNativeTestSuiteVariant.class, variant -> {
			final NativeApplicationOutgoingDependencies outgoing = new NativeApplicationOutgoingDependencies(variant, variant.getRuntimeElements().getAsConfiguration(), project);
			outgoing.getExportedBinary().convention(variant.getDevelopmentBinary());
		});
		project.afterEvaluate(__ -> {
			// TODO: Use ComponentModelBasePlugin to calculate variants
			testSuites(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(component -> {
				component.finalizeExtension(project);
			});
		});
		// TODO: Only for executable-based native test suite
		testSuites(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(component -> {
			component.getTargetLinkages().convention(Collections.singletonList(TargetLinkages.EXECUTABLE));
		});

		project.afterEvaluate(proj -> {
			// TODO: We delay as late as possible to "fake" a finalize action.
			testSuites(proj).withType(DefaultNativeTestSuiteComponent.class).configureEach(it -> {
				it.finalizeExtension(proj);
			});
		});
	}
}
