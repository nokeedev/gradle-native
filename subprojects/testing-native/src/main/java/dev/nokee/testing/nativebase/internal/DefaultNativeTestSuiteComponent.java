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
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.internal.DependentComponentSpec;
import dev.nokee.platform.base.internal.VariantComponentSpec;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.testing.nativebase.NativeTestSuite;
import dev.nokee.testing.nativebase.NativeTestSuiteVariant;
import org.gradle.api.Project;

public /*final*/ abstract class DefaultNativeTestSuiteComponent extends ModelElementSupport implements NativeTestSuite
	, NativeTestSuiteComponentSpec
	, NativeSourcesAware
	, ExtensionAwareMixIn
	, DependentComponentSpec<NativeComponentDependencies>
	, SourceComponentSpec
	, VariantComponentSpec<DefaultNativeTestSuiteVariant>
	, VariantAwareComponentMixIn<NativeTestSuiteVariant>, HasDevelopmentVariant<NativeTestSuiteVariant>
	, BinaryAwareComponentMixIn
	, AssembleTaskMixIn
	, HasBaseName
{
	@Override
	@NestedObject
	public abstract DefaultNativeComponentDependencies getDependencies();

	public void finalizeExtension(Project project) {
//		if (getTestedComponent().isPresent()) {
//			val component = (BaseComponent<?>) getTestedComponent().get();
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
//			});
//		}
	}

	@Override
	protected String getTypeName() {
		return "native test suite";
	}
}
