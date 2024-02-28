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
package dev.nokee.platform.nativebase.internal.linking;

import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.tasks.ObjectLink;
import org.gradle.api.Action;

import java.util.function.BiConsumer;

import static dev.nokee.platform.base.internal.util.PropertyUtils.addAll;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static java.util.Arrays.asList;

final class ConfigureLinkTaskBundleRule implements Action<Artifact> {
	@Override
	public void execute(Artifact target) {
		if (target instanceof BundleBinary) {
			((BundleBinary) target).getLinkTask().configure(configureLinkerArgs(addAll(asList("-Xlinker", "bundle"))));
		}
	}

	//region Linker args
	private static Action<ObjectLink> configureLinkerArgs(BiConsumer<? super ObjectLink, ? super PropertyUtils.CollectionProperty<String>> action) {
		return task -> action.accept(task, wrap(task.getLinkerArgs()));
	}
	//endregion
}
