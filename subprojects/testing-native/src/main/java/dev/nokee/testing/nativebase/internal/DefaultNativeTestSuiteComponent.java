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

import com.google.common.collect.ImmutableList;
import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.language.c.internal.plugins.SupportCSourceSetTag;
import dev.nokee.language.cpp.internal.plugins.SupportCppSourceSetTag;
import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.language.nativebase.internal.PrivateHeadersComponent;
import dev.nokee.language.nativebase.internal.PublicHeadersComponent;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import dev.nokee.language.objectivec.internal.plugins.SupportObjectiveCSourceSetTag;
import dev.nokee.language.objectivecpp.internal.plugins.SupportObjectiveCppSourceSetTag;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.internal.plugins.SupportSwiftSourceSetTag;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelSpecs;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.BaseNameUtils;
import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.OutputDirectoryPath;
import dev.nokee.platform.base.internal.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetBuildTypeAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetMachineAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.NativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.nativebase.NativeTestSuite;
import dev.nokee.testing.nativebase.NativeTestSuiteVariant;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.language.nativeplatform.tasks.UnexportMainSymbol;
import org.gradle.language.swift.tasks.SwiftCompile;
import org.gradle.nativeplatform.test.tasks.RunTestExecutable;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static dev.nokee.model.internal.core.ModelNodes.descendantOf;
import static dev.nokee.model.internal.core.ModelNodes.withType;
import static dev.nokee.model.internal.type.GradlePropertyTypes.property;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;
import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
import static dev.nokee.runtime.nativebase.BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static java.util.stream.Collectors.toList;

