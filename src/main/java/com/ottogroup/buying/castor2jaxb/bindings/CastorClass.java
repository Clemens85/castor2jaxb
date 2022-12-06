package com.ottogroup.buying.castor2jaxb.bindings;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class CastorClass {

  private String name;

  private boolean verifyConstructable;

  private CastorMapTo mapTo;

  private List<CastorClassField> field;

  @XmlAttribute
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlAttribute(name = "verify-constructable")
  public boolean isVerifyConstructable() {
    return verifyConstructable;
  }

  public void setVerifyConstructable(boolean verifyConstructable) {
    this.verifyConstructable = verifyConstructable;
  }

  @XmlElement(name = "map-to")
  public CastorMapTo getMapTo() {
    return mapTo;
  }

  public void setMapTo(CastorMapTo mapTo) {
    this.mapTo = mapTo;
  }

  @XmlTransient
  public String getMappedXmlRootElementName() {
    return mapTo != null ? mapTo.getXml() : null;
  }

  @XmlElement
  public List<CastorClassField> getField() {
    return field;
  }

  public void setField(List<CastorClassField> field) {
    this.field = field;
  }

  @Override
  public String toString() {
    return name + ": " + mapTo.toString() + ": " + field.toString();
  }

}
