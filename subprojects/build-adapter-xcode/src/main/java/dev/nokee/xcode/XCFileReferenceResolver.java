package dev.nokee.xcode;

import dev.nokee.xcode.internal.XCFileReference;

import java.io.File;

class XCFileReferenceResolver {
	private static final String FILE_REFERENCE_GROUP_LOCATION_TAG = "group:";
	private static final String FILE_REFERENCE_ABSOLUTE_LOCATION_TAG = "absolute:";
	private final File baseDirectory;

	public XCFileReferenceResolver(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public File resolve(XCFileReference fileReference) {
		if (isRelativeToWorkspace(fileReference)) {
			return new File(baseDirectory, withoutPrefix(fileReference.getLocation()));
		} else if (isAbsolute(fileReference)) {
			return new File(withoutPrefix(fileReference.getLocation()));
		}
		throw new IllegalArgumentException(String.format("Unknown Xcode workspace file reference '%s'.", fileReference));
	}

	private boolean isRelativeToWorkspace(XCFileReference fileReferenceLocation) {
		return fileReferenceLocation.getLocation().startsWith(FILE_REFERENCE_GROUP_LOCATION_TAG);
	}

	private String withoutPrefix(String fileReference) {
		return fileReference.substring(fileReference.indexOf(':') + 1);
	}

	private boolean isAbsolute(XCFileReference fileReferenceLocation) {
		return fileReferenceLocation.getLocation().startsWith(FILE_REFERENCE_ABSOLUTE_LOCATION_TAG);
	}
}
