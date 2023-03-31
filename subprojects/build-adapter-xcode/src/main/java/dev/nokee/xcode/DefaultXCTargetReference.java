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
package dev.nokee.xcode;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public final class DefaultXCTargetReference implements XCTargetReference, Serializable {
	private final XCProjectReference project;
	private final String name;

	public DefaultXCTargetReference(XCProjectReference project, String name) {
		this.project = project;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public XCProjectReference getProject() {
		return project;
	}

	public XCTarget load() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "target '" + name + "' in " + project;
	}
}
