/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.platform.base.internal.BinaryIdentifier;
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

import static dev.nokee.platform.base.internal.util.PropertyUtils.*;
import static dev.nokee.utils.TransformerUtils.flatTransformEach;

public final class AttachLinkLibrariesToLinkTaskRule extends ModelActionWithInputs.ModelAction4<BinaryIdentifier<?>, DependentLinkLibraries, DependentFrameworks, NativeLinkTask> {
	private final BinaryIdentifier<?> identifier;

	public AttachLinkLibrariesToLinkTaskRule(BinaryIdentifier<?> identifier) {
		this.identifier = identifier;
	}

	@Override
	protected void execute(ModelNode entity, BinaryIdentifier<?> identifier, DependentLinkLibraries incomingLibraries, DependentFrameworks incomingFrameworks, NativeLinkTask linkTask) {
		if (identifier.equals(this.identifier)) {
			linkTask.configure(ObjectLink.class, configureLibraries(from(incomingLibraries)));
			linkTask.configure(ObjectLink.class, configureLinkerArgs(addAll(asFrameworkFlags(incomingFrameworks))));
		}
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
