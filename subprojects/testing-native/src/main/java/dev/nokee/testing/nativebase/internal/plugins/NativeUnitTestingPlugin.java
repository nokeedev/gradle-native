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

import com.google.common.collect.MoreCollectors;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.OutputDirectoryPath;
import dev.nokee.platform.base.internal.VariantIdentifier;
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
import dev.nokee.utils.CallableUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.language.nativeplatform.tasks.UnexportMainSymbol;

import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.runtime.nativebase.BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS;
import static dev.nokee.testing.base.internal.plugins.TestingBasePlugin.testSuites;
import static dev.nokee.util.ProviderOfIterableTransformer.toProviderOfIterable;
import static dev.nokee.utils.CallableUtils.memoizeOnCall;
import static dev.nokee.utils.DeferUtils.asToStringObject;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TransformerUtils.flatTransformEach;
import static dev.nokee.utils.TransformerUtils.flatten;
import static dev.nokee.utils.TransformerUtils.nullSafeValue;
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

			// TODO(TestingBase): Attach runTask to test suite lifecycle task if it make sense.
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
					.map(testedVariants -> {
						return testedVariants.stream().collect(MoreCollectors.onlyElement());
					})
					.map(NativeComponentSpecEx.class::cast);

				final Provider<Iterable<? extends FileSystemLocation>> objectFilesProvider = testedVariant
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
					.map(transformEach(DirectoryProperty::getAsFileTree))
					.map(transformEach(it -> it.matching(objectFiles())))
					.map(transformEach(FileCollection::getElements))
					.flatMap(toProviderOfIterable(project.getObjects()::listProperty))
					.map(flatten());

				return ZipProviderBuilder.newBuilder(project.getObjects())
					.value(testedComponentProvider).value(objectFilesProvider)
					.zip((testedComponent, objectFiles) -> {
						final ConfigurableFileCollection objects = project.getObjects().fileCollection();
						objects.from(objectFiles);
						if (testedComponent instanceof NativeApplicationComponent) {
							final ModelObjectRegistry<Task> taskRegistry = model(project, registryOf(Task.class));
							val relocateTask = taskRegistry.register(variant.getIdentifier().child(TaskName.of("relocateMainSymbolFor")), UnexportMainSymbol.class)
								.configure(task -> {
									task.getObjects().from(objectFiles);
									task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir(OutputDirectoryPath.forIdentifier(variant.getIdentifier()) + "/objs/for-test"));
								}).asProvider();
							objects.setFrom(relocateTask.map(UnexportMainSymbol::getRelocatedObjects));
						}
						return objects;
					}).flatMap(FileCollection::getElements);
			}));

			// Attach object files
			variant.getExecutable().configure(binary -> {
				binary.getLinkTask().configure(task -> {
					task.source(variant.getComponentObjects());
				});
			});
		});

		// TODO: Convert to include NativeTestSuiteOf (may convert NativeTestSuiteComponentSpec)
		testSuites(project).withType(NativeTestSuiteComponentSpec.class)
			.configureEach(new TargetedNativeComponentDimensionsRule(project.getObjects().newInstance(ToolChainSelectorInternal.class)));
		testSuites(project).withType(NativeTestSuiteComponentSpec.class).configureEach(testSuite -> {
			testSuite.getTargetMachines().convention(testSuite.getTestedComponent().flatMap(it -> {
				if (it instanceof TargetMachineAwareComponent) {
					return ((TargetMachineAwareComponent) it).getTargetMachines();
				} else {
					return null; // safe as per-contract
				}
			}).orElse(Collections.singleton(TargetMachines.host())));
			testSuite.getTargetBuildTypes().convention(testSuite.getTestedComponent().flatMap(it -> {
				if (it instanceof TargetBuildTypeAwareComponent) {
					return ((TargetBuildTypeAwareComponent) it).getTargetBuildTypes();
				} else {
					return null; // safe as per-contract
				}
			}));
		});

		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultNativeTestSuiteVariant.class);

		// TODO: Move to NativeComponentBasePlugin
		variants(project).withType(DefaultNativeTestSuiteVariant.class).configureEach(variant -> {
			variant.getDevelopmentBinary().convention(variant.getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(variant.getBuildVariant().getAxisValue(BINARY_LINKAGE_COORDINATE_AXIS))));
		});
		// TODO: Move to ComponentModelBasePlugin
		variants(project).withType(DefaultNativeTestSuiteVariant.class).configureEach(variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getAssembleTask().configure(configureDependsOn((Callable<Object>) variant.getDevelopmentBinary()::get));
			}
		});
		// TODO: Move to NativeComponentBasePlugin
		variants(project).withType(DefaultNativeTestSuiteVariant.class).configureEach(variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getObjectsTask().configure(configureDependsOn(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)));
			}
		});

		model(project, factoryRegistryOf(TestSuiteComponent.class)).registerFactory(DefaultNativeTestSuiteComponent.class);

		variants(project).withType(DefaultNativeTestSuiteVariant.class).configureEach(variant -> {
			final NativeApplicationOutgoingDependencies outgoing = new NativeApplicationOutgoingDependencies(variant.getRuntimeElements().getAsConfiguration(), project.getObjects());
			outgoing.getExportedBinary().convention(variant.getDevelopmentBinary());
		});
		project.afterEvaluate(__ -> {
			// TODO: Use ComponentModelBasePlugin to calculate variants
			testSuites(project).withType(DefaultNativeTestSuiteComponent.class).configureEach(component -> {
				for (BuildVariant it : component.getBuildVariants().get()) {
					final BuildVariantInternal buildVariant = (BuildVariantInternal) it;
					final VariantIdentifier variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).build();
					model(project, registryOf(Variant.class)).register(variantIdentifier, DefaultNativeTestSuiteVariant.class);
				}

				component.finalizeExtension(project);
//				component.getDevelopmentVariant().convention((Provider<? extends DefaultNativeTestSuiteVariant>) project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) component.getVariants().map(VariantInternal.class::cast).get())));
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
