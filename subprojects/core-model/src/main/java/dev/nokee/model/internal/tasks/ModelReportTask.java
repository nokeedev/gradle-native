/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.model.internal.tasks;

import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.text.TreeFormatter;

public class ModelReportTask extends DefaultTask {
	@TaskAction
	private void doReport() {
		val lookup = getProject().getExtensions().getByType(ModelLookup.class);
		val rootNode = lookup.get(ModelPath.root());
		TreeFormatter formatter = new TreeFormatter();
		printNode(formatter, rootNode);
		System.out.println(formatter.toString());
	}

	private void printNode(TreeFormatter formatter, ModelNode node) {
		val path = ModelNodeUtils.getPath(node);
		if (path.getName().isEmpty()) {
			formatter.node("<root>");
		} else {
			formatter.node(path.getName());
		}
		val childNodes = node.getComponent(ModelComponentType.componentOf(DescendantNodes.class)).getDirectDescendants();
		if (!childNodes.isEmpty()) {
			formatter.startChildren();
			for (ModelNode childNode : childNodes) {
				printNode(formatter, childNode);
			}
			formatter.endChildren();
		}
	}
}
