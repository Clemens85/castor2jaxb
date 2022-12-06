package com.ottogroup.buying.fixture;

public class MetaData {

  private String userLanguage;

  private Integer qualityLevelCode;

  public MetaData(String userLanguage, Integer qualityLevelCode) {
    this.userLanguage = userLanguage;
    this.qualityLevelCode = qualityLevelCode;
  }

  public String getUserLanguage() {
    return userLanguage;
  }

  public void setUserLanguage(String userLanguage) {
    this.userLanguage = userLanguage;
  }

  public Integer getQualityLevelCode() {
    return qualityLevelCode;
  }

  public void setQualityLevelCode(Integer qualityLevelCode) {
    this.qualityLevelCode = qualityLevelCode;
  }

}
