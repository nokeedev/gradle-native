/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.utils.internal;

import com.google.common.base.Preconditions;
import dev.nokee.utils.DeferredUtils;
import lombok.Value;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;

import java.util.function.Supplier;

import static dev.nokee.utils.DeferredUtils.unpack;

@Value
public class AssertingTaskAction implements Action<Task> {
	Supplier<Boolean> expression;
	Object errorMessage;

	@Override
	public void execute(Task task) {
		val expression = Preconditions.checkNotNull(this.expression.get());
		if (!expression) {
			throw new IllegalArgumentException(String.valueOf(unpack(errorMessage)));
		}
	}
}
