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
package dev.nokee.model.internal.state;

public enum ModelState {
	Created, // Node instance created, can now add projections
	Initialized, // All projection added
	Registered, // Node attached to registry
	// Discovered, // Node discovered, can now register child nodes
	Realized, // Node is in use
	Finalized // Node data should not mutate any more, can now compute additional data on child nodes
	;

	public boolean isAtLeast(ModelState state) {
		return this.compareTo(state) >= 0;
	}

	public static final class IsAtLeastCreated {}
	public static final class IsAtLeastInitialized {}
	public static final class IsAtLeastRegistered {}
	public static final class IsAtLeastRealized {}
	public static final class IsAtLeastFinalized {}
}
