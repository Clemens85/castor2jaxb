package com.ottogroup.buying.castor2jaxb.generator;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import com.ottogroup.buying.castor2jaxb.bindings.CastorBindXmlNodeType;

public class JaxbCodeGenerator {

  public static final String XML_ATTRIBUTE_IMPORT = "import javax.xml.bind.annotation.XmlAttribute;";
  public static final String XML_ELEMENT_IMPORT = "import javax.xml.bind.annotation.XmlElement;";
  public static final String XML_ROOTELEMENT_IMPORT = "import javax.xml.bind.annotation.XmlRootElement;";
  public static final String XML_TRANSIENT_IMPORT = "import javax.xml.bind.annotation.XmlTransient;";
  public static final String XML_TYPE_IMPORT = "import javax.xml.bind.annotation.XmlType;";
  public static final String XML_JAVA_ADAPTER_IMPORT = "import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;";

  private static final Pattern IMPORT_PATTERN = Pattern.compile("import .*;");
  private static final Pattern PACKAGE_PATTERN = Pattern.compile("package .*;");
  private static final Pattern ANY_PUBLIC_METHOD_PATTERN = Pattern.compile("\\s*public.*\\(.*\\).*\\{");

  private static final String TWO_SPACES_INDENT = "  ";

  private String absoluteFilePath;
  private List<String> fileLines;
  private Set<String> importsAdded = new HashSet<>(6);

  private JaxbCodeGenerator(String absoluteFilePath) {
    this.absoluteFilePath = absoluteFilePath;
    readFileContents();

  }
  public static JaxbCodeGenerator newInstance(String absoluteFilePath) {
    return new JaxbCodeGenerator(absoluteFilePath);
  }
  
  public void addImport(String importLine) {

    if (importsAdded.contains(importLine)) {
      return;
    }
    importsAdded.add(importLine);

    if (addLineBeforePatternMatch(IMPORT_PATTERN, importLine)) {
      return;
    }

    List<String> resultingFileLines = new ArrayList<>(fileLines);
    boolean foundPackageLine = false;
    for (int i = 0; i < fileLines.size(); i++) {
      Matcher matcher = PACKAGE_PATTERN.matcher(fileLines.get(i));
      if (matcher.find()) {
        resultingFileLines.add(i + 1, importLine); // Add after package declaration
        foundPackageLine = true;
        break;
      }
    }
    if (!foundPackageLine) { // Default package
      resultingFileLines.add(importLine);
    }
    this.fileLines = resultingFileLines;
  }

  public void addXmlRootElementAnnotation(Class<?> clazz, String xmlRootElementName) {
    Pattern pattern = getPatternForClass(clazz);
    addLineBeforePatternMatch(pattern, "@XmlRootElement(name = \"" + xmlRootElementName + "\")");
  }

  public void addXmlMappingAnnotation(CastorBindXmlNodeType xmlNodeType, String fieldXmlName, Method getMethod) {
    Pattern patternForMethodMatching = getPatternForMethodMatching(getMethod);
    addLineBeforePatternMatch(patternForMethodMatching, getXmlFieldMappingLine(xmlNodeType, fieldXmlName));
  }

  public void addXmlTransientAnnotations(List<Method> transientMethods) {
    for (Method method : transientMethods) {
      addLineBeforePatternMatch(getPatternForMethodMatching(method), TWO_SPACES_INDENT + "@XmlTransient");
    }
  }

  public void addXmlTypeOrderAnnotation(Class<?> clazz, List<String> orderedFields) {
    StringBuilder orderedFieldsAsString = new StringBuilder();
    int cnt = 0;
    for (String field : orderedFields) {
      if (cnt++ > 0) {
        orderedFieldsAsString.append(", ");
      }
      orderedFieldsAsString.append("\"").append(field).append("\"");
    }
    String xmlTypeOrderLine = "@XmlType(propOrder = { " + orderedFieldsAsString.toString() + " })";

    addLineBeforePatternMatch(getPatternForClass(clazz), xmlTypeOrderLine);
  }

  public void rewriteMethodFromIterableToList(Method getMethod) {
    String iterableMethodRegex = "(\\s*public\\s*)(Iterable<)(.*" + getMethod.getName() + ")";
    Pattern iterableMethodPattern = Pattern.compile(iterableMethodRegex);

    Map<Integer, String> linesToReplace = new HashMap<>();
    
    for (int i = 0; i < fileLines.size(); i++) {
      String line = fileLines.get(i);
      Matcher matcher = iterableMethodPattern.matcher(line);
      if (matcher.find()) {
        line = line.replaceFirst(iterableMethodRegex, "$1java.util.List<$3");
        linesToReplace.put(i, line);
        break;
      }
    }

    linesToReplace.forEach((index, line) -> {
      fileLines.set(index, line);
    });
  }

  public void generateDefaultConstructor(Class<?> javaClazz) {
    StringBuilder ctor = new StringBuilder();
    ctor.append(TWO_SPACES_INDENT).append("protected ").append(javaClazz.getSimpleName()).append("() { }\r\n");
    addLineBeforePatternMatch(ANY_PUBLIC_METHOD_PATTERN, ctor.toString());

    removeFinalFromPrivateFields();
  }

  private void removeFinalFromPrivateFields() {
    try {
      StringWriter tmpWriter = new StringWriter();
      IOUtils.writeLines(fileLines, null, tmpWriter);
      String javaFileAsString = tmpWriter.toString();
      javaFileAsString = javaFileAsString.replaceAll("(\\s*private\\s*)(final)(.*;)", "$1$3");
      fileLines = new ArrayList<>(IOUtils.readLines(new StringReader(javaFileAsString)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Pattern getPatternForMethodMatching(Method method) {
    String methodName = method.getName();
    return Pattern.compile("\\s*public.*" + methodName + "\\s*\\(");
  }

  private static String getXmlFieldMappingLine(CastorBindXmlNodeType xmlNodeType, String fieldXmlName) {
    if (xmlNodeType == CastorBindXmlNodeType.ATTRIBUTE) {
      return TWO_SPACES_INDENT + "@XmlAttribute(name = \"" + fieldXmlName + "\")";
    } else if (xmlNodeType == CastorBindXmlNodeType.ELEMENT) {
      return TWO_SPACES_INDENT + "@XmlElement(name = \"" + fieldXmlName + "\")";
    }
    throw new IllegalArgumentException("Cannot handle " + xmlNodeType + " for " + fieldXmlName);
  }

  private static Pattern getPatternForClass(Class<?> clazz) {
    return Pattern.compile("\\s*public.*class\\s*" + clazz.getSimpleName());
  }

  private boolean addLineBeforePatternMatch(Pattern pattern, String line) {
    boolean patternMatched = false;
    List<String> resultingFileLines = new ArrayList<>(fileLines);
    for (int i = 0; i < fileLines.size(); i++) {
      Matcher matcher = pattern.matcher(fileLines.get(i));
      if (matcher.find()) {
        resultingFileLines.add(i, line);
        patternMatched = true;
        break;
      }
    }
    this.fileLines = resultingFileLines;
    return patternMatched;
  }

  public void saveToFileAndClose() {
    try (OutputStream out = new FileOutputStream(absoluteFilePath)) {
      IOUtils.writeLines(fileLines, null, out, Charset.defaultCharset());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void readFileContents() {
    try (InputStream is = new FileInputStream(absoluteFilePath)) {
      fileLines = new ArrayList<>(IOUtils.readLines(is, Charset.defaultCharset()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
