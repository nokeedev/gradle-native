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

import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.files.PBXReferenceProxy;
import dev.nokee.xcode.objects.files.PBXSourceTree;

import java.util.Objects;
import java.util.Optional;

import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

public final class CodeablePBXReferenceProxy extends AbstractCodeable implements PBXReferenceProxy {
	public enum CodingKeys implements CodingKey {
		name,
		path,
		sourceTree,
		remoteRef,
		fileType,
		;


		@Override
		public String getName() {
			return name();
		}
	}
	public CodeablePBXReferenceProxy(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public Optional<String> getName() {
		return getAsOptional(CodingKeys.name);
	}

	@Override
	public Optional<String> getPath() {
		return getAsOptional(CodingKeys.path);
	}

	@Override
	public PBXSourceTree getSourceTree() {
		return tryDecode(CodingKeys.sourceTree);
	}

	@Override
	public PBXContainerItemProxy getRemoteReference() {
		return tryDecode(CodingKeys.remoteRef);
	}

	@Override
	public String getFileType() {
		return tryDecode(CodingKeys.fileType);
	}

	@Override
	public String toString() {
		return String.format(
			"%s isa=%s name=%s path=%s sourceTree=%s",
			super.toString(),
			this.getClass().getSimpleName(),
			getName().orElse(null),
			getPath().orElse(null),
			getSourceTree());
	}

	@Override
	public int stableHash() {
		return Objects.hash(getName().orElse(null));
	}

	public static CodeablePBXReferenceProxy newInstance(KeyedObject delegate) {
		return new CodeablePBXReferenceProxy(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
