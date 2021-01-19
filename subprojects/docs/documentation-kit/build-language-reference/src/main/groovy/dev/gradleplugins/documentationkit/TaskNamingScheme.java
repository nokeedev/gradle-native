package dev.gradleplugins.documentationkit;

import dev.nokee.platform.base.internal.tasks.TaskName;
import org.apache.commons.lang3.StringUtils;

public interface TaskNamingScheme {
	String taskName(TaskName name);

	static TaskNamingScheme identity() {
		return new TaskNamingScheme() {
			@Override
			public String taskName(TaskName name) {
				return String.valueOf(name);
			}
		};
	}

	static TaskNamingScheme forComponent(String componentName) {
		return new TaskNamingScheme() {
			@Override
			public String taskName(TaskName name) {
				return name.getVerb() + StringUtils.capitalize(componentName) + name.getObject().map(StringUtils::capitalize).orElse("");
			}
		};
	}
}
