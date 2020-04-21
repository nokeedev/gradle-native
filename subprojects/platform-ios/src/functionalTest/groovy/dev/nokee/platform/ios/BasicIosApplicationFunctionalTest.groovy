package dev.nokee.platform.ios

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.platform.jni.fixtures.ObjectiveCIosApp

import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

import static org.junit.Assert.assertEquals

class BasicIosApplicationFunctionalTest extends AbstractFunctionalSpec {
	def "can assemble"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assemble')
		then:
		result.assertTasksExecutedAndNotSkipped(':compileMainExecutableMainObjc', ':linkMainExecutable', ':mainExecutable',
			':compileStoryboard', ':linkStoryboard', ':compileAssetCatalog', ':createApplicationBundle', ':processPropertyList', ':signApplicationBundle',
			':assemble')
		bundle('build/ios/products/main/Application.app').assertHasDescendants('Application', 'Base.lproj/LaunchScreen.storyboardc', 'Base.lproj/Main.storyboardc', 'Info.plist', '_CodeSignature/CodeResources')
		// TODO: Check that it's signed
		// TODO: Check what is the target of the app
	}

	BundleFixture bundle(String path) {
		return new BundleFixture(file(path))
	}

	static class BundleFixture {
		private final TestFile bundle

		BundleFixture(TestFile bundle) {
			bundle.assertIsDirectory()
			this.bundle = bundle
		}

		BundleFixture assertHasDescendants(String... descendants) {
			Set<String> actual = new TreeSet<String>();
			visit(actual);
			Set<String> expected = new TreeSet<String>(Arrays.asList(descendants));

			Set<String> extras = new TreeSet<String>(actual);
			extras.removeAll(expected);
			Set<String> missing = new TreeSet<String>(expected);
			missing.removeAll(actual);

			assertEquals(String.format("For dir: %s\n extra files: %s, missing files: %s, expected: %s", this, extras, missing, expected), expected, actual);

			return this;
		}

		private void visit(Set<String> actual) {
			Path baseDirectory = bundle.toPath();
			Files.walkFileTree(baseDirectory, new FileVisitor<Path>() {
				@Override
				FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
					if (path.getFileName().toString().endsWith(".storyboardc")) {
						actual.add(baseDirectory.relativize(path).toString());
						return FileVisitResult.SKIP_SUBTREE;
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
					actual.add(baseDirectory.relativize(path).toString());
					return FileVisitResult.CONTINUE
				}

				@Override
				FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
					return FileVisitResult.TERMINATE
				}

				@Override
				FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
					return FileVisitResult.CONTINUE
				}
			});
		}
	}

	void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.ios-application'
				id 'dev.nokee.objective-c-language'
			}
		'''
		settingsFile << "rootProject.name = 'application'"
	}

	ObjectiveCIosApp getComponentUnderTest() {
		return new ObjectiveCIosApp()
	}
}
