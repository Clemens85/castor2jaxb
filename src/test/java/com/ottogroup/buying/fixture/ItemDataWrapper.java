package com.ottogroup.buying.fixture;

import java.util.Iterator;
import java.util.List;

public class ItemDataWrapper {

  private MetaData metaData;
  
  private List<Item> items;

  public MetaData getMetaData() {
    return metaData;
  }

  public void setMetaData(MetaData metaData) {
    this.metaData = metaData;
  }

  public Iterable<Item> getItems() {
    return items;
  }

  public Iterator<Item> iterateItems() {
    return items.iterator();
  }

  public void setItems(List<Item> items) {
    this.items = items;
  }

}
