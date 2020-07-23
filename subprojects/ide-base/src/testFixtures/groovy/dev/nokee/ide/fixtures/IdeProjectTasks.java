package dev.nokee.ide.fixtures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface IdeProjectTasks {
	String getIdeLifecycle();

	default List<String> getAllToIde() {
		return Collections.singletonList(getIdeLifecycle());
	}

	default List<String> allToIdeProject(String... ideProjectNames) {
		List<String> result = new ArrayList<>();
		result.addAll(Arrays.stream(ideProjectNames).map(this::ideProject).collect(Collectors.toList()));
		return Collections.unmodifiableList(result);
	}

	default List<String> allToIde(String... ideProjectNames) {
		List<String> result = new ArrayList<>(getAllToIde());
		result.addAll(allToIdeProject(ideProjectNames));
		return Collections.unmodifiableList(result);
	}

	String ideProject(String name);
}
