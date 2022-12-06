package com.ottogroup.buying.castor2jaxb.bindings;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "mapping")
public class CastorRootMapping {

  private List<CastorClass> classes;

  @XmlElement(name = "class")
  public List<CastorClass> getClasses() {
    return classes;
  }

  public void setClasses(List<CastorClass> classes) {
    this.classes = classes;
  }

  @Override
  public String toString() {
    return classes.toString();
  }

}
