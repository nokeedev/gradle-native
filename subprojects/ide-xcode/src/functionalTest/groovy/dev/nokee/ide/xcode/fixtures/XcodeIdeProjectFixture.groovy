package dev.nokee.ide.xcode.fixtures

import com.dd.plist.*
import com.google.common.base.MoreObjects
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.ide.fixtures.IdePathUtils
import dev.nokee.ide.fixtures.IdeProjectFixture

import javax.annotation.Nullable

class XcodeIdeProjectFixture implements IdeProjectFixture {
	final TestFile dir
	final TestFile workspaceSettingsFile
	final ProjectFixture projectFile
	final List<SchemeFixture> schemes = []

	XcodeIdeProjectFixture(TestFile projectLocation) {
		projectLocation.assertIsDirectory()
		dir = projectLocation
		projectFile = new ProjectFixture(projectLocation.file('project.pbxproj'))
		workspaceSettingsFile = dir.file('project.xcworkspace/xcshareddata/WorkspaceSettings.xcsettings')

		def xcschemesDir = dir.file('xcshareddata/xcschemes')
		if (xcschemesDir.exists()) {
			xcschemesDir.eachFileMatch ~/.*\.xcscheme/, { schemes << new SchemeFixture(TestFile.of(it)) }
		}
	}

	static XcodeIdeProjectFixture of(Object path) {
		return new XcodeIdeProjectFixture(TestFile.of(new File(projectName(path))))
	}

	static String projectName(Object path) {
		return IdePathUtils.addExtensionIfAbsent(path, 'xcodeproj')
	}

	XcodeIdeProjectFixture assertHasSchemes(String... schemeNames) {
		assert schemes*.name as Set == schemeNames as Set
		return this
	}

	XcodeIdeProjectFixture assertHasSourceLayout(String... layout) {
		return assertHasSourceLayout(Arrays.asList(layout))
	}

	XcodeIdeProjectFixture assertHasSourceLayout(Iterable<String> layout) {
		projectFile.mainGroup.assertHasSourceLayout(layout)
		return this
	}

	@Override
	XcodeIdeProjectFixture assertHasBuildFiles(Iterable<String> files) {
		projectFile.mainGroup.files == files as Set
		return this
	}

	XcodeIdeProjectFixture assertHasTargets(String... targetNames) {
		assert projectFile.targets*.name as Set == targetNames as Set
		return this
	}

	XcodeIdeProjectFixture assertHasBuildConfigurations(String... buildConfigurationNames) {
		assert projectFile.buildConfigurations*.name as Set == buildConfigurationNames as Set
		return this
	}

	@Override
	IdeProjectFixture assertHasBuildTypes(Iterable<String> buildTypes) {
		assert projectFile.buildConfigurations*.name as Set == buildTypes as Set
		return this
	}

	List<ProjectFixture.PBXTargetFixture> getTargets() {
		return projectFile.targets
	}

	ProjectFixture.PBXTargetFixture getTargetByName(String targetName) {
		def target = projectFile.targets.find { it.name == targetName }
		assert target != null
		return target
	}

	XcodeIdeProjectFixture assertHasTarget(String targetName) {
		assert projectFile.targets*.name.contains(targetName)
		return this
	}

	ProjectFixture.PBXGroupFixture getGroupByName(String groupName) {
		return projectFile.mainGroup.getGroupByName(groupName)
	}

	ProjectFixture.PBXGroupFixture getProductsGroup() {
		return projectFile.mainGroup.getGroupByName('Products')
	}

	ProjectFixture.PBXGroupFixture getMainGroup() {
		return projectFile.mainGroup
	}

	static class SchemeFixture {
		final TestFile file
		final String name
		final Node schemeXml

		SchemeFixture(TestFile schemeFile) {
			schemeFile.assertIsFile()
			file = schemeFile
			name = file.name.replace('.xcscheme', '')
			schemeXml = new XmlParser().parse(file)
		}
	}

	static class ProjectFixture {
		final TestFile file
		final NSDictionary content
		private Map<String, NSObject> objects
		private PBXObjectFixture rootObject

		ProjectFixture(TestFile pbxProjectFile) {
			pbxProjectFile.assertIsFile()
			file = pbxProjectFile
			content = PropertyListParser.parse(file)
			objects = ((NSDictionary)content.get('objects')).getHashMap()
			rootObject = toPbxObject(toNSString(content.get('rootObject')).getContent())
		}

