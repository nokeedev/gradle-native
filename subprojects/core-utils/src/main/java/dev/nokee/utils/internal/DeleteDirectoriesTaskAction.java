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
package dev.nokee.utils.internal;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;

import java.io.File;
import java.nio.file.Path;

import static dev.nokee.utils.DeferredUtils.flatUnpackUntil;
import static dev.nokee.utils.DeferredUtils.unpack;


@RequiredArgsConstructor
public class DeleteDirectoriesTaskAction implements Action<Task> {
	private final Iterable<Object> directories;

	@Override
	public void execute(Task task) {
		flatUnpackUntil(directories, this::unpackToFile, File.class).forEach(this::deleteDirectory);
	}

	@SneakyThrows
	private void deleteDirectory(File directory) {
		FileUtils.deleteDirectory(directory);
	}

	private Object unpackToFile(Object obj) {
		obj = unpack(obj);
		if (obj instanceof Directory) {
			return ((Directory) obj).getAsFile();
		} else if (obj instanceof Path) {
			return ((Path)obj).toFile();
		}
		return obj;
	}
}
