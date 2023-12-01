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
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.internal.BaseNameUtils;
import dev.nokee.platform.base.internal.DependentComponentSpec;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.testing.nativebase.NativeTestSuite;
import dev.nokee.testing.nativebase.NativeTestSuiteVariant;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

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
//			if (component instanceof BaseNativeComponent) {
//				val testedComponentDependencies = ((BaseNativeComponent<?>) component).getDependencies();
//				getDependencies().getImplementation().getAsConfiguration().extendsFrom(testedComponentDependencies.getImplementation().getAsConfiguration());
//				getDependencies().getLinkOnly().getAsConfiguration().extendsFrom(testedComponentDependencies.getLinkOnly().getAsConfiguration());
//				getDependencies().getRuntimeOnly().getAsConfiguration().extendsFrom(testedComponentDependencies.getRuntimeOnly().getAsConfiguration());
//			}
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
