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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.buildadapter.xcode.internal.plugins.DefaultXCProjectLocator;
import dev.nokee.buildadapter.xcode.internal.plugins.DefaultXCWorkspaceLocator;
import dev.nokee.buildadapter.xcode.internal.plugins.SelectSingleXCWorkspaceTransformer;
import dev.nokee.buildadapter.xcode.internal.plugins.UnpackCrossProjectReferencesTransformer;
import dev.nokee.buildadapter.xcode.internal.plugins.WarnOnMissingXCProjectsTransformer;
import dev.nokee.buildadapter.xcode.internal.plugins.XCProjectLocator;
import dev.nokee.buildadapter.xcode.internal.plugins.XCWorkspaceLocator;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeBuildAdapterExtension;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.xcode.XCLoaders;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCWorkspaceReference;
import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.initialization.Settings;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static dev.nokee.utils.TransformerUtils.transformEach;

public final class XCProjectsDiscoveryRule implements Action<Settings> {
	private final ProviderFactory providers;
	private final ObjectFactory objects;

	public XCProjectsDiscoveryRule(ProviderFactory providers, ObjectFactory objects) {
		this.providers = providers;
		this.objects = objects;
	}

	@Override
	public void execute(Settings settings) {
		final XcodeBuildAdapterExtension extension = settings.getExtensions().getByType(XcodeBuildAdapterExtension.class);

		final Provider<List<XCWorkspaceReference>> foundWorkspaces = ProviderUtils.forUseAtConfigurationTime(providers.of(WorkspaceReferencesValueSource.class, spec -> spec.parameters(it -> it.getBaseDirectory().fileValue(settings.getSettingsDir()))));
		final Provider<XCWorkspaceReference> selectedWorkspace = foundWorkspaces.map(new SelectSingleXCWorkspaceTransformer());
		extension.getWorkspaceLocation().convention(selectedWorkspace);

		final Provider<List<XCProjectReference>> foundProjects = ProviderUtils.forUseAtConfigurationTime(providers.of(ProjectReferencesValueSource.class, spec -> spec.parameters(it -> it.getBaseDirectory().fileValue(settings.getSettingsDir()))));
		final Provider<List<XCProjectReference>> selectedProjects = extension.getWorkspaceLocation().map(XCLoaders.workspaceProjectReferencesLoader()::load).map(it -> (List<XCProjectReference>) ImmutableList.copyOf(it)).orElse(foundProjects);
		final Provider<Set<XCProjectReference>> allProjects = selectedProjects.map(ImmutableSet::copyOf).map(new UnpackCrossProjectReferencesTransformer());
		extension.getProjects().addAllLater(objects.setProperty(XcodeBuildAdapterExtension.XCProjectExtension.class).value(allProjects.orElse(providers.provider(() -> {
			new WarnOnMissingXCProjectsTransformer(settings.getSettingsDir().toPath());
			return Collections.emptySet();
		})).map(transformEach(it -> {
			final XcodeBuildAdapterExtension.XCProjectExtension result = objects.newInstance(XcodeBuildAdapterExtension.XCProjectExtension.class);
			result.getProjectLocation().value(it).disallowChanges();
			return result;
		}))));
	}

	public static abstract class WorkspaceReferencesValueSource implements ValueSource<List<XCWorkspaceReference>, WorkspaceReferencesValueSource.Parameters> {
		public interface Parameters extends ValueSourceParameters {
			DirectoryProperty getBaseDirectory();
		}

		private final XCWorkspaceLocator locator = new DefaultXCWorkspaceLocator();

		@Nullable
		@Override
		public List<XCWorkspaceReference> obtain() {
			return locator.findWorkspaces(getParameters().getBaseDirectory().get().getAsFile().toPath());
		}
	}

	public static abstract class ProjectReferencesValueSource implements ValueSource<List<XCProjectReference>, ProjectReferencesValueSource.Parameters> {
		public interface Parameters extends ValueSourceParameters {
			DirectoryProperty getBaseDirectory();
		}

		private final XCProjectLocator locator = new DefaultXCProjectLocator();

		@Nullable
		@Override
		public List<XCProjectReference> obtain() {
			return locator.findProjects(getParameters().getBaseDirectory().get().getAsFile().toPath());
		}
	}
}
