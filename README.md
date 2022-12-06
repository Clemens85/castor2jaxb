Code for automatically migrating existing Java classes that were bound to XML by [Castor XML bindings](https://castor.exolab.org/) to JAXB annotations.

More details can be found in this [article](XXX).

tl;dr: Just copy the main code from com.ottogroup.buying.castor2jaxb into your project and execute the CastorToJaxbMigrator (ensure that the needed dependencies are also setup in the project -> pom.xml)


### About the JUnit Tests

* Castor2JaxbMigratorTest can just be executed to perform a simple example migration. Most code in the test is just about copying Java classes around which is needed due to the migration adapts Java class files inplace.
* Just for demo purpose, it is also possible to comment-in the DeserializationTest after migration, for executing a demo XML deserialization of the migrated classes.

