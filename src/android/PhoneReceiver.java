package com.tongdatech.callshow;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


import com.tongdatech.phonedemo.R;



import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PhoneReceiver extends BroadcastReceiver {


  //每次事件都会new一个对象出来
  private static boolean outgoing = false;
  private static View phoneView;
  private static long startTime;

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
        String msg = outgoing ? "去电挂断电话" : "来电挂断电话";
        Log.e("TAG", msg + outgoing);
        hidden(context);

        executor.execute(() -> {
          try {

            for (int i = 0; i < 10; i++) {
              Thread.sleep(100);
              if (readCallLog(context, incomeNumber)) break;
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
    LayoutInflater inflate = LayoutInflater.from(context);
    WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
    params.type = WindowManager.LayoutParams.TYPE_PHONE;
    params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
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

  private void hidden(Context context) {
    WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    if (phoneView != null) wm.removeView(phoneView);
    //释放
    phoneView = null;
  }


  private boolean readCallLog(Context context, String number) {
    boolean result = false;
    ContentResolver cr = context.getContentResolver();
    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
    if (ContextCompat.checkSelfPermission(context,
      Manifest.permission.READ_CALL_LOG)
      != PackageManager.PERMISSION_GRANTED) {
      Log.e("TAG", "需要权限2:读取通话记录");
      final Intent intent = new Intent("callEndErr");
      Bundle b = new Bundle();
      b.putString( "msg", "读取通话记录" );
      intent.putExtras(b);
      localBroadcastManager.sendBroadcast(intent);
      return false;

    }

    final Cursor cursor = cr.query(
      CallLog.Calls.CONTENT_URI,
      new String[]{CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.DURATION},
      CallLog.Calls.NUMBER + "=?",
      new String[]{number},
      CallLog.Calls.DATE + " desc");

    //取第一条数据
    if (cursor.moveToNext()) {
      long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
      if (Math.abs(date - startTime) > 5 * 1000) {
        Log.e("TAG", "date:" + date + " startTime:" + startTime);
        return false;
      }

      long durationTime = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
      //1接入 2打出 3未接
      int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
      Log.e("TAG", "date:" + date + " durationTime:" + durationTime + " type:" + type);
      final Intent intent = new Intent("callEnd");
      Bundle b = new Bundle();
      b.putString( "evtNumber", number );
      b.putLong( "evtDate", date );
      b.putLong( "evtDurationTime", durationTime );
      b.putInt( "evtType", type );
      intent.putExtras(b);
      localBroadcastManager.sendBroadcast(intent);

      return true;

    }
    return result;
  }


};
