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

@EqualsAndHashCode
public final class InputLocationSpec implements XCBuildSpec {
	private final PBXReference reference;

	InputLocationSpec(PBXReference reference) {
		this.reference = reference;
	}

	@Override
	public XCBuildPlan resolve(ResolveContext context) {
		final FileCollection locations = context.inputs(reference);
		return new XCBuildPlan() {
			@InputFiles
			public FileCollection getInputFiles() {
				return locations;
			}
		};
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitValue(reference.toString()); // TODO: Proper tostring
	}
}
