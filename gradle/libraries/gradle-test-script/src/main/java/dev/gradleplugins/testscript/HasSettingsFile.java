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
package dev.gradleplugins.testscript;

import dev.gradleplugins.buildscript.blocks.SettingsBlock;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;

import java.util.function.Consumer;

public interface HasSettingsFile {
	void settingsFile(Consumer<? super SettingsBlock> action);

	GradleSettingsFile getSettingsFile();
}
