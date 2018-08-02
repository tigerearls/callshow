package com.tongdatech.callshow;

public class DataStorage {

  private static class Holder {

    private static final DataStorage INSTANCE = new DataStorage();

  }



  private DataStorage (){}

  public static final DataStorage getInstance() {

    return Holder.INSTANCE;

  }

  private ShowData showData;

  public ShowData getShowData() {
    return showData;
  }

  public void setShowData(ShowData showData) {
    this.showData = showData;
  }
}
