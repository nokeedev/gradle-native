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
package dev.nokee.language.nativebase;

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.language.nativebase.internal.HasPrivateHeadersMixIn;
import dev.nokee.language.nativebase.internal.NativeHeaderLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.PrivateHeadersPropertyComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.platform.base.internal.DomainObjectEntities.entityOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;

@PluginRequirement.Require(type = NativeHeaderLanguageBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class PrivateHeadersSourceLayoutIntegrationTest {
	ModelNode entity;

	@BeforeEach
	void createSubject(Project project) {
		entity = ModelStates.discover(project.getExtensions().getByType(ModelRegistry.class).instantiate(ModelRegistration.builder()
			.withComponent(new FullyQualifiedNameComponent("wudoFelu"))
			.mergeFrom(entityOf(HasPrivateHeadersMixIn.class))
			.build()));
	}

	@Test
	void uses_srcSlashFullyQualifiedNameSlashHeaders_asSourceLayout() {
		assertThat(ModelProperties.valueOf(entity.get(PrivateHeadersPropertyComponent.class).get()),
			providerOf(hasItem(aFile(withAbsolutePath(endsWith("/src/wudoFelu/headers"))))));
	}
}
