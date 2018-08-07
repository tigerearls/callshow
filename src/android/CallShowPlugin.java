package com.tongdatech.callshow;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CallShowPlugin extends CordovaPlugin {

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("add")) {

      DataStorage dataStorage = DataStorage.getInstance();
      try {
        JSONObject obj = args.getJSONObject(0);
        ShowData data = new ShowData();
        data.setPhoneNumber(obj.getString("phoneNumber"));
        data.setName(obj.getString("name"));
        data.setNumber(obj.getString("number"));
        data.setLine1(obj.getString("line1"));
        data.setLine2(obj.getString("line2"));
        dataStorage.setShowData(data);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "OK"));
      } catch (Exception e) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
      }

    }
    if (action.equals("get")) {

      DataStorage dataStorage = DataStorage.getInstance();
      try {
        ShowData data = dataStorage.getShowData();
        JSONObject json=new JSONObject();
        json.put("phoneNumber",data.getPhoneNumber());
        json.put("name",data.getName());
        json.put("number",data.getNumber());
        json.put("line1",data.getLine1());
        json.put("line2",data.getLine2());
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
      } catch (Exception e) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
      }

    }
    return super.execute(action, args, callbackContext);
  }
}
