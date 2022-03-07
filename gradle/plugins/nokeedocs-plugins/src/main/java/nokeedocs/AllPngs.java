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
package nokeedocs;

import org.gradle.api.file.CopySpec;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;

import java.util.function.BiConsumer;

final class AllPngs<T extends ExtensionAware> implements BiConsumer<T, CopySpec> {
	@Override
	public void accept(T sample, CopySpec spec) {
		spec.from(sourceDirectory(sample).map(it -> it.getAsFileTree().matching(t -> t.include("**/*.png"))));
	}

	private static Provider<Directory> sourceDirectory(ExtensionAware target) {
		return (Provider<Directory>) target.getExtensions().getByName("sourceDirectory");
	}
}
