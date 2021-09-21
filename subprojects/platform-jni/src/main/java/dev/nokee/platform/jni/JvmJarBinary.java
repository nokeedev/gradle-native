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
package dev.nokee.platform.jni;

/**
 * Configuration for a JVM JAR binary.
 * A JVM JAR refers to a JAR binary containing only the compiled classes.
 * It is always present and the instance is shared across all variants.
 * If the JNI library has only a single variant, the produced JAR will also contain theshared library binary.
 *
 * The binary is accessible via the library's {@link dev.nokee.platform.base.BinaryView} containing all the binary instance of a library:
 * <pre class="autoTested">
 * plugins {
 *     id 'dev.nokee.jni-library'
 *     id 'dev.nokee.cpp-language'
 *     id 'java'
 * }
 *
 * import dev.nokee.platform.jni.JvmJarBinary
 *
 * library {
 *     binaries.withType(JvmJarBinary).configureEach {
 *         // ...
 *     }
 * }
 * </pre>
 *
 * The binary is also accessible via the variant's {@link dev.nokee.platform.base.BinaryView} containing only the binaries instance for a specific variant:
 * <pre class="autoTested">
 * plugins {
 *     id 'dev.nokee.jni-library'
 *     id 'dev.nokee.cpp-language'
 *     id 'java'
 * }
 *
 * import dev.nokee.platform.jni.JvmJarBinary
 *
 * library {
 *     variants.configureEach {
 *         binaries.withType(JvmJarBinary).configureEach {
 *             // ...
 *         }
 *     }
 * }
 * </pre>
 *
 * @since 0.3
 */
public interface JvmJarBinary extends JarBinary {
}
