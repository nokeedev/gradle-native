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
package dev.nokee.core.exec;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Consumer;

enum CommandLineToolLogContentEmptyImpl implements CommandLineToolLogContent {
	EMPTY_LOG_CONTENT;

	@Override
	public <T> T parse(CommandLineToolOutputParser<T> parser) {
		return parser.parse(StringUtils.EMPTY);
	}

	@Override
	public String getAsString() {
		return StringUtils.EMPTY;
	}

	@Override
	public CommandLineToolLogContent withNormalizedEndOfLine() {
		return this;
	}

	@Override
	public CommandLineToolLogContent drop(int n) {
		return this;
	}

	@Override
	public CommandLineToolLogContent withAnsiControlCharactersInterpreted() {
		return this;
	}

	@Override
	public List<String> getLines() {
		return ImmutableList.of();
	}

	@Override
	public CommandLineToolLogContent visitEachLine(Consumer<LineDetails> visitor) {
		return this;
	}


}
