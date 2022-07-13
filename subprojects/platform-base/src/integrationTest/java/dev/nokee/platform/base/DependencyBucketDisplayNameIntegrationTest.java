/*
 * Copyright 2022 the original author or authors.
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

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ConfigurationComponent;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketTag;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketTag;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketTag;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.annotation.Nonnull;
import java.util.Iterator;

import static dev.nokee.model.internal.core.DisplayName.of;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class DependencyBucketDisplayNameIntegrationTest {
	ModelNode subject;

	@BeforeEach
	void createSubject(Project project) {
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.build());
	}

	@Test
	void doesNotOverrideDisplayNameIfPresent() {
		subject.addComponent(new ElementNameComponent("vufu"));
		subject.addComponent(new DisplayNameComponent("bopa boze"));
		assertThat(ModelStates.create(subject).get(DisplayNameComponent.class).get(), equalTo(of("bopa boze")));
	}

	@Test
	void computesDisplayNameFromElementNameToDeclarableDependencyBucketIfAbsent() {
		subject.addComponent(tag(DeclarableDependencyBucketTag.class));
		subject.addComponent(new ElementNameComponent("runtimeOnly"));
		assertThat(ModelStates.create(subject).get(DisplayNameComponent.class).get(), equalTo(of("runtime only dependencies")));
	}

	@Test
	void computesDisplayNameFromElementNameToConsumableDependencyBucketIfAbsent() {
		subject.addComponent(tag(ConsumableDependencyBucketTag.class));
		subject.addComponent(new ElementNameComponent("linkElements"));
		assertThat(ModelStates.create(subject).get(DisplayNameComponent.class).get(), equalTo(of("link elements")));
	}

	@Test
	void computesDisplayNameFromElementNameToResolvableDependencyBucketIfAbsent() {
		subject.addComponent(tag(ResolvableDependencyBucketTag.class));
		subject.addComponent(new ElementNameComponent("linkLibraries"));
		assertThat(ModelStates.create(subject).get(DisplayNameComponent.class).get(), equalTo(of("link libraries")));
	}
}
