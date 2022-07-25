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
package dev.nokee.platform.nativebase;

import com.google.common.collect.Iterators;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.tasks.TaskDescriptionComponent;
import dev.nokee.platform.nativebase.internal.archiving.NativeArchiveTask;
import dev.nokee.platform.nativebase.internal.linking.NativeLinkTask;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.annotation.Nonnull;

import java.util.Iterator;

import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class LinkTaskDescriptionIntegrationTest {
	ModelNode taskEntity;
	ModelNode subject;

	@BeforeEach
	void createSubject(Project project) {
		taskEntity = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder().build());
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(new NativeLinkTask(taskEntity))
			.withComponent(new IdentifierComponent(new DomainObjectIdentifier() {
				@Nonnull
				@Override
				public Iterator<Object> iterator() {
					return Iterators.forArray((HasName) () -> "pelw");
				}

				@Override
				public String toString() {
					return "artifact ':pelw'";
				}
			}))
			.build());
	}

	@Test
	void addsTaskDescriptionComponentToTaskEntity() {
		assertTrue(taskEntity.has(TaskDescriptionComponent.class));
		assertThat(taskEntity.get(TaskDescriptionComponent.class).get(),
			equalTo("Links the artifact ':pelw'."));
	}
}
