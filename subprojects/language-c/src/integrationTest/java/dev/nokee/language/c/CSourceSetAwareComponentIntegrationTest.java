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
package dev.nokee.language.c;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.c.internal.plugins.CSourceSetComponent;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAwareTag;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.LinkedEntity;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@PluginRequirement.Require(id = "dev.nokee.c-language")
class CSourceSetAwareComponentIntegrationTest extends AbstractPluginTest {
	private ModelNode componentEntity;
	private ModelNode sourceSetEntity;

	@BeforeEach
	void createSubject() {
		componentEntity = ModelStates.register(project.getExtensions().getByType(ModelRegistry.class).instantiate(ModelRegistration.builder().withComponent(new IdentifierComponent(ComponentIdentifier.ofMain(ProjectIdentifier.of(project)))).build()));
		assertThat("expect no source set", componentEntity.has(CSourceSetComponent.class), is(false));
		componentEntity.addComponentTag(NativeLanguageSourceSetAwareTag.class);
		sourceSetEntity = componentEntity.find(CSourceSetComponent.class).map(LinkedEntity::get).orElse(null);
	}

	@Test
	void hasSourceSetComponent() {
		assertThat(componentEntity.has(CSourceSetComponent.class), is(true));
	}

	@Test
	void usesDefaultSourceSetName() {
		assertThat(sourceSetEntity.get(ElementNameComponent.class).get(), equalTo(ElementName.of("c")));
	}

	@Test
	void hasReadableDisplayName() {
		assertThat(sourceSetEntity.get(DisplayNameComponent.class).get(), equalTo(DisplayName.of("C sources")));
	}
}
