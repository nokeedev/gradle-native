package dev.nokee.platform.ios.fixtures.elements

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile

import java.nio.file.Files

import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;

class NokeeAppAssets extends SourceElement {
	@Override
	List<SourceFile> getFiles() {
		return [
		    sourceFile('resources/Assets.xcassets', 'Contents.json', '''{
  "info" : {
    "version" : 1,
    "author" : "xcode"
  }
}
'''),
			sourceFile('resources/Assets.xcassets/AppIcon.appiconset', 'Contents.json', '''{
  "images" : [
    {
      "idiom" : "iphone",
      "size" : "20x20",
      "scale" : "2x"
    },
    {
      "idiom" : "iphone",
      "size" : "20x20",
      "scale" : "3x"
    },
    {
      "idiom" : "iphone",
      "size" : "29x29",
      "scale" : "2x"
    },
    {
      "idiom" : "iphone",
      "size" : "29x29",
      "scale" : "3x"
    },
    {
      "idiom" : "iphone",
      "size" : "40x40",
      "scale" : "2x"
    },
    {
      "idiom" : "iphone",
      "size" : "40x40",
      "scale" : "3x"
    },
    {
      "idiom" : "iphone",
      "size" : "60x60",
      "scale" : "2x"
    },
    {
      "idiom" : "iphone",
      "size" : "60x60",
      "scale" : "3x"
    },
    {
      "idiom" : "ipad",
      "size" : "20x20",
      "scale" : "1x"
    },
    {
      "idiom" : "ipad",
      "size" : "20x20",
      "scale" : "2x"
    },
    {
      "idiom" : "ipad",
      "size" : "29x29",
      "scale" : "1x"
    },
    {
      "idiom" : "ipad",
      "size" : "29x29",
      "scale" : "2x"
    },
    {
      "idiom" : "ipad",
      "size" : "40x40",
      "scale" : "1x"
    },
    {
      "idiom" : "ipad",
      "size" : "40x40",
      "scale" : "2x"
    },
    {
      "idiom" : "ipad",
      "size" : "76x76",
      "scale" : "1x"
    },
    {
      "idiom" : "ipad",
      "size" : "76x76",
      "scale" : "2x"
    },
    {
      "idiom" : "ipad",
      "size" : "83.5x83.5",
      "scale" : "2x"
    },
    {
      "idiom" : "ios-marketing",
      "size" : "1024x1024",
      "scale" : "1x"
    }
  ],
  "info" : {
    "version" : 1,
    "author" : "xcode"
  }
}
'''),
			sourceFile('resources/Assets.xcassets/full-green.imageset', 'Contents.json', '''{
  "images" : [
    {
      "idiom" : "universal",
      "filename" : "full-green.pdf",
      "scale" : "1x"
    },
    {
      "idiom" : "universal",
      "scale" : "2x"
    },
    {
      "idiom" : "universal",
      "scale" : "3x"
    }
  ],
  "info" : {
    "version" : 1,
    "author" : "xcode"
  }
}
''')
		]
	}

	@Override
	void writeToProject(File projectDir) {
		super.writeToProject(projectDir)
        Files.copy(getClass().getResourceAsStream('full-green.pdf'), file(projectDir, 'src/main/resources/Assets.xcassets/full-green.imageset/full-green.pdf').toPath());
	}
}
