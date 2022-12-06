package com.ottogroup.buying.fixture;

public class ImportData {

  private MetaData metaData;

  private ItemDataWrapper itemData;

  public MetaData getMetaData() {
    return metaData;
  }

  public void setMetaData(MetaData metaData) {
    this.metaData = metaData;
  }

  public ItemDataWrapper getItemData() {
    return itemData;
  }

  public void setItemData(ItemDataWrapper itemData) {
    this.itemData = itemData;
  }

  public boolean isMetaDataSet() {
    return metaData != null;
  }
}
