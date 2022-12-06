package com.ottogroup.buying.castor2jaxb;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.ottogroup.buying.castor2jaxb.bindings.CastorBindXml;
import com.ottogroup.buying.castor2jaxb.bindings.CastorBindXmlNodeType;
import com.ottogroup.buying.castor2jaxb.bindings.CastorClass;
import com.ottogroup.buying.castor2jaxb.bindings.CastorClassField;
import com.ottogroup.buying.castor2jaxb.bindings.CastorRootMapping;
import com.ottogroup.buying.castor2jaxb.generator.JaxbCodeGenerator;

public class CastorToJaxbMigrator {

  static final String ROOT_PROJECT_PATH = System.getProperty("user.dir");

  private static final boolean addXmlTypeOrdering = true;

  private final String javaSrcDirRelative;
  private final String castorMappingFileRelativeToProjectRoot;

  private CastorToJaxbMigrator(String castorMappingFileRelativToRoot, String javaSrcDirRelative) {
    this.castorMappingFileRelativeToProjectRoot = castorMappingFileRelativToRoot;
    this.javaSrcDirRelative = javaSrcDirRelative;
  }

  public static CastorToJaxbMigrator newInstance(String castorMappingFileRelativeToProjectRoot,
      String javaSrcDirRelative) {

    return new CastorToJaxbMigrator(castorMappingFileRelativeToProjectRoot, javaSrcDirRelative);
  }

