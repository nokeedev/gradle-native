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
package dev.nokee.buildadapter.xcode.internal.rules;

import com.google.common.collect.Iterables;
import dev.nokee.buildadapter.xcode.internal.components.GradleProjectTag;
import dev.nokee.buildadapter.xcode.internal.components.GradleSettingsTag;
import dev.nokee.buildadapter.xcode.internal.components.SettingsDirectoryComponent;
import dev.nokee.buildadapter.xcode.internal.components.XCProjectComponent;
import dev.nokee.buildadapter.xcode.internal.components.XCWorkspaceComponent;
import dev.nokee.buildadapter.xcode.internal.plugins.AllXCProjectLocationsValueSource;
import dev.nokee.buildadapter.xcode.internal.plugins.AllXCProjectWithinProjectValueSource;
import dev.nokee.buildadapter.xcode.internal.plugins.AllXCWorkspaceLocationsValueSource;
import dev.nokee.buildadapter.xcode.internal.plugins.SelectXCWorkspaceLocationTransformation;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeBuildAdapterPlugin;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCWorkspace;
import dev.nokee.xcode.XCWorkspaceReference;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import java.nio.file.Path;
import java.util.Collections;

import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.utils.ProviderUtils.forParameters;
import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;
import static dev.nokee.utils.ProviderUtils.ifPresent;

public final class XcodeProjectsDiscoveryRule extends ModelActionWithInputs.ModelAction2<SettingsDirectoryComponent, ModelComponentTag<GradleSettingsTag>> {
	private static final Logger LOGGER = Logging.getLogger(XcodeProjectsDiscoveryRule.class);

	private final ModelRegistry registry;
	private final ObjectFactory objects;
	private final ProviderFactory providers;

	public XcodeProjectsDiscoveryRule(ModelRegistry registry, ObjectFactory objects, ProviderFactory providers) {
		this.registry = registry;
		this.objects = objects;
		this.providers = providers;
	}

	@Override
	protected void execute(ModelNode entity, SettingsDirectoryComponent settingsDirectory, ModelComponentTag<GradleSettingsTag> ignored) {
		val selectedWorkspace = findWorkspace(settingsDirectory.get());

		ifPresent(selectedWorkspace, workspace -> {
			entity.addComponent(new XCWorkspaceComponent(workspace.toReference()));
		});

		val projects = objects.setProperty(XCProjectReference.class);
		projects.addAll(findAllProjects(settingsDirectory.get()).map(warnsWhenNoProjects(settingsDirectory.get())));
		projects.addAll(selectedWorkspace.map(XCWorkspace::getProjectLocations).orElse(Collections.emptyList()));

		val actualProjects = forUseAtConfigurationTime(providers.of(AllXCProjectWithinProjectValueSource.class, forParameters(it -> it.getProjectLocations().addAll(projects)))).get();

		actualProjects.forEach(project -> {
			registry.instantiate(ModelRegistration.builder().withComponent(new ParentComponent(entity)).withComponent(tag(GradleProjectTag.class)).withComponent(new XCProjectComponent(project)).build());
		});
	}

	private Provider<Iterable<XCProjectReference>> findAllProjects(Path path) {
		return forUseAtConfigurationTime(providers.of(AllXCProjectLocationsValueSource.class, forParameters(it -> it.getSearchDirectory().set(path.toFile()))));
	}

	private Provider<XCWorkspace> findWorkspace(Path path) {
		return useAtConfigurationTime(findAllWorkspaces(path).map(toSingleWorkspace()));
	}

	private Provider<Iterable<XCWorkspaceReference>> findAllWorkspaces(Path path) {
		return ProviderUtils.forUseAtConfigurationTime(providers.of(AllXCWorkspaceLocationsValueSource.class, forParameters(it -> it.getSearchDirectory().set(path.toFile()))));
	}

	private static Transformer<XCWorkspaceReference, Iterable<XCWorkspaceReference>> toSingleWorkspace() {
		return new SelectXCWorkspaceLocationTransformation();
	}

	private Provider<XCWorkspace> useAtConfigurationTime(Provider<XCWorkspaceReference> workspaceReference) {
		return forUseAtConfigurationTime(providers.of(XcodeBuildAdapterPlugin.XCWorkspaceDataValueSource.class, forParameters(it -> it.getWorkspace().set(workspaceReference))));
	}

	private Transformer<Iterable<XCProjectReference>, Iterable<XCProjectReference>> warnsWhenNoProjects(Path basePath) {
		return allProjects -> {
			if (Iterables.isEmpty(allProjects)) {
				LOGGER.warn(String.format("The plugin 'dev.nokee.xcode-build-adapter' has no effect because no Xcode workspace or project were found in '%s'. See https://nokee.fyi/using-xcode-build-adapter for more details.", basePath));
			}
			return allProjects;
		};
	}
}
