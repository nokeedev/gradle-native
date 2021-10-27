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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;

public enum DependencyBucketType {
	Declarable, Consumable, Resolvable;

	static DependencyBucketType from(Class<? extends DependencyBucket> type) {
		if (ConsumableDependencyBucket.class.isAssignableFrom(type)) {
			return Consumable;
		} else if (ResolvableDependencyBucket.class.isAssignableFrom(type)) {
			return Resolvable;
		} else {
			return Declarable;
		}
	}
}
