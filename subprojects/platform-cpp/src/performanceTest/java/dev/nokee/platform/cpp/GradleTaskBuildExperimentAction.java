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
package dev.nokee.platform.cpp;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;

final class GradleTaskBuildExperimentAction implements BuildExperimentAction {
	private final List<String> tasksToRun;

	public GradleTaskBuildExperimentAction(List<String> tasksToRun) {
		this.tasksToRun = tasksToRun;
	}

	public List<String> getTasksToRun() {
		return tasksToRun;
	}

	public void writeObject(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeAttribute("type", "GradleTasks");
		writer.writeStartElement("tasks");
		for (String taskToRun : tasksToRun) {
			writer.writeStartElement("task");
			writer.writeCharacters(taskToRun);
			writer.writeEndElement();
		}
		writer.writeEndElement();
	}
}
