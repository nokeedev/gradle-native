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
package dev.nokee.platform.ios.internal;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import dev.nokee.core.exec.internal.AbstractCommandLineTool;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.Input;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

public class DescriptorCommandLineTool extends AbstractCommandLineTool {
	private final CommandLineToolDescriptor toolDescriptor;

	public DescriptorCommandLineTool(File toolDescriptor) {
		try {
			this.toolDescriptor = new GsonBuilder().registerTypeAdapter(File.class, (JsonDeserializer<File>) (json, typeOfT, context) -> new File(json.getAsString())).create().fromJson(FileUtils.readFileToString(toolDescriptor, Charset.defaultCharset()), CommandLineToolDescriptor.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public String getExecutable() {
		return toolDescriptor.getPath().getAbsolutePath();
	}

	@Input
	public String getVersion() {
		return toolDescriptor.getVersion();
	}
}