		protected Map<String, NSObject> getObjects() {
			return objects
		}

		List<XCBuildConfigurationFixture> getBuildConfigurations() {
			return rootObject.getObject('buildConfigurationList').getObject('buildConfigurations')
		}

		PBXObjectFixture getBuildConfigurationList() {
			return rootObject.getObject('buildConfigurationList')
		}

		List<PBXTargetFixture> getTargets() {
			return rootObject.getObject('targets')
		}

		PBXGroupFixture getMainGroup() {
			return rootObject.getObject('mainGroup')
		}

		private <T extends PBXObjectFixture> T toPbxObject(String id) {
			NSDictionary object = (NSDictionary)getObjects().get(id)

			if (object.isa.toJavaObject() == 'PBXGroup') {
				return new PBXGroupFixture(id, object)
			} else if (object.isa.toJavaObject() == 'PBXLegacyTarget') {
				return new PBXLegacyTargetFixture(id, object)
			} else if (object.isa.toJavaObject() == 'PBXNativeTarget') {
				return new PBXNativeTargetFixture(id, object)
			} else if (object.isa.toJavaObject() == 'XCBuildConfiguration') {
				return new XCBuildConfigurationFixture(id, object)
			} else if (object.isa.toJavaObject() == 'PBXFileReference') {
				return new PBXFileReferenceFixture(id, object)
			} else {
				return new PBXObjectFixture(id, object)
			}
		}

		private static NSString toNSString(NSObject object) {
			return (NSString)object
		}

		class PBXObjectFixture {
			final String id
			private final NSDictionary object

			PBXObjectFixture(String id, NSDictionary object) {
				this.id = id
				this.object = object
			}

			@Nullable
			def getObject(String name) {
				def value = object.get(name)
				if (value == null) {
					return null
				} else if (isId(value)) {
					return toPbxObject(toNSString(value).getContent())
				} else if (value instanceof NSArray) {
					def list = []
					for (NSObject obj : value.getArray()) {
						if (isId(obj)) {
							list.add(toPbxObject(toNSString(obj).getContent()))
						} else {
							list.add(obj)
						}
					}
					return list
				}
				return value.toJavaObject()
			}

			private static boolean isId(NSObject obj) {
				// Check if the value is a FB generated id (static 24 chars)
				if (obj instanceof NSString && (obj.getContent().length() == 24)) {
					return obj.getContent().toCharArray().every {
						Character.isDigit(it) || Character.isUpperCase(it)
					}
				}
				return false
			}

			@Override
			String toString() {
				MoreObjects.toStringHelper(this)
					.add('isa', getObject('isa'))
					.toString()
			}
		}

		class PBXFileReferenceFixture extends PBXObjectFixture {
			PBXFileReferenceFixture(String id, NSDictionary object) {
				super(id, object)
			}

			String getName() {
				return getObject('name')
			}
		}

		class XCBuildConfigurationFixture extends PBXObjectFixture {
			XCBuildConfigurationFixture(String id, NSDictionary object) {
				super(id, object)
			}

			String getName() {
				return getObject('name')
			}

			Map<String, String> getBuildSettings() {
				def map = [:]
				getObject('buildSettings').entrySet().each {
					map.put(it.key, toNSString(it.value).getContent())
				}
				return map
			}
		}

		class PBXGroupFixture extends PBXObjectFixture {
			PBXGroupFixture(String id, NSDictionary object) {
				super(id, object)
			}

			String getName() {
				return getObject('name')
			}

			List<PBXObjectFixture> getChildren() {
				return getObject('children')
			}

			PBXGroupFixture getGroupByName(String groupName) {
				def group = (PBXGroupFixture) getObject('children').find { it instanceof PBXGroupFixture && it.name == groupName }
				assert group != null
				return group
			}

			PBXGroupFixture assertHasSourceLayout(String... layout) {
				return assertHasSourceLayout(Arrays.asList(layout))
			}

			PBXGroupFixture assertHasSourceLayout(Iterable<String> layout) {
				assert listSource(this) as Set == layout as Set
				return this
			}

			Set<String> getFiles() {
				return getObject('children').findAll { !(it instanceof PBXGroupFixture) }*.name as Set
			}

			def assertHasChildren(List<String> entries) {
				def children = getObject('children')
				assert children.size() == entries.size()
				assert children*.name.containsAll(entries)
				return true
			}

