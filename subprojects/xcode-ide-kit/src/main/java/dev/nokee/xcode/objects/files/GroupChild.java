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
package dev.nokee.xcode.objects.files;

import dev.nokee.xcode.objects.Visitable;

import java.util.Optional;

public interface GroupChild {
	// It seems the name or path can be null but not both which is a bit different from PBXFileReference.
	//   Except for the mainGroup which both the name and path is null

	Optional<String> getName();

	Optional<String> getPath();

	PBXSourceTree getSourceTree();

	@Visitable
	void accept(Visitor visitor);

	interface Visitor {
		void visit(PBXFileReference fileReference);
		void visit(PBXReferenceProxy referenceProxy);
		void visit(PBXVariantGroup variantGroup);
		void visit(XCVersionGroup versionGroup);
		void visit(PBXGroup group);
	}
}