  public void migrate() {

    CastorRootMapping mapping;
    try {
      mapping = loadMapping(castorMappingFileRelativeToProjectRoot);
    } catch (FileNotFoundException | JAXBException | ParserConfigurationException
        | SAXException e) {
      throw new RuntimeException(
          "Could not load path to castor xml mapping " + castorMappingFileRelativeToProjectRoot, e);
    }

    try {
      traverseMappedClasses(mapping.getClasses());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

  }

  private void traverseMappedClasses(List<CastorClass> classes) throws ClassNotFoundException {

    StringBuilder migratedClassesList = new StringBuilder();

    System.out.println("Start to migrate " + classes.size() + " classes to JAXB");
    for (CastorClass castorClass : classes) {

      String clazzName = castorClass.getName();
      JaxbCodeGenerator jaxbCodeGenerator = JaxbCodeGenerator.newInstance(getAbsoluteJavaClassFilePath(clazzName));
      
      Class<?> javaClazz = Class.forName(clazzName);

      String xmlRootElementName = castorClass.getMappedXmlRootElementName();
      if (StringUtils.isEmpty(xmlRootElementName)) {
        xmlRootElementName = javaClazz.getSimpleName();
      }
      if (!hasAnnotation(javaClazz, XmlRootElement.class)) {
        jaxbCodeGenerator.addXmlRootElementAnnotation(javaClazz, xmlRootElementName);
        jaxbCodeGenerator.addImport(JaxbCodeGenerator.XML_ROOTELEMENT_IMPORT);
      }

      List<CastorClassField> fields = castorClass.getField();

      Set<CastorBindXmlNodeType> xmlMappingImportsToAdd = new HashSet<>(3);

      Set<Method> annotatedGetMethods = new HashSet<>(fields.size());
      List<String> fieldNamesOrdered = new ArrayList<String>();
      for (CastorClassField field : fields) {

        CastorBindXml bindXml = field.getBindXml();
        String fieldXmlName = bindXml != null ? bindXml.getName() : field.getName();
        CastorBindXmlNodeType xmlNodeType = CastorBindXml.mapToNodeType(bindXml);
        
        Method getMethod = retrieveGetMethodForField(javaClazz, field);
        if (getMethod == null) {
          continue;
        }
        if (hasExplicitGetMethod(field) && getMethodStartsWithIterate(field)) {
          // Special case: Need to rewrite iterateXXX method to getXXX method and let it
          // return a list instead of an Iterable (In out code base we typically have
          // those getXXX methods already in place):
          getMethod = findGetMethodByName(javaClazz, computeJavaBeanPropertyName(field));
          assertNotNull(getMethod, "Expected to find getter for " + field.getName());
          if (!hasAnnotation(getMethod, XmlAttribute.class, XmlElement.class)) {
            jaxbCodeGenerator.rewriteMethodFromIterableToList(getMethod);
            jaxbCodeGenerator.addXmlMappingAnnotation(xmlNodeType, fieldXmlName, getMethod);
            xmlMappingImportsToAdd.add(xmlNodeType);
          }
        } else { // Default case
          if (!hasAnnotation(getMethod, XmlAttribute.class, XmlElement.class)) {
            jaxbCodeGenerator.addXmlMappingAnnotation(xmlNodeType, fieldXmlName, getMethod);
            xmlMappingImportsToAdd.add(xmlNodeType);
          }
        }
        
        annotatedGetMethods.add(getMethod);
        fieldNamesOrdered.add(getFieldNameNormalized(field));
      }

      if (xmlMappingImportsToAdd.contains(CastorBindXmlNodeType.ATTRIBUTE)) {
        jaxbCodeGenerator.addImport(JaxbCodeGenerator.XML_ATTRIBUTE_IMPORT);
      }
      if (xmlMappingImportsToAdd.contains(CastorBindXmlNodeType.ELEMENT)) {
        jaxbCodeGenerator.addImport(JaxbCodeGenerator.XML_ELEMENT_IMPORT);
      }

      if (!hasDefaultConstructorForJaxb(javaClazz)) {
        jaxbCodeGenerator.generateDefaultConstructor(javaClazz);
      }

      if (addXmlTypeOrdering && !hasAnnotation(javaClazz, XmlType.class)) {
        jaxbCodeGenerator.addXmlTypeOrderAnnotation(javaClazz, fieldNamesOrdered);
        jaxbCodeGenerator.addImport(JaxbCodeGenerator.XML_TYPE_IMPORT);
      }

      List<Method> transientMethods = computeXmlTransientMethods(javaClazz, annotatedGetMethods);
      if (!transientMethods.isEmpty()) {
        jaxbCodeGenerator.addImport(JaxbCodeGenerator.XML_TRANSIENT_IMPORT);
      }
      jaxbCodeGenerator.addXmlTransientAnnotations(transientMethods);

      jaxbCodeGenerator.saveToFileAndClose();
      System.out.println("Migrated class " + javaClazz.getName());

      migratedClassesList.append(javaClazz.getSimpleName()).append(".class").append(", ");
    }

    System.out.println("Finished migration");
    System.out.println("Top Level Classes List which can be used when creating JAXBContext");
    System.out.println(StringUtils.removeEnd(migratedClassesList.toString(), ", "));
  }

  private String getFieldNameNormalized(CastorClassField field) {

    String result = field.getName();
    if (StringUtils.startsWith(result, "oUID")) { // We have weird mapping like "oUIDItemOption" which is however in
                                                  // Java-Code denoted as OUIDItemOption...
      result = StringUtils.capitalize(result);
    }
    return result;
  }

  private static boolean hasDefaultConstructorForJaxb(Class<?> javaClazz) {

    try {
      Constructor<?> constructor = javaClazz.getDeclaredConstructor();
      return constructor != null && //
          (Modifier.isProtected(constructor.getModifiers()) || Modifier.isPublic(constructor.getModifiers()));
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      // NOP (is thrown when no constructor is found)
    }
    return false;
  }

  private static List<Method> computeXmlTransientMethods(Class<?> javaClazz, Set<Method> annotatedGetMethods) {
    List<Method> result = new ArrayList<Method>();
    Set<String> alreadyAddedMethodNames = new HashSet<String>();
    List<Method> publicMethods = getPublicMethods(javaClazz);
    for (Method publicMethod : publicMethods) {
      if (annotatedGetMethods.contains(publicMethod) || //
          (!publicMethod.getName().startsWith("get") && !publicMethod.getName().startsWith("is")) || //
          publicMethod.getParameterCount() > 0 || //
          Modifier.isStatic(publicMethod.getModifiers())) {
        continue;
      }
      if (!hasAnnotation(publicMethod, XmlTransient.class)
          && !alreadyAddedMethodNames.contains(publicMethod.getName())) {
        result.add(publicMethod);
        alreadyAddedMethodNames.add(publicMethod.getName());
      }
    }
    return result;
  }

  @SafeVarargs
  private static boolean hasAnnotation(AnnotatedElement methodOrClass, Class<? extends Annotation>... annotations) {
    
    for (Class<? extends Annotation> annotation : annotations) {
      if (methodOrClass.isAnnotationPresent(annotation)) {
        return true;
      }
    }
    return false;
  }

  private static Method retrieveGetMethodForField(Class<?> javaClazz, CastorClassField field) {

    String expectedMethodNameToFind = field.getGetMethod();
    if (!hasExplicitGetMethod(field)) {
      expectedMethodNameToFind = computeJavaBeanPropertyName(field);
    }
    Method result = findGetMethodByName(javaClazz, expectedMethodNameToFind);
    if (result == null) { // Sometimes it might be "is" (for booleans)
      result = findGetMethodByName(javaClazz, computeJavaBeanPropertyName(field, "is"));
    }
    return result;
  }

  private static Method findGetMethodByName(Class<?> javaClazz, String name) {
    List<Method> publicMethods = getPublicMethods(javaClazz);
    for (Method publicMethod : publicMethods) {
      if (publicMethod.getName().equals(name)) {
        return publicMethod;
      }
    }
    // This may occur when dealing with inheritance or when there is just a wrong
    // castor mapping (there is denoted a field mapping, but it does not exist in
    // code)
    return null;
  }

  private static String computeJavaBeanPropertyName(CastorClassField field) {
    return computeJavaBeanPropertyName(field, "get");
  }

  private static String computeJavaBeanPropertyName(CastorClassField field, String methodPrefix) {
    return methodPrefix + StringUtils.capitalize(field.getName());
  }


  private static boolean hasExplicitGetMethod(CastorClassField field) {
    return StringUtils.isNotEmpty(field.getGetMethod());
  }

  private static boolean getMethodStartsWithIterate(CastorClassField field) {
    return StringUtils.startsWith(field.getGetMethod(), "iterate");
  }

  private static List<Method> getPublicMethods(Class<?> javaClazz) {

    Method[] allMethods = javaClazz.getDeclaredMethods();
    return Arrays.stream(allMethods) //
        .filter(m -> Modifier.isPublic(m.getModifiers())) //
        .collect(Collectors.toList());
  }

  private CastorRootMapping loadMapping(String inputFile)
      throws JAXBException, FileNotFoundException, ParserConfigurationException, SAXException {

    SAXSource inputSource = getXmlSourceWithoutDtdValidation(inputFile);

    JAXBContext context = JAXBContext.newInstance(CastorRootMapping.class);
    Unmarshaller mar = context.createUnmarshaller();
    return (CastorRootMapping) mar.unmarshal(inputSource);
  }

  private static SAXSource getXmlSourceWithoutDtdValidation(String inputFile)
      throws SAXException, FileNotFoundException, ParserConfigurationException {

    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    spf.setFeature("http://xml.org/sax/features/validation", false);

    XMLReader xmlReader = spf.newSAXParser().getXMLReader();
    InputSource inputSource = new InputSource(new FileReader(ROOT_PROJECT_PATH + "/" + inputFile));
    SAXSource source = new SAXSource(xmlReader, inputSource);
    return source;
  }
  
  private String getAbsoluteJavaClassFilePath(String clazzNameFullqualified) {

    String clazzFilePath = clazzNameFullqualified.replaceAll("\\.", "/");
    return ROOT_PROJECT_PATH + "/" + javaSrcDirRelative + "/" + clazzFilePath + ".java";
  }

  private static void assertNotNull(Object object, String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    }
  }

}

