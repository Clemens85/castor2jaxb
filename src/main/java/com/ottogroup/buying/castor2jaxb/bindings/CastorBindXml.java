package com.ottogroup.buying.castor2jaxb.bindings;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

@XmlRootElement(name = "bind-xml")
public class CastorBindXml {

  private String name;

  private String node;

  private String type;

  @XmlAttribute
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlAttribute
  public String getNode() {
    return node;
  }

  public void setNode(String node) {
    this.node = node;
  }

  @XmlAttribute
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public static CastorBindXmlNodeType mapToNodeType(CastorBindXml castorBindXml) {
    if (castorBindXml == null) {
      return CastorBindXmlNodeType.ATTRIBUTE; // Seems to be the default (for primitive types ... which should only be
                                              // used in our case)
    }
    if (StringUtils.isEmpty(castorBindXml.getNode())) {
      return CastorBindXmlNodeType.ELEMENT; // Seems to be default when node is not given
    }
    return CastorBindXmlNodeType.valueOf(StringUtils.toRootUpperCase(castorBindXml.getNode()));
  }

  @Override
  public String toString() {
    return name + " - " + node + " => " + type;
  }

}
