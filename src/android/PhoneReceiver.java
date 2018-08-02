package com.tongdatech.callshow;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.tongdatech.callshow.R;

public class PhoneReceiver extends BroadcastReceiver {


  //每次事件都会new一个对象出来
  private static boolean outgoing=false;
  private static View phoneView;
  @Override
  public void onReceive(Context context, Intent intent) {

    String incomeNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);


    if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
      outgoing=true;
      Log.e("TAG", "去电"+outgoing);
      incomeNumber=intent.getStringExtra(intent.EXTRA_PHONE_NUMBER);
      show(context,incomeNumber,outgoing);
    }else{


      String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
      if (TelephonyManager.EXTRA_STATE_RINGING.equals(phoneState)) {
        outgoing=false;
        Log.e("TAG", "拨入电话,铃声响起");
        show(context,incomeNumber,outgoing);
      } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(phoneState)) {
        String msg=outgoing?"去电通话中":"来电通话中";
        Log.e("TAG", msg+outgoing);
//        if(outgoing)show(context,incomeNumber,outgoing);

      } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(phoneState)) {
        String msg=outgoing?"去电挂断电话":"来电挂断电话";
        Log.e("TAG", msg+outgoing);
        hidden(context);
      } else {
        Log.e("TAG", "其他"+phoneState);
      }
    }


  }

  private void show(Context context,String incomeNumber,boolean outgoingFlag){
    LayoutInflater inflate = LayoutInflater.from(context);
    WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
    params.type = WindowManager.LayoutParams.TYPE_PHONE;
    params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    params.gravity = Gravity.TOP;
//    Display display = wm.getDefaultDisplay();

    DisplayMetrics outMetrics = new DisplayMetrics();
    wm.getDefaultDisplay().getMetrics(outMetrics);
    int width2 = outMetrics.widthPixels;
    int height2 = outMetrics.heightPixels;
    params.width = WindowManager.LayoutParams.MATCH_PARENT;
    params.height = height2/2;
    params.format = PixelFormat.RGBA_8888;
    phoneView = inflate.inflate(R.layout.phone_alert, null);
    wm.addView(phoneView, params);


    DataStorage dataStorage = DataStorage.getInstance();
    ShowData showdata=dataStorage.getShowData();
    if(showdata!=null){
      TextView tv1=phoneView.findViewById(R.id.tv_pa_name);
      if(incomeNumber!=null&&incomeNumber.equals(showdata.getPhoneNumber())){

        tv1.setText(showdata.getName());
        TextView tv2=phoneView.findViewById(R.id.tv_pa_number);
        tv2.setText(showdata.getNumber());
        TextView tv3=phoneView.findViewById(R.id.tv_pa_f1);
        tv3.setText(showdata.getLine1());
        TextView tv4=phoneView.findViewById(R.id.tv_pa_f2);
        tv4.setText(showdata.getLine2());

      }else {
        tv1.setText(incomeNumber);
        Log.e("TAG", "号码与数据不匹配");
      }

    }else Log.e("TAG", "显示数据为空");




  }

  private void hidden(Context context){
    WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    if(phoneView!=null)wm.removeView(phoneView);
    //释放
    phoneView=null;
  }


//  private PhoneStateListener outgoingListener = new PhoneStateListener() {
//    @Override
//    public void onCallStateChanged(int state, final String incomingNumber) {
//      // TODO Auto-generated method stub
//      //state 当前状态 incomingNumber,貌似没有去电的API
//      super.onCallStateChanged(state, incomingNumber);
//      switch (state) {
//        case TelephonyManager.CALL_STATE_IDLE:
//          Log.e("TAG", "去电挂断");
//          if (phoneView != null) wm.removeView(phoneView);
//          break;
//        case TelephonyManager.CALL_STATE_OFFHOOK:
//          Log.e("TAG", "去电接听");
//          if (phoneView != null) wm.removeView(phoneView);
//          break;
//        case TelephonyManager.CALL_STATE_RINGING:
//          Log.e("TAG", "去电响铃");
//          inflate = LayoutInflater.from(mcontext);
//          wm = (WindowManager) mcontext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//          WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//          params.type = WindowManager.LayoutParams.TYPE_PHONE;
//          params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//          params.gravity = Gravity.CENTER;
//          params.width = WindowManager.LayoutParams.MATCH_PARENT;
//          params.height = 600;
//          params.format = PixelFormat.RGBA_8888;
//          phoneView = inflate.inflate(R.layout.phone_alert, null);
//          wm.addView(phoneView, params);
//          Log.e("TAG", "响铃:去电号码" + incomingNumber);
//          Log.e("TAG", "响铃:======" + Thread.currentThread().getName());
//          //输出来电号码
//          break;
//      }
//    }
//  };
//
//  private PhoneStateListener listener = new PhoneStateListener() {
//
//    @Override
//    public void onCallStateChanged(int state, final String incomingNumber) {
//      // TODO Auto-generated method stub
//      //state 当前状态 incomingNumber,貌似没有去电的API
//      super.onCallStateChanged(state, incomingNumber);
//      switch (state) {
//        case TelephonyManager.CALL_STATE_IDLE:
//          Log.e("TAG", "来电挂断");
//          if (phoneView != null) wm.removeView(phoneView);
//          break;
//        case TelephonyManager.CALL_STATE_OFFHOOK:
//          Log.e("TAG", "来电接听");
//          if (phoneView != null) wm.removeView(phoneView);
//          break;
//        case TelephonyManager.CALL_STATE_RINGING:
//          Log.e("TAG", "来电响铃");
//          inflate = LayoutInflater.from(mcontext);
//          wm = (WindowManager) mcontext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//          WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//          params.type = WindowManager.LayoutParams.TYPE_PHONE;
//          params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//          params.gravity = Gravity.CENTER;
//          params.width = WindowManager.LayoutParams.MATCH_PARENT;
//          params.height = 600;
//          params.format = PixelFormat.RGBA_8888;
//          phoneView = inflate.inflate(R.layout.phone_alert, null);
//          wm.addView(phoneView, params);
//          Log.e("TAG", "响铃:来电号码" + incomingNumber);
//          Log.e("TAG", "响铃:======" + Thread.currentThread().getName());
//          //输出来电号码
//          break;
//      }
//    }
//  };
};
