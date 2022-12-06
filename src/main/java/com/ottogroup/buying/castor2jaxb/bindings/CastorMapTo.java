package com.ottogroup.buying.castor2jaxb.bindings;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "map-to")
public class CastorMapTo {

  private String xml;

  @XmlAttribute
  public String getXml() {
    return xml;
  }

  public void setXml(String xml) {
    this.xml = xml;
  }

  @Override
  public String toString() {
    return xml;
  }

}
