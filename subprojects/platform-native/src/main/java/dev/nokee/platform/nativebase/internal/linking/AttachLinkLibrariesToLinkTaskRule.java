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

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.nativebase.tasks.ObjectLink;
import dev.nokee.utils.ActionUtils;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import java.nio.file.Path;
import java.util.function.BiConsumer;

import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.platform.base.internal.util.PropertyUtils.addAll;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static dev.nokee.utils.TransformerUtils.flatTransformEach;

final class AttachLinkLibrariesToLinkTaskRule extends ModelActionWithInputs.ModelAction3<DependentLinkLibraries, DependentFrameworks, NativeLinkTask> {
	private final ModelRegistry registry;

	public AttachLinkLibrariesToLinkTaskRule(ModelRegistry registry) {
		this.registry = registry;
	}

	@Override
	protected void execute(ModelNode entity, DependentLinkLibraries incomingLibraries, DependentFrameworks incomingFrameworks, NativeLinkTask linkTask) {
		registry.instantiate(configure(linkTask.get().getId(), ObjectLink.class, configureLibraries(from(incomingLibraries))));
		registry.instantiate(configure(linkTask.get().getId(), ObjectLink.class, configureLinkerArgs(addAll(asFrameworkFlags(incomingFrameworks)))));
	}

	//region Link libraries
	public static <SELF extends Task> ActionUtils.Action<SELF> configureLibraries(BiConsumer<? super SELF, ? super PropertyUtils.FileCollectionProperty> action) {
		return task -> action.accept(task, wrap(librariesProperty(task)));
	}

	private static ConfigurableFileCollection librariesProperty(Task task) {
		if (task instanceof AbstractLinkTask) {
			return ((AbstractLinkTask) task).getLibs();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion

	//region Compiler arguments
	private static Action<ObjectLink> configureLinkerArgs(BiConsumer<? super ObjectLink, ? super PropertyUtils.CollectionProperty<String>> action) {
		return task -> action.accept(task, wrap(task.getLinkerArgs()));
	}

	private static Transformer<Iterable<String>, Path> toFrameworkSearchPathFlags() {
		return it -> ImmutableList.of("-F", it.getParent().toString());
	}

	private static Transformer<Iterable<String>, Path> toFrameworkFlags() {
		return it -> ImmutableList.of("-framework", FilenameUtils.removeExtension(it.getFileName().toString()));
	}

	private static Provider<Iterable<String>> asFrameworkFlags(DependentFrameworks frameworksSearchPaths) {
		return frameworksSearchPaths.getAsProvider().map(flatTransformEach(it -> {
			return ImmutableList.<String>builder()
				.addAll(toFrameworkSearchPathFlags().transform(it))
				.addAll(toFrameworkFlags().transform(it))
				.build();
		}));
	}
	//endregion
}
