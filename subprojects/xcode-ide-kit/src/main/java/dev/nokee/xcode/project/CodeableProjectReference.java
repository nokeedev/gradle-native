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
package dev.nokee.xcode.project;

import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;

import static dev.nokee.xcode.project.RecodeableKeyedObject.of;

public final class CodeableProjectReference extends AbstractCodeable implements PBXProject.ProjectReference {
	public enum CodingKeys implements CodingKey {
		ProductGroup,
		ProjectRef,
		;

		@Override
		public String getName() {
			return name();
		}
	}
	public CodeableProjectReference(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public PBXGroup getProductGroup() {
		return getOrNull(CodingKeys.ProductGroup);
	}

	@Override
	public PBXFileReference getProjectReference() {
		return getOrNull(CodingKeys.ProjectRef);
	}

	public static CodeableProjectReference newInstance(KeyedObject delegate) {
		return new CodeableProjectReference(new RecodeableKeyedObject(delegate, of(CodingKeys.values())));
	}
}
