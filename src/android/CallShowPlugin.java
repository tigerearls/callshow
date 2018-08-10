package com.tongdatech.callshow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.RestrictionsManager.ACTION_REQUEST_PERMISSION;

public class CallShowPlugin extends CordovaPlugin {
  private static final int REQUEST_CODE = 1;
  private CallbackContext permissionsCallback;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    cordova.setActivityResultCallback(this);
  }


  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (requestCode == REQUEST_CODE) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (Settings.canDrawOverlays(this.cordova.getActivity())) {
          Log.e("TAG", "onActivityResult granted");
        }
      }
    }
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
        JSONObject json = new JSONObject();
        json.put("phoneNumber", data.getPhoneNumber());
        json.put("name", data.getName());
        json.put("number", data.getNumber());
        json.put("line1", data.getLine1());
        json.put("line2", data.getLine2());
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
      } catch (Exception e) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
      }

    }
    if (action.equals("ACTION_MANAGE_OVERLAY_PERMISSION")) {
      try {
        reqWindowPerms(args, callbackContext);
      } catch (Exception e) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
      }

    }
    if (action.equals("CHECK_SYSTEM_ALERT_WINDOW")) {
      try {
        boolean flag = checkWindowPerms();
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, flag));
      } catch (Exception e) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
      }

    }
    return super.execute(action, args, callbackContext);
  }

  private void reqWindowPerms(JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      callbackContext.success("OK");
    } else {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
      String packageName = args.getString(0);

      if (packageName != null && !"".equals(packageName))
        intent.setData(Uri.parse("package:" + packageName));
      this.cordova.getActivity().startActivityForResult(intent, REQUEST_CODE);
      callbackContext.success("OK");
    }
  }

  private boolean checkWindowPerms() {
    boolean flag;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      flag = Settings.canDrawOverlays(this.cordova.getActivity());

    } else {
      flag = ContextCompat.checkSelfPermission(this.cordova.getActivity(), Manifest.permission.SYSTEM_ALERT_WINDOW)
        == PackageManager.PERMISSION_GRANTED;

    }
    return flag;
  }
//  @Override
//  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
//    if (permissionsCallback == null) {
//      return;
//    }
//    if (permissions != null && permissions.length > 0) {
//      boolean flag=checkWindowPerms();
//      permissionsCallback.success(String.valueOf(flag));
//    } else {
//      permissionsCallback.error("false");
//    }
//    permissionsCallback = null;
//  }
}
