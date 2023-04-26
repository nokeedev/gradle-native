/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.LoggingEngine;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettingLiteral;
import dev.nokee.xcode.XCTargetReference;
import lombok.val;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static dev.nokee.core.exec.CommandLineToolExecutionEngine.processBuilder;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.inherit;
import static dev.nokee.util.provider.ZipProviderBuilder.newBuilder;
import static dev.nokee.utils.ProviderUtils.disallowChanges;
import static dev.nokee.utils.ProviderUtils.finalizeValueOnRead;

public final class XcodebuildBuildSettingLayer implements XCBuildSettingLayer {
	private static final Logger LOGGER = Logging.getLogger(XcodebuildBuildSettingLayer.class);
	private final Provider<Map<String, XCBuildSetting>> buildSettings;
	private final Set<String> queriedBuildSettings = new HashSet<>();

	public XcodebuildBuildSettingLayer(Provider<Map<String, XCBuildSetting>> buildSettings) {
		this.buildSettings = buildSettings;
	}

	@Override
	public XCBuildSetting find(SearchContext context) {
		val result = buildSettings.get().get(context.getName());
		if (queriedBuildSettings.add(context.getName())) {
			LOGGER.info("Unknown build setting '" + context.getName() + "' requiring xcodebuild query to find value '" + result + "'");
		}
		return result;
	}

	@Override
	public Map<String, XCBuildSetting> findAll() {
		return buildSettings.get();
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@SuppressWarnings("UnstableApiUsage")
	public static class Builder {
		private final MapProperty<String, String> arguments;
		private final MapProperty<String, String> buildSettings;
		private final ObjectFactory objects;
		private final Property<Path> developerDir;

		public Builder(ObjectFactory objects) {
			this.arguments = objects.mapProperty(String.class, String.class);
			this.buildSettings = objects.mapProperty(String.class, String.class);
			this.developerDir = objects.property(Path.class);
			this.objects = objects;
		}

		public Builder targetReference(Provider<XCTargetReference> reference) {
			assert reference != null : "'reference' must not be null";
			arguments.put("-project", reference.map(it -> it.getProject().getLocation().toString()));
			arguments.put("-target", reference.map(it -> it.getName()));
			return this;
		}

		public Builder sdk(Provider<String> sdk) {
			assert sdk != null : "'sdk' must not be null";
			arguments.putAll(sdk.map(it -> ImmutableMap.of("-sdk", it)).orElse(ImmutableMap.of()));
			return this;
		}

		public Builder configuration(Provider<String> configuration) {
			assert configuration != null : "'configuration' must not be null";
			arguments.put("-configuration", configuration);
			return this;
		}

		public Builder developerDir(Provider<Path> developerDir) {
			assert developerDir != null : "'developerDir' must not be null";
			this.developerDir.set(developerDir);
			return this;
		}

		public Builder buildSettings(Provider<? extends Map<String, XCBuildSetting>> buildSettings) {
			this.buildSettings.putAll(buildSettings.map(it -> it.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()))));
			return this;
		}

		private List<String> toArgument(Map<String, String> args) {
			return args.entrySet().stream().flatMap(this::toArgument).collect(toImmutableList());
		}

		private Stream<String> toArgument(Map.Entry<String, String> entry) {
			return Stream.of(entry.getKey(), entry.getValue());
		}

		public XCBuildSettingLayer build() {
			val effectiveBuildSettings = finalizeValueOnRead(disallowChanges(objects.mapProperty(String.class, String.class)
				.value(newBuilder(objects).value(newBuilder(objects).value(arguments.map(this::toArgument)).value(buildSettings).zip((args, settings) -> ImmutableList.<String>builder().addAll(args).addAll(settings.entrySet().stream().map(it -> it.getKey() + "=" + it.getValue()).collect(Collectors.toList())).build())).value(developerDir).zip((allArguments, developerDir) -> {
					return CommandLineTool.of("xcodebuild").withArguments(it -> {
							it.args(allArguments);
							it.args("-showBuildSettings", "-json");
						}).newInvocation(it -> {
							it.withEnvironmentVariables(inherit("PATH").putOrReplace("DEVELOPER_DIR", developerDir.toString()));
						}).submitTo(LoggingEngine.wrap(processBuilder())).waitFor().assertNormalExitValue()
						.getStandardOutput().parse(output -> {
							@SuppressWarnings("unchecked")
							val parsedOutput = (List<ShowBuildSettingsEntry>) new Gson().fromJson(output, new TypeToken<List<ShowBuildSettingsEntry>>() {}.getType());
							return parsedOutput.get(0).getBuildSettings();
						});
				}))));

			return new XcodebuildBuildSettingLayer(effectiveBuildSettings.map(it -> {
				ImmutableMap.Builder<String, XCBuildSetting> builder = ImmutableMap.builder();
				it.forEach((key, value) -> {
					builder.put(key, new XCBuildSettingLiteral(key, value));
				});
				return builder.build();
			}));
		}

		private static final class ShowBuildSettingsEntry {
			private final Map<String, String> buildSettings;

			private ShowBuildSettingsEntry(Map<String, String> buildSettings) {
				this.buildSettings = buildSettings;
			}

			public Map<String, String> getBuildSettings() {
				return buildSettings;
			}
		}
	}
}
