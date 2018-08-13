package com.tongdatech.callshow;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;


import com.tongdatech.phonedemo.R;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PhoneReceiver extends BroadcastReceiver {


  //每次事件都会new一个对象出来
  private static boolean outgoing = false;
  private static View phoneView;
  private static long startTime;
  private static long endTime;

  private static Executor executor = Executors.newCachedThreadPool();

  @Override
  public void onReceive(Context context, Intent intent) {

    final String incomeNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);


    if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
      startTime = System.currentTimeMillis();
      outgoing = true;
      Log.e("TAG", "去电" + outgoing);
      String phoneNumber = intent.getStringExtra(intent.EXTRA_PHONE_NUMBER);
      show(context, phoneNumber, outgoing);
    } else {


      String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
      if (TelephonyManager.EXTRA_STATE_RINGING.equals(phoneState)) {
        startTime = System.currentTimeMillis();
        outgoing = false;
        Log.e("TAG", "拨入电话,铃声响起");
        // show(context, incomeNumber, outgoing);
      } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(phoneState)) {
        String msg = outgoing ? "去电通话中" : "来电通话中";
        Log.e("TAG", msg + outgoing);
//        if(outgoing)show(context,incomeNumber,outgoing);

      } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(phoneState)) {
        endTime = System.currentTimeMillis();
        String msg = outgoing ? "去电挂断电话" : "来电挂断电话";
        Log.e("TAG", msg + outgoing);
        final String text=hidden(context);

        executor.execute(() -> {
          try {

            for (int i = 0; i < 10; i++) {
              Thread.sleep(100);
              if (readCallLog(context, incomeNumber,text)) break;
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (Exception e) {
            e.printStackTrace();
          }
        });

      } else {
        Log.e("TAG", "其他" + phoneState);
      }
    }


  }

  private void show(Context context, String incomeNumber, boolean outgoingFlag) {

    boolean flag;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      flag = Settings.canDrawOverlays(context);
      Log.e("TAG", "权限canDrawOverlays:" + flag);
    } else {
      int ret = ContextCompat.checkSelfPermission(context, Manifest.permission.SYSTEM_ALERT_WINDOW);
      Log.e("TAG", "权限系统弹窗:" + ret);
      flag = ret
        == PackageManager.PERMISSION_GRANTED;

    }
    if (!flag) {
      Log.e("TAG", "需要权限2:系统弹窗");
      return;
    }


    LayoutInflater inflate = LayoutInflater.from(context);
    WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    } else {
      params.type = WindowManager.LayoutParams.TYPE_PHONE;
    }

    params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
    //| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    params.gravity = Gravity.TOP;


    DisplayMetrics outMetrics = new DisplayMetrics();
    wm.getDefaultDisplay().getMetrics(outMetrics);
    int width2 = outMetrics.widthPixels;
    int height2 = outMetrics.heightPixels;
    params.width = WindowManager.LayoutParams.MATCH_PARENT;
    params.height = height2 / 2;
    params.format = PixelFormat.RGBA_8888;
    phoneView = inflate.inflate(R.layout.phone_alert, null);
    wm.addView(phoneView, params);


    DataStorage dataStorage = DataStorage.getInstance();
    ShowData showdata = dataStorage.getShowData();
    if (showdata != null) {
      TextView tv1 = phoneView.findViewById(R.id.tv_pa_name);
      if (incomeNumber != null && incomeNumber.equals(showdata.getPhoneNumber())) {

        tv1.setText(showdata.getName());
        TextView tv2 = phoneView.findViewById(R.id.tv_pa_number);
        tv2.setText(showdata.getNumber());
        TextView tv3 = phoneView.findViewById(R.id.tv_pa_f1);
        tv3.setText(showdata.getLine1());
        TextView tv4 = phoneView.findViewById(R.id.tv_pa_f2);
        tv4.setText(showdata.getLine2());

      } else {
        tv1.setText(incomeNumber);
        Log.e("TAG", "号码与数据不匹配");
      }

    } else Log.e("TAG", "显示数据为空");

  }

  private String hidden(Context context) {
    String text="";
    WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    if (phoneView != null) {
      wm.removeView(phoneView);
      EditText editText=phoneView.findViewById(R.id.tv_pa_et1);
      text = editText.getText().toString();
      Log.d("TAG", "text:"+text);
    }
    //释放
    phoneView = null;
    return text;
  }


  private boolean readCallLog(Context context, String number, String text) {
    boolean result = false;
    ContentResolver cr = context.getContentResolver();
    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
    if (ContextCompat.checkSelfPermission(context,
      Manifest.permission.READ_CALL_LOG)
      != PackageManager.PERMISSION_GRANTED) {
      Log.e("TAG", "需要权限2:读取通话记录");
      final Intent intent = new Intent("callEndErr");
      Bundle b = new Bundle();
      b.putString("msg", "读取通话记录");
      intent.putExtras(b);
      localBroadcastManager.sendBroadcast(intent);
      return false;

    }

    final Cursor cursor = cr.query(
      CallLog.Calls.CONTENT_URI,
      new String[]{CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.LAST_MODIFIED},
      CallLog.Calls.NUMBER + "=?",
      new String[]{number},
      CallLog.Calls.DATE + " desc");

    //取第一条数据
    if (cursor.moveToNext()) {
      long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));

      long durationTime = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
      //4.2以后可用
      long lastModified = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.LAST_MODIFIED));

      long activeTime=lastModified-durationTime*1000;
      //date有些手机为拨出时间 有些为接通时间 与两个时间做比较 差别都较大的为不可信
      //小米手机是拨出时间 华为是接通时间
      if (Math.abs(date - startTime) > 3 * 1000
        && Math.abs(activeTime - date)> 3 * 1000) {
        Log.e("TAG", "date:" + date + " startTime:" + startTime+" activeTime:"+activeTime);
        return false;
      }

      //1接入 2打出 3未接
      int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
      Log.e("TAG", "date:" + date + " durationTime:" + durationTime + " type:" + type);
      final Intent intent = new Intent("callEnd");
      Bundle b = new Bundle();
      b.putString("evtNumber", number);
      b.putLong("evtDate", date);
      b.putLong("evtDurationTime", durationTime);
      b.putInt("evtType", type);
      b.putLong("evtEndDate", endTime);
      b.putLong("evtLastModified", lastModified);
      b.putLong("evtActiveDate", activeTime);
      b.putString("evtText",text);
      intent.putExtras(b);
      localBroadcastManager.sendBroadcast(intent);

      return true;

    }
    return result;
  }


};
