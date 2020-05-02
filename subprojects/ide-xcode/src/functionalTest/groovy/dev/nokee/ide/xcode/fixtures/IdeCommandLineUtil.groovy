package dev.nokee.ide.xcode.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import groovy.transform.CompileStatic
import org.gradle.util.GUtil

@CompileStatic
abstract class IdeCommandLineUtil {
	private IdeCommandLineUtil() {}

	static String generateGradleProbeInitFile(String ideTaskName, String ideCommandLineTool) {
		return """
            gradle.startParameter.showStacktrace = ShowStacktrace.ALWAYS_FULL
            Properties gatherEnvironment() {
                Properties properties = new Properties()
                properties.JAVA_HOME = String.valueOf(System.getenv('JAVA_HOME'))
                properties.GRADLE_USER_HOME = String.valueOf(gradle.gradleUserHomeDir.absolutePath)
                properties.GRADLE_OPTS = String.valueOf(System.getenv().getOrDefault('GRADLE_OPTS', ''))
                return properties
            }

            void assertEquals(key, expected, actual) {
				String actualValue = actual[key]
				if (key == 'GRADLE_OPTS') {
					// macOS adds Xdock properties in a funky way.
					// We will remove that because the tests are split between the tooling API (inside the test) and a real distribution (inside Xcode).
					// We should do like in `gradle/gradle` where we require a real distribution (going through the launcher script), but the toolbox doesn't allow that just yet.
					int lastIndex = actualValue.lastIndexOf("\\"-Xdock:name=Gradle\\"")
					if (lastIndex > 0) {
						actualValue = actualValue.substring(0, lastIndex - 1)
					}
				}
                assert expected[key] == actualValue
                if (expected[key] != actualValue) {
                    throw new GradleException(""\"
Environment's \$key did not match!
Expected: \${expected[key]}
Actual: \${actual[key]}
""\")
                }
            }

            rootProject {
                def gradleEnvironment = file("gradle-environment")
                tasks.matching { it.name == '$ideTaskName' }.all { ideTask ->
                    ideTask.doLast {
                        def writer = gradleEnvironment.newOutputStream()
                        gatherEnvironment().store(writer, null)
                        writer.close()
                    }
                }
                gradle.taskGraph.whenReady { taskGraph ->
                    taskGraph.allTasks.last().doLast {
                        if (!gradleEnvironment.exists()) {
                            throw new GradleException("could not determine if $ideCommandLineTool is using the correct environment, did $ideTaskName task run?")
                        } else {
                            def expectedEnvironment = new Properties()
                            expectedEnvironment.load(gradleEnvironment.newInputStream())

                            def actualEnvironment = gatherEnvironment()

                            assertEquals('JAVA_HOME', expectedEnvironment, actualEnvironment)
                            assertEquals('GRADLE_USER_HOME', expectedEnvironment, actualEnvironment)
                            assertEquals('GRADLE_OPTS', expectedEnvironment, actualEnvironment)
                        }
                    }
                }
            }
        """
	}

	static List<String> buildEnvironment(TestFile testDirectory) {
		Map<String, String> envvars = new HashMap<>()
		envvars.putAll(System.getenv())

		Properties props = GUtil.loadProperties(testDirectory.file("gradle-environment"))
		assert !props.isEmpty()

		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			if (entry.key == "GRADLE_OPTS") {
				// macOS adds Xdock properties in a funky way that makes us duplicate them on the command-line
				String value = entry.value.toString()
				int lastIndex = value.lastIndexOf("\"-Xdock:name=Gradle\"")
				if (lastIndex > 0) {
					envvars.put(entry.key.toString(), value.substring(0, lastIndex - 1))
					continue
				}
			}
			envvars.put(entry.key.toString(), entry.value.toString())
		}

		return envvars.entrySet().collect { "${it.key}=${it.value}".toString() }
	}
}
