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
package dev.gradleplugins.buildscript;

public interface GradleBuildScript {
	/*
	blockSelector { /= blockBody =/ }

	statement/section

	// comment
	<group>
	 */


	// Syntax
	//pluginDependencySpec(String pluginId, @Nullable String version)
	/*
	id '$pluginId'[ version '$version']
	id("$pluginId")[.version("$version")]

	// if matches [a-z]+
	`$pluginId`
	 */

	// nestedPluginDependencySpec(String pluginId);
	/*
	apply plugin: '$pluginId'
	apply(plugin = "$pluginId")
	 */

	// dependencySpec(String config, String notation);
	/*
	$config $notation
	$config($notation)
	 */

	// propertyAssignment(PropertyAssignment expression);
	/*
	${expression.propertyName} = ${expression.propertyValue}

	// If boolean property
	is${expression.propertyName} = ${expression.propertyValue}
	// else
	${expression.propertyName} = ${expression.propertyValue}
	 */

	// taskSelector(TaskSelector selector)
	/*
	tasks.named(${selector.taskName})

	tasks.${selector.taskName}
	 */

	// taskByTypeSelector(String taskType)
	/*
	tasks.withType($taskType)
	tasks.withType<$taskType}>()
	 */

	// taskRegistration(String taskName, String taskType)
	/*
	tasks.register('$taskName', $taskType)
	val ${taskName} by tasks.registering(${taskType}::class);
	 */

	// string(String string)
	/*
	'$string'
	"$string"
	 */

	// mapLiteral(Map<String, ExpressionValue> map)
	/*
	[$k1: $v1, $k2, $v2]
	mapOf($k1 to $v1, $k2 to $v2)
	 */

	// createContainerElement(String comment, String container, String elementName, @Nullable String elementType, String varName, List<Statement> body)
	/*
	[def $varName = $container.register('$elementName'[, $elementType]) {
		$body
	}

	// if varName == null
	$container.register<$elementType>("$elementName")
	// if elementType == null && varName == null
	val $elementName by $container.registering
	// if elementType == null
	val $varName = $container.register("$elementName")
	// else
	val $varName = $container.register<$elementType>("$elementName")
	 */

	// referenceCreatedContainerElement(String container, String elementName, @Nullable String varName);
	/*
	$container.$elementName

	$elementName
	 */

	// containerElement(String container, String element)
	/*
	$container.$element
	$container["$element"]
	 */

	// methodInvocation(@Nullable String comment, String methodName, Object... methodArgs)
	// MethodInvocation(MethodInvocationExpression(target, methodName, list of expression values)
	/*
	// if no args
	$methodName()
	// if one args that is map
	$methodName($k1: $v1, $k2: $v2)
	// if one args that is not map or more than one args
	$methodName(${methodArgs.join(', ')})

	// if no args
	$methodName()
	// if one args that is map
	$methodName($k1 = $v1, $k2 = $v2)
	// if one args that is not map or more than one args
	$methodName(${methodArgs.join(', ')})
	 */

	// methodInvocation(@Nullable String comment, Expression target, String methodName, Object... methodArgs)
	/*
		same as above except with $target.$methodName(...)
	 */
}
