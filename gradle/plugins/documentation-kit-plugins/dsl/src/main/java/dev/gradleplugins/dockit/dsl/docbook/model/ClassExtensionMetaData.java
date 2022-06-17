/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.gradleplugins.dockit.dsl.docbook.model;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Set;

@Builder(builderClassName = "Builder")
@Value
@AllArgsConstructor
public class ClassExtensionMetaData {
	String targetClass;
	@Singular
	Set<MixinMetaData> mixinClasses;
	@Singular
	Set<ExtensionMetaData> extensionClasses;

	public ClassExtensionMetaData(String targetClass) {
		this(targetClass, ImmutableSet.of(), ImmutableSet.of());
	}
}
