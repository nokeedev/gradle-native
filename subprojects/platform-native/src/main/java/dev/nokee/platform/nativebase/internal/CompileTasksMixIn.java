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

package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.model.internal.ModelMixIn;
import dev.nokee.model.internal.decorators.Decorate;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.NestedViewDecorator;
import dev.nokee.platform.nativebase.NativeBinary;

// TODO: Isolate compileTasks into its own interface OR convert into a NativeBinaryMixIn
public interface CompileTasksMixIn extends ModelMixIn, NativeBinary {
	@Override
	@Decorate(NestedViewDecorator.class)
	default TaskView<SourceCompile> getCompileTasks() {
		return mixedIn("compileTasks");
	}
}
