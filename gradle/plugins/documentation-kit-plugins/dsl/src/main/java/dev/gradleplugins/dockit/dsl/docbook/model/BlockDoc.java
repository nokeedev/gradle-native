/*
 * Copyright 2010 the original author or authors.
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

import dev.gradleplugins.dockit.dsl.docbook.DocComment;
import dev.gradleplugins.dockit.dsl.source.model.ClassMetaData;
import dev.gradleplugins.dockit.dsl.source.model.TypeMetaData;
import lombok.Value;

@Value
public class BlockDoc implements DslElementDoc {
	MethodDoc blockMethod;
	PropertyDoc blockProperty;
	TypeMetaData type;
	boolean multiValued;

	BlockDoc forClass(ClassMetaData refererMetaData) {
		return new BlockDoc(blockMethod.forClass(refererMetaData), blockProperty.forClass(refererMetaData), type, multiValued);
	}

	public String getId() {
		return blockMethod.getId();
	}

	public String getName() {
		return blockMethod.getName();
	}

	public String getDescription() {
		return blockMethod.getDescription();
	}

	public DocComment getComment() {
		return blockMethod.getComment();
	}

	public boolean isDeprecated() {
		return blockProperty.isDeprecated() || blockMethod.isDeprecated();
	}

	public boolean isIncubating() {
		return blockProperty.isIncubating() || blockMethod.isIncubating();
	}

	public boolean isReplaced() {
		return blockProperty.isReplaced();
	}

	@Override
	public String getReplacement() {
		return blockProperty.getReplacement();
	}
}
