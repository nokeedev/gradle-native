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
package dev.nokee.core.exec;

import com.google.common.collect.ImmutableList;
import dev.nokee.utils.DeferredUtils;

import java.nio.file.Path;
import java.util.Objects;

import static dev.nokee.utils.DeferredUtils.DEFAULT_FLATTENER;
import static dev.nokee.utils.DeferredUtils.flat;
import static dev.nokee.utils.DeferredUtils.isDeferred;
import static dev.nokee.utils.DeferredUtils.isFlattenableType;

enum UnpackStrategies implements UnpackStrategy {
	FLAT_UNPACK_TO_STRING {
		@Override
		public <R> R unpack(Object value) {
			@SuppressWarnings("unchecked")
			R result = (R) flat(new IgnoreJavaPathFlattener(DEFAULT_FLATTENER)).unpack()
				.whileTrue(it -> isDeferred(it) || (isFlattenableType(it) && !(it instanceof Path)))
				.execute(value).stream()
				.map(Object::toString).collect(ImmutableList.toImmutableList());
			return result;
		}
	},
	UNPACK_TO_STRING {
		@Override
		public <R> R unpack(Object value) {
			@SuppressWarnings("unchecked")
			R result = (R) Objects.toString(DeferredUtils.unpack(value), null);
			return result;
		}
	}
}