			@Override
			String toString() {
				MoreObjects.toStringHelper(this)
					.add('name', getName())
					.toString()
			}
		}

		class PBXTargetFixture extends PBXObjectFixture {
			PBXTargetFixture(String id, NSDictionary object) {
				super(id, object)
			}

			String getName() {
				return getObject('name')
			}

			String getProductName() {
				return getObject('productName')
			}

			@Nullable
			PBXObjectFixture getProductReference() {
				return getObject('productReference')
			}

			String getProductType() {
				return getObject('productType')
			}

			List<XCBuildConfigurationFixture> getBuildConfigurations() {
				return this.buildConfigurationList.buildConfigurations
			}

			@Override
			String toString() {
				MoreObjects.toStringHelper(this)
					.add('name', getName())
					.add('productName', getProductName())
					.add('productType', getProductType())
					.toString()
			}

			void assertSupportedArchitectures(String... architectures) {
				def toXcodeArchitecture = [x86: 'i386', 'x86-64': 'x86_64'].withDefault { it }
				String expectedValidArchitectures = architectures.collect { toXcodeArchitecture.get(it) }.join(" ")
				assert this.buildConfigurationList.buildConfigurations.every { it.buildSettings.VALID_ARCHS == expectedValidArchitectures }
			}

			PBXTargetFixture assertTargetDelegateToGradle() {
				throw new AssertionError((Object)String.format("The target of type %s is not delegating to Gradle.", getObject('isa')))
			}
		}

		class PBXNativeTargetFixture extends PBXTargetFixture {
			PBXNativeTargetFixture(String id, NSDictionary object) {
				super(id, object)
			}

			Map<String, ?> getBuildSettings() {
				return buildConfigurationList.buildConfigurations[0].buildSettings
			}
		}

		class PBXLegacyTargetFixture extends PBXTargetFixture {
			PBXLegacyTargetFixture(String id, NSDictionary object) {
				super(id, object)
			}

			String getBuildToolPath() {
				return getObject('buildToolPath')
			}

			String getBuildArgumentsString() {
				return getObject('buildArgumentsString').toString()
			}

			List<String> getBuildArguments() {
				return getBuildArgumentsString().split(' ')
			}

			/**
			 * Asserts the target delegate to Gradle build tool.
			 * Delegating to Gradle means several things:
			 * <ul>
			 *     <li>a PBXLegacyTarget</li>
			 *     <li>a build tool path ending with Gradle execution entry point</li>
			 *     <li>build arguments forwarding important build settings to Gradle via project properties</li>
			 *     <li>build arguments containing the Xcode IDE bridge task format</li>
			 *     <li>no build settings as environment conversion</li>
			 * </ul>
			 *
			 * @return this PBXTarget fixture instance, never null
			 */
			@Override
			PBXTargetFixture assertTargetDelegateToGradle() {
				assert getBuildToolPath() =~ /\/bin\/gradle(w)?$/
				getBuildArguments().with {
					assert it.size() >= 7 // At the very least we expect the following arguments
					assert it.contains('-Pdev.nokee.internal.xcode.bridge.ACTION="${ACTION}"')
					assert it.contains('-Pdev.nokee.internal.xcode.bridge.PRODUCT_NAME="${PRODUCT_NAME}"')
					assert it.contains('-Pdev.nokee.internal.xcode.bridge.CONFIGURATION="${CONFIGURATION}"')
					assert it.contains('-Pdev.nokee.internal.xcode.bridge.BUILT_PRODUCTS_DIR="${BUILT_PRODUCTS_DIR}"')
					assert it.contains('-Pdev.nokee.internal.xcode.bridge.PROJECT_NAME="${PROJECT_NAME}"')
					assert it.contains('-Pdev.nokee.internal.xcode.bridge.TARGET_NAME="${TARGET_NAME}"')
					assert it.contains(':_xcode__${ACTION}_${PROJECT_NAME}_${TARGET_NAME}_${CONFIGURATION}')
				}
				assert getObject('passBuildSettingsInEnvironment').toString() == '0'
				return this
			}
		}
	}

	private static List<String> listSource(ProjectFixture.PBXGroupFixture group) {
		return group.children.collect { child ->
			if (child instanceof ProjectFixture.PBXGroupFixture) {
				return listSource(child).collect { "${child.name}/${it}" }
			}
			return child.name
		}.flatten()
	}
}
