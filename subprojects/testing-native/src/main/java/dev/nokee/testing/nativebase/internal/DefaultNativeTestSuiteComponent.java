/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.testing.nativebase.internal;

import dev.nokee.language.base.internal.SourceComponentSpec;
import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.internal.BaseNameUtils;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.DependentComponentSpec;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.NativeExecutableBinarySpec;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.testing.nativebase.NativeTestSuite;
import dev.nokee.testing.nativebase.NativeTestSuiteVariant;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.test.tasks.RunTestExecutable;

import javax.inject.Inject;
import java.util.concurrent.Callable;

public /*final*/ abstract class DefaultNativeTestSuiteComponent extends BaseNativeComponent<NativeTestSuiteVariant> implements NativeTestSuite
	, NativeTestSuiteComponentSpec
	, NativeSourcesAware
	, ExtensionAwareMixIn
	, DependentComponentSpec<NativeComponentDependencies>
	, SourceComponentSpec
	, VariantAwareComponentMixIn<NativeTestSuiteVariant>
	, HasDevelopmentVariant<NativeTestSuiteVariant>
	, BinaryAwareComponentMixIn
	, AssembleTaskMixIn
	, HasBaseName
{
	private final ObjectFactory objects;
	private final ModelObjectRegistry<Task> taskRegistry;

	@Inject
	public DefaultNativeTestSuiteComponent(ObjectFactory objects, ModelObjectRegistry<Task> taskRegistry) {
		this.taskRegistry = taskRegistry;
		this.objects = objects;

		this.getBaseName().convention(BaseNameUtils.from(getIdentifier()).getAsString());
	}

	@Override
	@NestedObject
	public abstract DefaultNativeComponentDependencies getDependencies();

	public void finalizeExtension(Project project) {
		// HACK: This should really be solve using the variant whenElementKnown API
		getBuildVariants().get().forEach(buildVariant -> {
			val variantIdentifier = VariantIdentifier.builder().withComponentIdentifier(getIdentifier()).withBuildVariant((BuildVariantInternal) buildVariant).build();

			// TODO: The variant should have give access to the testTask
			//   It should be on executable-based variant
			val runTask = taskRegistry.register(getIdentifier().child(TaskName.of("run")), RunTestExecutable.class).configure(task -> {
				// TODO: Use a provider of the variant here
				task.dependsOn((Callable) () -> getVariants().filter(it -> it.getBuildVariant().equals(buildVariant)).flatMap(it -> it.get(0).getDevelopmentBinary()));
				task.setOutputDir(task.getTemporaryDir());
				task.commandLine(new Object() {
					@Override
					public String toString() {
						val binary = (NativeExecutableBinarySpec) getVariants().get().stream().filter(it -> it.getBuildVariant().equals(buildVariant)).findFirst().get().getDevelopmentBinary().get();
						return binary.getLinkTask().flatMap(LinkExecutable::getLinkedFile).get().getAsFile().getAbsolutePath();
					}
				});
			}).asProvider();

			// TODO: Attach runTask to test suite lifecycle task if it make sense.
		});


		getTestedComponent().disallowChanges();
//		if (getTestedComponent().isPresent()) {
//			val component = (BaseComponent<?>) getTestedComponent().get();
//
//			// TODO: Map name to something close to what is expected
//			getBaseName().convention(component.getBaseName().map(it -> {
//				// if the tested component has a SwiftSourceSet
//				if (model(project, objects()).get(SwiftSourceSet.class, t -> ModelObjectIdentifiers.descendantOf(t.getIdentifier(), component.getIdentifier())).get().isEmpty()) {
//					return it + "-" + getIdentifier().getName();
//				}
//				return it + StringUtils.capitalize(getIdentifier().getName().toString());
//			}));
//
//			// TODO: We won't need this once testSuites container will be maintained on component themselves
//			if (component.getExtensions().findByType(SupportCSourceSetTag.class) != null) {
//				getExtensions().create("$cSupport", SupportCSourceSetTag.class);
//			} else if (component.getExtensions().findByType(SupportCppSourceSetTag.class) != null) {
//				getExtensions().create("$cppSupport", SupportCppSourceSetTag.class);
//			} else if (component.getExtensions().findByType(SupportObjectiveCSourceSetTag.class) != null) {
//				getExtensions().create("$objectiveCSupport", SupportObjectiveCSourceSetTag.class);
//			} else if (component.getExtensions().findByType(SupportObjectiveCppSourceSetTag.class) != null) {
//				getExtensions().create("$objectiveCppSupport", SupportObjectiveCppSourceSetTag.class);
//			} else if (component.getExtensions().findByType(SupportSwiftSourceSetTag.class) != null) {
//				getExtensions().create("$swiftSupport", SupportSwiftSourceSetTag.class);
//			}
//			if (component instanceof BaseNativeComponent) {
//				val testedComponentDependencies = ((BaseNativeComponent<?>) component).getDependencies();
//				getDependencies().getImplementation().getAsConfiguration().extendsFrom(testedComponentDependencies.getImplementation().getAsConfiguration());
//				getDependencies().getLinkOnly().getAsConfiguration().extendsFrom(testedComponentDependencies.getLinkOnly().getAsConfiguration());
//				getDependencies().getRuntimeOnly().getAsConfiguration().extendsFrom(testedComponentDependencies.getRuntimeOnly().getAsConfiguration());
//			}
//			getVariants().configureEach(variant -> {
//				variant.getBinaries().configureEach(NativeExecutableBinarySpec.class, binary -> {
//					// TODO: Replace with object dependencies
//					Provider<List<? extends FileTree>> componentObjects = component.getVariants().filter(it -> ((BuildVariantInternal)it.getBuildVariant()).withoutDimension(BINARY_LINKAGE_COORDINATE_AXIS).equals(((VariantInternal) variant).getBuildVariant().withoutDimension(BINARY_LINKAGE_COORDINATE_AXIS))).map(it -> {
//						ImmutableList.Builder<FileTree> result = ImmutableList.builder();
//						it.stream().flatMap(v -> v.getBinaries().withType(NativeBinary.class).get().stream()).forEach(testedBinary -> {
//							result.addAll(testedBinary.getCompileTasks().withType(NativeSourceCompileTask.class).getElements().map(t -> {
//								return t.stream().map(a -> {
//									return ((AbstractNativeSourceCompileTask) a).getObjectFileDir().getAsFileTree().matching(pattern -> pattern.include("**/*.o", "**/*.obj"));
//								}).collect(toList());
//							}).get());
//
//							result.addAll(testedBinary.getCompileTasks().withType(SwiftCompileTask.class).getElements().map(t -> {
//								return t.stream().map(a -> {
//									return a.getObjectFileDir().getAsFileTree().matching(pattern -> pattern.include("**/*.o", "**/*.obj"));
//								}).collect(toList());
//							}).get());
//						});
//						return result.build();
//					});
////					Provider<List<? extends FileTree>> componentObjects = component.getBinaries().withType(NativeBinary.class).flatMap(it -> {
////						ImmutableList.Builder<FileTree> result = ImmutableList.builder();
////						result.addAll(it.getCompileTasks().withType(NativeSourceCompileTask.class).getElements().map(t -> {
////							return t.stream().map(a -> {
////								return ((AbstractNativeSourceCompileTask) a).getObjectFileDir().getAsFileTree().matching(UTTypeUtils.onlyIf(UTTypeObjectCode.INSTANCE));
////							}).collect(Collectors.toList());
////						}).get());
////
////						result.addAll(it.getCompileTasks().withType(SwiftCompileTask.class).getElements().map(t -> {
////							return t.stream().map(a -> {
////								return ((SwiftCompileTask) a).getObjectFileDir().getAsFileTree().matching(UTTypeUtils.onlyIf(UTTypeObjectCode.INSTANCE));
////							}).collect(Collectors.toList());
////						}).get());
////
////						return result.build();
////					});
//
//					ConfigurableFileCollection objects = this.objects.fileCollection();
//					objects.from(componentObjects);
//					if (component instanceof NativeApplicationComponent) {
//						val relocateTask = taskRegistry.register(((ModelElement) variant).getIdentifier().child(TaskName.of("relocateMainSymbolFor")), UnexportMainSymbol.class).configure(task -> {
//							task.getObjects().from(componentObjects);
//							task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir(OutputDirectoryPath.forIdentifier(binary.getIdentifier()) + "/objs/for-test"));
//						}).asProvider();
//						objects.setFrom(relocateTask.map(UnexportMainSymbol::getRelocatedObjects));
//					}
//					binary.getLinkTask().configure(task -> {
//						task.source(objects);
//					});
//				});
//			});
//
//			getBinaries().configureEach(ExecutableBinary.class, binary -> {
//				binary.getCompileTasks().configureEach(SwiftCompileTask.class, task -> {
//					task.getModules().from(component.getDevelopmentVariant().map(it -> it.getBinaries().withType(NativeBinary.class).getElements().get().stream().flatMap(b -> b.getCompileTasks().withType(SwiftCompileTask.class).get().stream()).map(SwiftCompile::getModuleFile).collect(toList())));
//				});
//				binary.getCompileTasks().configureEach(NativeSourceCompileTask.class, task -> {
//					((AbstractNativeSourceCompileTask)task).getIncludes().from((Callable<?>) () -> {
//						val builder = ImmutableList.builder();
//						Optional.ofNullable(component.getExtensions().findByName("privateHeaders")).ifPresent(builder::add);
//						Optional.ofNullable(component.getExtensions().findByName("publicHeaders")).ifPresent(builder::add);
//						return builder.build();
//					});
//				});
//			});
//		}
	}

	@Override
	protected String getTypeName() {
		return "native test suite";
	}
}
