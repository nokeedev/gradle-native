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

import com.google.common.collect.ImmutableSet;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static dev.nokee.xcode.project.PBXTypeSafety.orEmpty;
import static dev.nokee.xcode.project.PBXTypeSafety.orEmptyList;

@EqualsAndHashCode
public final class CodeablePBXShellScriptBuildPhase implements PBXShellScriptBuildPhase, Codeable {
	public enum CodingKeys implements CodingKey {
		files,
		name,
		shellScript,
		shellPath,
		inputPaths,
		inputFileListPaths,
		outputPaths,
		outputFileListPaths,
		;

		@Override
		public String getName() {
			return name();
		}
	}

	private final KeyedObject delegate;

	public CodeablePBXShellScriptBuildPhase(KeyedObject delegate) {
		this.delegate = delegate;
	}

	@Override
	public List<PBXBuildFile> getFiles() {
		return orEmptyList(delegate.tryDecode(CodingKeys.files));
	}

	@Override
	public Optional<String> getName() {
		return orEmpty(delegate.tryDecode(CodingKeys.name));
	}

	@Override
	public List<String> getInputPaths() {
		return orEmptyList(delegate.tryDecode(CodingKeys.inputPaths));
	}

	@Override
	public List<String> getInputFileListPaths() {
		return orEmptyList(delegate.tryDecode(CodingKeys.inputFileListPaths));
	}

	@Override
	public List<String> getOutputPaths() {
		return orEmptyList(delegate.tryDecode(CodingKeys.outputPaths));
	}

	@Override
	public List<String> getOutputFileListPaths() {
		return orEmptyList(delegate.tryDecode(CodingKeys.outputFileListPaths));
	}

	@Override
	public String getShellPath() {
		return delegate.tryDecode(CodingKeys.shellPath);
	}

	@Override
	public String getShellScript() {
		return delegate.tryDecode(CodingKeys.shellScript);
	}

	@Override
	public Builder toBuilder() {
		return new Builder(delegate);
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
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
	public void encode(EncodeContext context) {
		delegate.encode(context);
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	public static CodeablePBXShellScriptBuildPhase newInstance(KeyedObject delegate) {
		return new CodeablePBXShellScriptBuildPhase(new RecodeableKeyedObject(delegate, ImmutableSet.copyOf(CodingKeys.values())));
	}
}
