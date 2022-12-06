// package com.ottogroup.buying.castor2jaxb;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
//
// import java.io.File;
// import java.io.IOException;
// import java.io.Reader;
// import java.io.StringReader;
// import java.net.URI;
// import java.net.URISyntaxException;
// import java.net.URL;
//
// import javax.xml.bind.JAXBContext;
// import javax.xml.bind.JAXBElement;
// import javax.xml.bind.JAXBException;
// import javax.xml.bind.Unmarshaller;
// import javax.xml.transform.stream.StreamSource;
//
// import org.apache.commons.io.FileUtils;
// import org.junit.jupiter.api.Test;
//
// import com.ottogroup.buying.result.ImportData;
//
/// **
// * Can be used after migration test case has been executed for checking deserializtaion of XML
// file
// *
// *
// */
// class DeserializationTest {
//
// @Test
// void testXmlDeserializationOfMigratedClasses()
// throws JAXBException, IOException, URISyntaxException {
//
// // Test deserialization with JAXB by using example XML file:
// ImportData result = deserializeFromXmlFile(getFileFromClasspath("/example.xml"),
// ImportData.class);
// assertNotNull(result.getMetaData());
// assertNotNull(result.getItemData());
//
// assertEquals("EN", result.getMetaData().getUserLanguage());
// assertEquals(10, result.getMetaData().getQualityLevelCode());
//
// assertEquals(2, result.getItemData().getItems().size());
// assertEquals("Art-1", result.getItemData().getItems().get(0).getItemNumber());
// assertEquals("Art-2", result.getItemData().getItems().get(1).getItemNumber());
// }
//
// private <T> T deserializeFromXmlFile(File file, Class<T> clazz)
// throws JAXBException, IOException {
// JAXBContext jaxbInstance = JAXBContext.newInstance(clazz);
// String xmlContent = FileUtils.readFileToString(file, "UTF-8");
// try (Reader xmlInputReader = new StringReader(xmlContent)) {
// Unmarshaller mar = jaxbInstance.createUnmarshaller();
// JAXBElement<T> wrappedResult = mar.unmarshal(new StreamSource(xmlInputReader), clazz);
// return wrappedResult.getValue();
// }
// }
//
// private File getFileFromClasspath(String fileFromClasspath) throws URISyntaxException {
// URL resource = this.getClass().getResource(fileFromClasspath);
// URI uri = resource.toURI();
// return new File(uri);
// }
// }
