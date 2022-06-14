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
package dev.nokee.nvm

/**
 * Helper class to add extension methods to Groovy DSL classes at runtime.
 * The end result is comparable to the Kotlin extension methods.
 */
final class GroovyDslRuntimeExtensions {
    /**
     * Add an extension methods to an object.
     *
     * @param self the object to extend
     * @param methodName the extension method name
     * @param methodBody the extension method body
     */
    static void extendWithMethod(Object self, String methodName, Closure methodBody) {
        self.metaClass."${methodName}" = methodBody
    }
}