@DomainObjectEntities.Tag({IsComponent.class})
public /*final*/ abstract class DefaultNativeTestSuiteComponent extends BaseNativeComponent<NativeTestSuiteVariant> implements NativeTestSuite
	, NativeSourcesAware
	, ExtensionAwareMixIn
	, DependencyAwareComponentMixIn<NativeComponentDependencies>
	, SourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
	, ModelBackedVariantAwareComponentMixIn<NativeTestSuiteVariant>
	, HasDevelopmentVariant<NativeTestSuiteVariant>
	, BinaryAwareComponentMixIn
	, AssembleTaskMixIn
	, ModelBackedTargetBuildTypeAwareComponentMixIn
	, ModelBackedTargetLinkageAwareComponentMixIn
	, ModelBackedTargetMachineAwareComponentMixIn
{
	private final ObjectFactory objects;
	private final ModelLookup modelLookup;
	private final ModelRegistry registry;

	@Inject
	public DefaultNativeTestSuiteComponent(ObjectFactory objects, ModelLookup modelLookup, ModelRegistry registry, ModelObjectRegistry<DependencyBucket> bucketRegistry, ModelObjectRegistry<Task> taskRegistry, Factory<BinaryView<Binary>> binariesFactory, Factory<SourceView<LanguageSourceSet>> sourcesFactory) {
		getExtensions().create("dependencies", DefaultNativeComponentDependencies.class, getIdentifier(), bucketRegistry);
		getExtensions().add("assembleTask", taskRegistry.register(getIdentifier().child(TaskName.of("assemble")), Task.class).asProvider());
		getExtensions().add("binaries", binariesFactory.create());
		getExtensions().add("sources", sourcesFactory.create());
		this.objects = objects;
		this.modelLookup = modelLookup;
		this.registry = registry;

		this.getBaseName().convention(BaseNameUtils.from(getIdentifier()).getAsString());
	}

	public Property<Component> getTestedComponent() {
		return ModelProperties.getProperty(this, "testedComponent").asProperty(property(of(Component.class)));
	}

	@Override
	public DefaultNativeComponentDependencies getDependencies() {
		return (DefaultNativeComponentDependencies) DependencyAwareComponentMixIn.super.getDependencies();
	}

	@Override
	public Provider<Set<BuildVariant>> getBuildVariants() {
		return ModelProperties.getProperty(this, "buildVariants").as(set(of(BuildVariant.class))).asProvider();
	}

	@Override
	public abstract Property<NativeTestSuiteVariant> getDevelopmentVariant();

	@Override
	@SuppressWarnings("unchecked")
	public VariantView<NativeTestSuiteVariant> getVariants() {
		return ModelProperties.getProperty(this, "variants").as(VariantView.class).get();
	}

	@Override
	public TestSuiteComponent testedComponent(Object component) {
		if (component instanceof BaseComponent) {
			getTestedComponent().set((BaseComponent) component);
		} else if (ModelNodeUtils.canBeViewedAs(ModelNodes.of(component), of(BaseComponent.class))) {
			getTestedComponent().set(ModelNodeUtils.get(ModelNodes.of(component), BaseComponent.class));
		} else {
			throw new UnsupportedOperationException();
		}
		return this;
	}

	public void finalizeExtension(Project project) {
		val checkTask = registry.register(newEntity(getIdentifier().child(TaskName.of("check")), Task.class, it -> it.ownedBy(project.getExtensions().getByType(ModelLookup.class).get(ModelPath.root())))).as(Task.class);

		// HACK: This should really be solve using the variant whenElementKnown API
		getBuildVariants().get().forEach(buildVariant -> {
			val variantIdentifier = VariantIdentifier.builder().withComponentIdentifier(getIdentifier()).withBuildVariant((BuildVariantInternal) buildVariant).build();

			// TODO: The variant should have give access to the testTask
			val runTask = registry.register(newEntity(getIdentifier().child(TaskName.of("run")), RunTestExecutable.class, it -> it.ownedBy(modelLookup.get(DomainObjectIdentifierUtils.toPath(variantIdentifier))))).as(RunTestExecutable.class).configure(task -> {
				// TODO: Use a provider of the variant here
				task.dependsOn((Callable) () -> getVariants().filter(it -> it.getBuildVariant().equals(buildVariant)).flatMap(it -> it.get(0).getDevelopmentBinary()));
				task.setOutputDir(task.getTemporaryDir());
				task.commandLine(new Object() {
					@Override
					public String toString() {
						val binary = (ExecutableBinaryInternal) getVariants().get().stream().filter(it -> it.getBuildVariant().equals(buildVariant)).findFirst().get().getDevelopmentBinary().get();
						return binary.getLinkTask().flatMap(LinkExecutable::getLinkedFile).get().getAsFile().getAbsolutePath();
					}
				});
			}).asProvider();
			// TODO: The following is a gap is how we declare task, it should be possible to register a lifecycle task for a entity
			val testTask = registry.register(newEntity(getIdentifier().child(TaskName.lifecycle()), Task.class, it -> it.ownedBy(modelLookup.get(DomainObjectIdentifierUtils.toPath(variantIdentifier))))).as(Task.class).configure(task -> {
				task.dependsOn(runTask);
			}).asProvider();
			checkTask.configure(configureDependsOn(testTask));
		});


		getTestedComponent().disallowChanges();
		if (getTestedComponent().isPresent()) {
			val component = (BaseComponent<?>) getTestedComponent().get();

			// TODO: Map name to something close to what is expected
			getBaseName().convention(component.getBaseName().map(it -> {
				// if the tested component has a SwiftSourceSet
				if (!modelLookup.anyMatch(ModelSpecs.of(descendantOf(ModelNodeUtils.getPath(component.getNode())).and(withType(of(SwiftSourceSet.class)))))) {
					return it + "-" + getIdentifier().getName();
				}
				return it + StringUtils.capitalize(getIdentifier().getName().toString());
			}));

			// TODO: We won't need this once testSuites container will be maintained on component themselves
			if (ModelNodes.of(component).hasComponent(ModelTags.typeOf(SupportCSourceSetTag.class))) {
				ModelNodes.of(this).addComponentTag(SupportCSourceSetTag.class);
			} else if (ModelNodes.of(component).hasComponent(ModelTags.typeOf(SupportCppSourceSetTag.class))) {
				ModelNodes.of(this).addComponentTag(SupportCppSourceSetTag.class);
			} else if (ModelNodes.of(component).hasComponent(ModelTags.typeOf(SupportObjectiveCSourceSetTag.class))) {
				ModelNodes.of(this).addComponentTag(SupportObjectiveCSourceSetTag.class);
			} else if (ModelNodes.of(component).hasComponent(ModelTags.typeOf(SupportObjectiveCppSourceSetTag.class))) {
				ModelNodes.of(this).addComponentTag(SupportObjectiveCppSourceSetTag.class);
			} else if (ModelNodes.of(component).hasComponent(ModelTags.typeOf(SupportSwiftSourceSetTag.class))) {
				ModelNodes.of(this).addComponentTag(SupportSwiftSourceSetTag.class);
			}
			if (component instanceof BaseNativeComponent) {
				val testedComponentDependencies = ((BaseNativeComponent<?>) component).getDependencies();
				getDependencies().getImplementation().getAsConfiguration().extendsFrom(testedComponentDependencies.getImplementation().getAsConfiguration());
				getDependencies().getLinkOnly().getAsConfiguration().extendsFrom(testedComponentDependencies.getLinkOnly().getAsConfiguration());
				getDependencies().getRuntimeOnly().getAsConfiguration().extendsFrom(testedComponentDependencies.getRuntimeOnly().getAsConfiguration());
			}
			getVariants().configureEach(variant -> {
				variant.getBinaries().configureEach(ExecutableBinaryInternal.class, binary -> {
					Provider<List<? extends FileTree>> componentObjects = component.getVariants().filter(it -> ((BuildVariantInternal)it.getBuildVariant()).withoutDimension(BINARY_LINKAGE_COORDINATE_AXIS).equals(((VariantInternal) variant).getBuildVariant().withoutDimension(BINARY_LINKAGE_COORDINATE_AXIS))).map(it -> {
						ImmutableList.Builder<FileTree> result = ImmutableList.builder();
						it.stream().flatMap(v -> v.getBinaries().withType(NativeBinary.class).get().stream()).forEach(testedBinary -> {
							result.addAll(testedBinary.getCompileTasks().withType(NativeSourceCompileTask.class).getElements().map(t -> {
								return t.stream().map(a -> {
									return ((AbstractNativeSourceCompileTask) a).getObjectFileDir().getAsFileTree().matching(pattern -> pattern.include("**/*.o", "**/*.obj"));
								}).collect(toList());
							}).get());

							result.addAll(testedBinary.getCompileTasks().withType(SwiftCompileTask.class).getElements().map(t -> {
								return t.stream().map(a -> {
									return a.getObjectFileDir().getAsFileTree().matching(pattern -> pattern.include("**/*.o", "**/*.obj"));
								}).collect(toList());
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
					if (component instanceof NativeApplicationComponent) {
						val relocateTask = registry.register(newEntity(((ModelElement) variant).getIdentifier().child(TaskName.of("relocateMainSymbolFor")), UnexportMainSymbol.class, it -> it.ownedBy(((BaseVariant) variant).getNode()))).as(UnexportMainSymbol.class).configure(task -> {
							task.getObjects().from(componentObjects);
							task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir(OutputDirectoryPath.forIdentifier(binary.getIdentifier()) + "/objs/for-test"));
						}).asProvider();
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
					task.getModules().from(component.getDevelopmentVariant().map(it -> it.getBinaries().withType(NativeBinary.class).getElements().get().stream().flatMap(b -> b.getCompileTasks().withType(SwiftCompileTask.class).get().stream()).map(SwiftCompile::getModuleFile).collect(toList())));
				});
				binary.getCompileTasks().configureEach(NativeSourceCompileTask.class, task -> {
					((AbstractNativeSourceCompileTask)task).getIncludes().from((Callable<?>) () -> {
						ModelStates.finalize(ModelNodes.of(component));

						val builder = ImmutableList.builder();
						ModelNodes.of(component).find(PrivateHeadersComponent.class).ifPresent(it -> builder.add(it.get()));
						ModelNodes.of(component).find(PublicHeadersComponent.class).ifPresent(it -> builder.add(it.get()));
						return builder.build();
					});
				});
			});
		}
	}

	@Override
	public String toString() {
		return "native test suite '" + getName() + "'";
	}
}
