/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.model.internal.core;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public abstract class NodeAction {
	abstract ModelAction scope(ModelPath path);

	/**
	 * Returns node action that will scope to {@link ModelActions#doNothing()}.
	 *
	 * @return a node action that will always scope to {@link ModelActions#doNothing()}, never null.
	 */
	public static NodeAction doNothing() {
		return new NodeAction() {
			@Override
			ModelAction scope(ModelPath path) {
				return ModelActions.doNothing();
			}
		};
	}
}
