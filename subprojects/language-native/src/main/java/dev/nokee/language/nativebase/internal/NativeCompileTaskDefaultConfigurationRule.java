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
package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import org.gradle.api.Action;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

import java.util.function.BiConsumer;

import static dev.nokee.platform.base.internal.util.PropertyUtils.set;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;

public final class NativeCompileTaskDefaultConfigurationRule extends ModelActionWithInputs.ModelAction2<LanguageSourceSetIdentifier, NativeCompileTask> {
	private final LanguageSourceSetIdentifier identifier;

	public NativeCompileTaskDefaultConfigurationRule(LanguageSourceSetIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	protected void execute(ModelNode entity, LanguageSourceSetIdentifier identifier, NativeCompileTask compileTask) {
		if (identifier.equals(this.identifier)) {
			compileTask.configure(NativeSourceCompileTask.class, configurePositionIndependentCode(set(false)));
		}
	}

	//region Position independent code
	private static Action<NativeSourceCompileTask> configurePositionIndependentCode(BiConsumer<? super NativeSourceCompileTask, ? super PropertyUtils.SetAwareProperty<Boolean>> action) {
		return task -> action.accept(task, wrap(((AbstractNativeCompileTask) task)::setPositionIndependentCode));
	}
	//endregion
}
