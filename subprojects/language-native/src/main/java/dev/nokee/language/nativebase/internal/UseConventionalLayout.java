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

package dev.nokee.language.nativebase.internal;

import com.google.common.collect.ImmutableList;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.ExtensionAware;

import java.util.List;
import java.util.stream.Collectors;

public final class UseConventionalLayout<TargetType extends Named> implements Action<TargetType> {
	private final String extensionName;
	private final List<String> sourceLayoutFormats;

	public UseConventionalLayout(String extensionName, String... sourceLayoutFormats) {
		this.extensionName = extensionName;
		this.sourceLayoutFormats = ImmutableList.copyOf(sourceLayoutFormats);
	}

	@Override
	public void execute(TargetType target) {
		if (!target.getName().isEmpty()) {
			final ConfigurableFileCollection extension = ((ConfigurableFileCollection) ((ExtensionAware) target).getExtensions().findByName(extensionName));
			if (extension != null) {
				extension.from(sourceLayoutFormats.stream().map(it -> String.format(it, target.getName())).collect(Collectors.toList()));
			}
		}
	}
}
