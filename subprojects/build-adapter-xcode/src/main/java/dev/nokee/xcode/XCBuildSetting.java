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
package dev.nokee.xcode;

import javax.annotation.Nullable;

/**
 * Represent a Xcode build setting.
 */
public interface XCBuildSetting {
	/**
	 * Returns the name of this build setting.
	 *
	 * @return this build setting's name
	 */
	String getName();

	// <build_settings_name>[arch=<architecture_pattern>] = <build_setting_specification>

	// name
	//   -> starts with a letter or underscore char
	//   -> remaining chars can be letters, underscore or numbers
	//   -> Case sensitive project_name != PROJECT_NAME

	// value (resolve)
	//  evaluate the build settings specification
	//  reference other build setting using $(BUILD_SETTING)
	//  specification of build setting defined in higher layers overrides the specifications of the same build setting at lower layer
	//  using layers, can define build settings in terms of itself, in multiple layers, see $(LAYERED) where LAYERED is a build setting name
	//  normal layer:
	//    1- Command line
	//    2- Target -> named collections of build settings, aka build configurations
	//    3- Project
	//    4- Application settings -> from Xcode Preference -> Building
	//    5- Built-in defaults
	//    6- Environment

	// specification:
	// type (maybe resolve)
	// displayName (title)

	//  -> Specification apply to a target regardless of the SDK used to build it or the architectures for which the product is built
	//    -> Conditional build setting definition



	String evaluate(EvaluationContext context);

	interface EvaluationContext {
//		String getArch();
//		String getVariant();
//		String getSdk();
		@Nullable
		String get(String value);
	}

	interface Specification {

	}
}
