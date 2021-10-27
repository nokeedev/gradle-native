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
package dev.nokee.platform.base.internal;

/**
 * Represents something that carries a name represented by any object conforming to the name object contract.
 * A name object's {@literal toString()} must returns the {@literal String} value of the name.
 * A typical name object also include a {@literal get()} methods that returns the {@literal String} value of the name.
 */
public interface HasName {
	/**
	 * Returns the name of this object.
	 *
	 * @return the name object, never null
	 */
	Object getName();
}
