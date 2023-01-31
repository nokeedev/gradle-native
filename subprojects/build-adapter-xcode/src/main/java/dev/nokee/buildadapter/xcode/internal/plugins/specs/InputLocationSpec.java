/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins.specs;

import dev.nokee.xcode.objects.files.PBXReference;
import lombok.EqualsAndHashCode;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;

import java.io.File;

@EqualsAndHashCode
public final class InputLocationSpec implements XCBuildSpec {
	private final ResolvablePathEncoder.LocationSpec delegate;

	InputLocationSpec(ResolvablePathEncoder.LocationSpec delegate) {
		this.delegate = delegate;
	}

	@InputFiles
	public FileCollection getInputFiles() {
		return delegate.get();
	}

	@Override
	public XCBuildSpec resolve(ResolveContext context) {
		return new InputLocationSpec(delegate.resolve(new ResolvablePathEncoder.LocationSpec.ResolveContext() {
			@Override
			public FileCollection resolve(PBXReference reference) {
				return context.inputs(reference); // resolve as inputs
			}
		}));
	}

	@Override
	public void visit(Visitor visitor) {
		int i = 0;
		for (File file : delegate.get()) {
			visitor.enterContext(String.valueOf(i++));
			visitor.visitValue(file);
			visitor.exitContext();
		}
	}
}
