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
package dev.nokee.xcode.objects;

import static dev.nokee.xcode.project.ToPBXProjConverter.isa;

public abstract class PBXObject {
	/**
	 * This method is used to generate stable GIDs and must be stable for identical contents.
	 * Returning a constant value is ok but will make the generated project order-dependent.
	 *
	 * @return stable hash
	 */
	public int stableHash() {
		return 0;
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), isa(this));
	}
}
