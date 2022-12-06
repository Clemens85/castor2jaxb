package com.ottogroup.buying.castor2jaxb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Castor2JaxbMigratorTest {

  private static final String UTF_8 = "UTF-8";
  private static final String[] JAVA_EXT = new String[] { "java" };
  private static final String JAVA_TEST_SRC_DIR_RELATIVE = "src/test/java";

  @BeforeEach
  void setUp() throws IOException {
    copyJavaClasses(getFixtureSrcDirectory(), getBackupSrcDirectory(), null);
  }

  @AfterEach
  void tearDown() throws IOException {
    copyJavaClasses(getBackupSrcDirectory(), getFixtureSrcDirectory(), null);
    FileUtils.deleteDirectory(getBackupSrcDirectory());
  }

	@Test
  void testMigration() throws IOException, URISyntaxException {
    CastorToJaxbMigrator //
        .newInstance("src/test/resources/mapping.castor", JAVA_TEST_SRC_DIR_RELATIVE) //
        .migrate();

    // Migrated files can be seen in this package:
    copyJavaClasses(getFixtureSrcDirectory(), getResultSrcDirectory(),
        "com.ottogroup.buying.result");
    
    // Naive assertion just to ensure that something happened after all
    Collection<File> migratedFiles = listJavaFiles(getResultSrcDirectory());
    assertEquals(4, migratedFiles.size());

  }

  private void copyJavaClasses(File srcPackageDirectory, File destPackageDirectory,
      String newPackageNameInDestDirectory) throws IOException {

    FileUtils.copyDirectory(srcPackageDirectory, destPackageDirectory, FileFileFilter.INSTANCE);
    if (StringUtils.isEmpty(newPackageNameInDestDirectory)) {
      return;
    }

    Collection<File> javaClasses = FileUtils.listFiles(destPackageDirectory, JAVA_EXT, false);
    for (File javaClass : javaClasses) {
      adjustPackageName(javaClass, newPackageNameInDestDirectory);
    }
  }

  private File getFixtureSrcDirectory() {
    return new File(getJavaTestSrcDirAbsolute() + "/com/ottogroup/buying/fixture");
  }

  private File getResultSrcDirectory() {
    return new File(getJavaTestSrcDirAbsolute() + "/com/ottogroup/buying/result");
  }

  private File getBackupSrcDirectory() {
    return new File(getJavaTestSrcDirAbsolute() + "/com/ottogroup/buying/fixture/backup");
  }

  private String getJavaTestSrcDirAbsolute() {
    return CastorToJaxbMigrator.ROOT_PROJECT_PATH + "/" + JAVA_TEST_SRC_DIR_RELATIVE;
  }

  private void adjustPackageName(File javaClass, String newPackageName) throws IOException {
    String javaFileAsString = FileUtils.readFileToString(javaClass, UTF_8);
    javaFileAsString = javaFileAsString.replaceFirst(
        "\\s*package\\s*com\\.ottogroup\\.buying\\.fixture;",
        "package " + newPackageName + ";");
    FileUtils.writeStringToFile(javaClass, javaFileAsString, UTF_8);
  }

  private Collection<File> listJavaFiles(File dir) {
    return FileUtils.listFiles(dir, JAVA_EXT, false);
  }

}
