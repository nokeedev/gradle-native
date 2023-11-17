/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.platform.base;

import dev.nokee.internal.Factory;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.DefaultVariantDimensions;
import dev.nokee.platform.base.internal.VariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantViewFactory;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.testers.VariantDimensionsIntegrationTester;
import org.gradle.api.Project;
import org.gradle.api.reflect.TypeOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class VariantAwareComponentDimensionsIntegrationTest extends VariantDimensionsIntegrationTester {
	private final Project project = ProjectTestUtils.rootProject();
	private VariantAwareComponent<Variant> subject;

	@BeforeEach
	void applyPlugins() {
		project.getPluginManager().apply(ComponentModelBasePlugin.class);
		subject = ModelElementSupport.newInstance(new ModelElement() {
			@Override
			public ModelObjectIdentifier getIdentifier() {
				return ProjectIdentifier.of(project).child("foo");
			}

			@Override
			public String getName() {
				return "foo";
			}
		}, () -> project.getObjects().newInstance(MyComponent.class, project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(new TypeOf<Factory<DefaultVariantDimensions>>() {
		})));
	}

	@Test
	void hasVariantDimensions() {
		assertNotNull(subject.getDimensions());
	}

	@Override
	public VariantAwareComponent<?> subject() {
		return subject;
	}

	public static abstract class MyComponent extends ModelElementSupport implements VariantAwareComponentMixIn<Variant> {
		public MyComponent(VariantViewFactory variantsFactory, Factory<DefaultVariantDimensions> dimensionsFactory) {
			getExtensions().add("variants", variantsFactory.create(Variant.class));
			getExtensions().add("dimensions", dimensionsFactory.create());
		}
	}
}
