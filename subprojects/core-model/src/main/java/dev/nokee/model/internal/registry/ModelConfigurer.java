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
package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelAction;

// TODO: It should probably be more an observer of the model where the ModelAction would configure.
//   As we don't have much intention to tightly control the lifecycle of the model like the software model there isn't a need for specifying the role of the action.
//   Instead, the action should specify what state it expect a node to be in before executing.
//   For example, nodes starts in Registered which listener can trigger on to add sub-links.
//   When the node transition to Discovered, listener can use that as "element known" configuration.
//   A user configuring a listener for Discovered but the node is created should still be called as the node *is discovered* but is also created.
public interface ModelConfigurer {
	void configure(ModelAction action);

	static ModelConfigurer failingConfigurer() {
		return new ModelConfigurer() {
			@Override
			public void configure(ModelAction action) {
				throw new UnsupportedOperationException("This instance always fails.");
			}
		};
	}
}
