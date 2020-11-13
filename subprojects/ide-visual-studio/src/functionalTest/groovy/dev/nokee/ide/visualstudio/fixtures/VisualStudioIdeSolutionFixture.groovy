package dev.nokee.ide.visualstudio.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.ide.fixtures.IdePathUtils
import dev.nokee.ide.fixtures.IdeWorkspaceFixture
import groovy.transform.ToString
import org.gradle.util.TextUtil

import java.nio.channels.FileChannel
import java.nio.channels.FileLock

class VisualStudioIdeSolutionFixture implements IdeWorkspaceFixture {
	final SolutionFile solutionFile

	VisualStudioIdeSolutionFixture(TestFile solutionFile) {
		this.solutionFile = new SolutionFile(solutionFile)
//		assert TextUtil.convertLineSeparators(solutionFile.text, TextUtil.windowsLineSeparator) == solutionFile.text : "Solution file contains non-windows line separators"
	}

	static VisualStudioIdeSolutionFixture of(Object path) {
		return new VisualStudioIdeSolutionFixture(TestFile.of(new File(solutionName(path))))
	}

	static String solutionName(Object path) {
		return IdePathUtils.addExtensionIfAbsent(path, 'sln')
	}

	VisualStudioIdeSolutionFixture assertHasProjects(Iterable<String> names) {
		assert solutionFile.projects.keySet() == names as Set
		return this
	}

	VisualStudioIdeSolutionFixture assertHasProjectConfigurations(String... projectConfigurations) {
		assert solutionFile.projectConfigurations as Set == projectConfigurations as Set
		return this
	}
//
//	void assertHasProject(File projectFile) {
//		assert contentFile.projectLocationPaths.contains(projectFile.absolutePath)
//	}
//
//	void assertDerivedDataLocationRelativeToWorkspace(String path) {
//		def settings = WorkspaceSettingsFile.user(dir)
//		settings.derivedDataCustomLocation.with {
//			assert it.present
//			assert it.get() == path
//		}
//		settings.derivedDataLocationStyle.with {
//			assert it.present
//			assert it.get() == 'WorkspaceRelativePath'
//		}
//	}
//
//	List<XcodeIdeProjectFixture> getProjects() {
//		return contentFile.projectLocationPaths.collect { new XcodeIdeProjectFixture(it) }
//	}

	static class SolutionFile {
		final TestFile file
		final String content
		final Map<String, ProjectReference> projects = [:]
		final List<String> projectConfigurations = []

		SolutionFile(TestFile solutionFile) {
			solutionFile.assertIsFile()
			file = solutionFile
			content = TextUtil.normaliseLineSeparators(solutionFile.text)
			content.findAll(~/(?m)^Project\(\"\{8BC9CEB8-8B4A-11D0-8D11-00A0C91BC942\}\"\) = \"([\w\-]+)\", \"([^\"]*)\", \"\{([\w\-]+)\}\"$/, {
				projects.put(it[1], new ProjectReference(it[1], it[2], it[3]))
			})

			visitGlobalSection(new GlobalSectionVisitor() {
				@Override
				void visitSolutionConfigurationPlatform(String configurationName, String platformName) {
					projectConfigurations.add("${configurationName}|${platformName}")
				}

				@Override
				void visitProjectConfigurationPlatform(String guid) {
					assert projects.any { name, reference -> reference.guid == guid }
				}
			})
		}

		private void visitGlobalSection(GlobalSectionVisitor visitor) {
			def iter = content.readLines().iterator()
			while (iter.hasNext()) {
				def line = iter.next()
				if (line.trim().startsWith('GlobalSection')) {
					if (line.contains('(SolutionConfigurationPlatforms)')) {
						visitingSolutionConfigurationPlatforms(iter, visitor)
					} else if (line.contains('(ProjectConfigurationPlatforms)')) {
						visitingProjectConfigurationPlatforms(iter, visitor)
					} else {
						// ignore section
					}
				}
			}
		}

		private void visitingSolutionConfigurationPlatforms(Iterator<String> iter, GlobalSectionVisitor visitor) {
			while (iter.hasNext()) {
				def line = iter.next()
				if (line.trim().startsWith('EndGlobalSection')) {
					return // normal exit condition
				}
				def tokens = line.split('=')[0].trim().split('\\|')
				visitor.visitSolutionConfigurationPlatform(tokens[0], tokens[1])
			}
			throw new IllegalArgumentException("Corrupted solution file at '${file.absolutePath}'.")
		}

		private void visitingProjectConfigurationPlatforms(Iterator<String> iter, GlobalSectionVisitor visitor) {
			while (iter.hasNext()) {
				def line = iter.next()
				if (line.trim().startsWith('EndGlobalSection')) {
					return // normal exit condition
				}
				def (String left, String right) = line.split('=')
				def (uuid, projectConfigurationPlatform, tag) = left.trim().split('\\.')
				visitor.visitProjectConfigurationPlatform(uuid)
			}
			throw new IllegalArgumentException("Corrupted solution file at '${file.absolutePath}'.")
		}
	}

	interface GlobalSectionVisitor {
		void visitSolutionConfigurationPlatform(String configurationName, String platformName)
		void visitProjectConfigurationPlatform(String guid)
	}

	@ToString
	static class ProjectReference {
		final String name
		final String file
		final String uuid

		ProjectReference(String name, String file, String uuid) {
			this.name = name
			this.file = file
			this.uuid = uuid
		}

		String getGuid() {
			return "{${uuid}}"
		}
	}

	//region .vs Directory
	DotvsDirectory getDotvsDirectory() {
		return new DotvsDirectory(solutionFile.file.parentFile.createDirectory('.vs'))
	}

	static class DotvsDirectory {
		private final TestFile directory
		private final TestFile fileToLock

		DotvsDirectory(TestFile directory) {
			this.directory = directory
			directory.createFile('foo')
			directory.createFile('bar')
			this.fileToLock = directory.createFile('file-to-lock')
		}

		void assertExists() {
			directory.assertHasDescendants('foo', 'bar', 'file-to-lock')
		}

		void assertDoesNotExist() {
			directory.assertDoesNotExist()
		}

		Lock simulateVisualStudioIdeLock() {
			def inStream = null
			def fileChannel = null
			def lock = null
			try {
				inStream = new RandomAccessFile(fileToLock, 'rw')
				fileChannel = inStream.getChannel()
				lock = fileChannel.lock()
				return new Lock(inStream, fileChannel, lock)
			} catch (Throwable ex) {
				lock?.close()
				fileChannel?.close()
				inStream?.close()
				throw ex
			}
		}

		static class Lock implements AutoCloseable {
			private final RandomAccessFile inStream
			private final FileChannel fileChannel
			private final FileLock lock

			Lock(RandomAccessFile inStream, FileChannel fileChannel, FileLock lock) {
				this.inStream = inStream
				this.fileChannel = fileChannel
				this.lock = lock
			}

			@Override
			void close() {
				lock.close()
				fileChannel.close()
				inStream.close()
			}
		}
	}
	//endregion
}
