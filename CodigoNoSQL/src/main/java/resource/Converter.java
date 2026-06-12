
package resource;

import java.io.File;

public interface Converter {

    String convert(File file,
                   String outputName,
                   String outputDir);
}
