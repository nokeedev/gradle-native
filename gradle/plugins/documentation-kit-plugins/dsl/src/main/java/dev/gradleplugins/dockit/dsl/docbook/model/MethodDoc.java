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
import dev.gradleplugins.dockit.dsl.source.model.MethodMetaData;
import lombok.Value;
import org.w3c.dom.Node;

@Value
public class MethodDoc implements DslElementDoc {
	ClassMetaData referringClass;
	MethodMetaData metaData;
	DocComment comment;

	public MethodDoc forClass(ClassMetaData refererMetaData) {
		if (refererMetaData == this.referringClass) {
			return this;
		}
		return new MethodDoc(refererMetaData, metaData, comment);
	}

	@Override
	public String getId() {
		return referringClass.getClassName() + ":" + metaData.getOverrideSignature();
	}

	public String getName() {
		return metaData.getName();
	}

	public boolean isDeprecated() {
		return metaData.isDeprecated() && !referringClass.isDeprecated();
	}

	public boolean isIncubating() {
		return metaData.isIncubating() || metaData.getOwnerClass().isIncubating();
	}

	public boolean isReplaced() {
		return metaData.isReplaced();
	}

	@Override
	public String getReplacement() {
		return metaData.getReplacement();
	}

	public String getDescription() {
		return comment.getDocbook().stream().filter(it -> it.getNodeName().equals("para")).map(Node::getTextContent).findFirst().orElse(null);
	}
}
