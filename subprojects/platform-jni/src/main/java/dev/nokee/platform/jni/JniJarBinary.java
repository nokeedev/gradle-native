package dev.nokee.platform.jni;

/**
 * Configuration for a Java Native Interface (JNI) JAR binary.
 * A JNI JAR refers to a JAR binary containing only the shared library binary.
 * It's only present if the library generate multiple variants.
 * If the JNI library has only a single variant, the shared library binary is included inside the {@link JvmJarBinary}.
 *
 * The binaries are accessible via the library's {@link dev.nokee.platform.base.BinaryView} containing all the binary instance of a library:
 * <pre class="autoTested">
 * plugins {
 *     id 'dev.nokee.jni-library'
 *     id 'dev.nokee.cpp-language'
 *     id 'java'
 * }
 *
 * import dev.nokee.platform.jni.JniJarBinary
 *
 * library {
 *     binaries.withType(JniJarBinary).configureEach {
 *         // ...
 *     }
 * }
 * </pre>
 *
 * The binary of a specific variant is also accessible via the variant's {@link dev.nokee.platform.base.BinaryView}:
 * <pre class="autoTested">
 * plugins {
 *     id 'dev.nokee.jni-library'
 *     id 'dev.nokee.cpp-language'
 *     id 'java'
 * }
 *
 * import dev.nokee.platform.jni.JniJarBinary
 *
 * library {
 *     variants.configureEach {
 *         binaries.withType(JniJarBinary).configureEach {
 *             // ...
 *         }
 *     }
 * }
 * </pre>
 *
 * @since 0.3
 */
public interface JniJarBinary extends JarBinary {
}
