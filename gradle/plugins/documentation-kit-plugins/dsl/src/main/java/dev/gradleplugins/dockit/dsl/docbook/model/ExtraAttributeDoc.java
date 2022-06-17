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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class ExtraAttributeDoc {
	final Element titleCell;
	final Element valueCell;

	public ExtraAttributeDoc(Element titleCell, Element valueCell) {
		this.titleCell = titleCell;
		this.valueCell = valueCell;
	}

	@Override
	public String toString() {
		return "attribute[key: " + getKey() + ", value: " + valueCell.getTextContent() + "]";
	}

	public String getKey() {
		return titleCell.getTextContent();
	}

	public List<Node> getTitle() {
		List<Node> result = new ArrayList<>();
		for (int i = 0; i < titleCell.getChildNodes().getLength(); ++i) {
			result.add(titleCell.getChildNodes().item(i));
		}
		return result;
	}

	public List<Node> getValue() {
		List<Node> result = new ArrayList<>();
		for (int i = 0; i < valueCell.getChildNodes().getLength(); ++i) {
			result.add(valueCell.getChildNodes().item(i));
		}
		return result;
	}
}
