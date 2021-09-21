/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.ide.fixtures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface IdeProjectTasks {
	String getIdeLifecycle();

	String getIdeClean();

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
