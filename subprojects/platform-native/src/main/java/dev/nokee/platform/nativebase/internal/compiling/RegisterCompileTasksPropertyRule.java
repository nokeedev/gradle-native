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
package dev.nokee.platform.nativebase.internal.compiling;

import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.HasObjectFiles;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.ComponentTasksPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.IsBinary;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static dev.nokee.utils.TransformerUtils.onlyInstanceOf;
import static dev.nokee.utils.TransformerUtils.stream;

final class RegisterCompileTasksPropertyRule extends ModelActionWithInputs.ModelAction2<IdentifierComponent, IsBinary> {
	private static final ModelType<TaskView<SourceCompile>> TASK_VIEW_MODEL_TYPE = ModelType.of(new TypeOf<TaskView<SourceCompile>>() {});
	private final ModelRegistry registry;
	private final ComponentTasksPropertyRegistrationFactory tasksPropertyRegistrationFactory;

	public RegisterCompileTasksPropertyRule(ModelRegistry registry, ComponentTasksPropertyRegistrationFactory tasksPropertyRegistrationFactory) {
		this.registry = registry;
		this.tasksPropertyRegistrationFactory = tasksPropertyRegistrationFactory;
	}

	@Override
	protected void execute(ModelNode entity, IdentifierComponent identifier, IsBinary tag) {
		val compileTasks = registry.register(tasksPropertyRegistrationFactory.create(ModelPropertyIdentifier.of(identifier.get(), "compileTasks"), SourceCompile.class));
		entity.addComponent(new ObjectFiles(compileTasks.as(TASK_VIEW_MODEL_TYPE).flatMap(toObjectFiles())));
	}

	private static Transformer<Provider<Set<Path>>, TaskView<SourceCompile>> toObjectFiles() {
		return view -> view.flatMap(onlyInstanceOf(HasObjectFiles.class, asObjectFiles())).map(asFlattenPathSet());
	}

	private static Transformer<FileCollection, HasObjectFiles> asObjectFiles() {
		return HasObjectFiles::getObjectFiles;
	}

	private static Transformer<Set<Path>, Iterable<FileCollection>> asFlattenPathSet() {
		return stream(s -> s.flatMap(it -> it.getFiles().stream()).map(File::toPath).collect(toImmutableSet()));
	}
}
