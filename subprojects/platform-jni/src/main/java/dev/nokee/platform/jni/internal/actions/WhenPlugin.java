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
package dev.nokee.platform.jni.internal.actions;

import com.google.common.collect.ImmutableList;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;

import java.util.List;

public final class WhenPlugin implements Action<Project> {
	private final List<String> pluginIds;
	private final Action<? super AppliedPlugin> delegate;

	public WhenPlugin(List<String> pluginIds, Action<? super AppliedPlugin> delegate) {
		this.pluginIds = pluginIds;
		this.delegate = delegate;
	}

	@Override
	public void execute(Project project) {
		final Action<AppliedPlugin> action = new OnceAction<>(delegate);
		for (String pluginId : pluginIds) {
			project.getPluginManager().withPlugin(pluginId, action);
		}
	}

	public static List<String> any(String... pluginIds) {
		return ImmutableList.copyOf(pluginIds);
	}
}
