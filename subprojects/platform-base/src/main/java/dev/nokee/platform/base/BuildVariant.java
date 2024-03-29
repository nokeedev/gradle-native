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
package dev.nokee.platform.base;

/**
 * A representation of the selected values for each dimension for a build.
 *
 * @since 0.5
 */
public interface BuildVariant {
	/**
	 * Returns true if the build variant contains the specified axis value.
	 *
	 * @param axisValue a axis value to check against this build variant.
	 * @return {@code true} if the axis value is in the build type or {@code false} otherwise.
	 */
	boolean hasAxisOf(Object axisValue);
}
