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
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;

import static dev.nokee.xcode.project.RecodeableKeyedObject.of;

@EqualsAndHashCode
public final class CodeableProjectReference implements PBXProject.ProjectReference, Codeable {
	public enum CodingKeys implements CodingKey {
		ProductGroup,
		ProjectRef,
		;

		@Override
		public String getName() {
			return name();
		}
	}
	private final KeyedObject delegate;

	public CodeableProjectReference(KeyedObject delegate) {
		this.delegate = delegate;
	}

	@Override
	public PBXGroup getProductGroup() {
		return delegate.tryDecode(CodingKeys.ProductGroup);
	}

	@Override
	public PBXFileReference getProjectReference() {
		return delegate.tryDecode(CodingKeys.ProjectRef);
	}

	@Override
	public String isa() {
		return delegate.isa();
	}

	@Nullable
	@Override
	public String globalId() {
		return delegate.globalId();
	}

	@Override
	public long age() {
		return delegate.age();
	}

	@Override
	public void encode(EncodeContext context) {
		delegate.encode(context);
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	public static CodeableProjectReference newInstance(KeyedObject delegate) {
		return new CodeableProjectReference(new RecodeableKeyedObject(delegate, of(CodingKeys.values())));
	}
}
