package com.ottogroup.buying.castor2jaxb.bindings;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "field")
public class CastorClassField {

  private String name;

  private String collection;

  private String setMethod;

  private String getMethod;

  private CastorBindXml bindXml;

  @XmlAttribute(required = true)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlAttribute(required = false)
  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  @XmlAttribute(name = "set-method", required = false)
  public String getSetMethod() {
    return setMethod;
  }

  public void setSetMethod(String setMmethod) {
    this.setMethod = setMmethod;
  }

  @XmlAttribute(name = "get-method", required = false)
  public String getGetMethod() {
    return getMethod;
  }

  public void setGetMethod(String getMethod) {
    this.getMethod = getMethod;
  }

  @XmlElement(name = "bind-xml")
  public CastorBindXml getBindXml() {
    return bindXml;
  }

  public void setBindXml(CastorBindXml bindXml) {
    this.bindXml = bindXml;
  }

  @Override
  public String toString() {
    return name + ": " + bindXml.toString();
  }

}