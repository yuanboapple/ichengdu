package com.hexin.ichengdu;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.webkit.JavascriptInterface;
import androidx.annotation.NonNull;

import com.ghca.MobelWlan.CheckNetwork;
import com.ghca.MobelWlan.Login;
import com.ghca.MobelWlan.LoginOut;
import com.hexin.ichengdu.Utils.OkHttp3;

import org.json.JSONException;
import org.json.JSONObject;

import wendu.dsbridge.CompletionHandler;

public class JsApi{
   static String send;
//    public static Handler handler = new Handler(){
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//            send = (String) msg.obj;
//        }
//    };
   Context context = MyApplication.getContext();
   final private String appPkg = "com.hexin.ichengdu";
    /*
    *@desc 获取网络连接状态
     */
    @JavascriptInterface
    public void getNetInfo_Asyn(Object msg, final CompletionHandler<JSONObject>  handler) throws JSONException {
        new CheckNetwork(context,new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                JSONObject jsonObject = new JSONObject();
                try {
//                    jsonObject.put("code", 5);
//                    jsonObject.put("msg", "");
                    jsonObject.put("code", msg.what);
                    System.out.println("************"+msg.what+"************");
                    if (msg.what == 7) {
                        jsonObject.put("msg", Util.getWiFiName(context));
                    } else if (msg.what == 4) {
                        jsonObject.put("msg", "wifi未连接");
                    } else if (msg.what == 5) {
                        jsonObject.put("msg", "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                handler.complete(jsonObject);
            }
        });

    }
    /*
     *@desc 连接wifi
     */
    @JavascriptInterface
    public  void linkWifi_Asyn(Object obj, CompletionHandler<Integer> handler) {
       MainActivity.mainActivity.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
        handler.complete(1);
    }
    /*
     *@desc 登陆
     */
    @JavascriptInterface
    public  void login_Async(Object obj, final CompletionHandler<JSONObject> handler) throws JSONException {
        JSONObject jsonObject = new JSONObject(obj.toString());
        final String loginname = jsonObject.getString("loginname");
        final String pwd  = jsonObject.getString("Pwd");
        new CheckNetwork(context,new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                System.out.println("登陆时网络状态："+msg.what);
                if(msg.what == 5) {
                    Login login = new Login();
                    System.out.println(loginname);
                    System.out.println(pwd);
                    login.userlogin(loginname, pwd, context, new Handler(){
                        @Override
                        public void handleMessage(@NonNull Message msg) {
                            super.handleMessage(msg);
                            System.out.println("ssssssssss");
                            System.out.println(msg.what);
                            JSONObject jsonObj = new JSONObject();
                        try {
                            if(msg.what == 1){
                                jsonObj.put("code", msg.what);
                                jsonObj.put("msg", "登陆成功");
                            } else if(msg.what == -1) {
                                jsonObj.put("code", msg.what);
                                jsonObj.put("msg", "登陆失败");
                            } else if(msg.what == -2) {
                                jsonObj.put("code", msg.what);
                                jsonObj.put("msg", "网络超时");
                            }
                         } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        handler.complete(jsonObj);

                        }
                    });
                }
            }
        });
    }
    /*
     *@desc 登出
     */
    @JavascriptInterface
    public void loginOut_Async(Object obj, final CompletionHandler<Integer> handler) throws  JSONException {
        LoginOut loginOut = new LoginOut();
        loginOut.UserLoginOut(context, new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                int result = msg.what;
                handler.complete(result);
            }
        });
    }
    /*
     *@desc 获取版本号
     */
    @JavascriptInterface
    public void getVersionCode(Object obj, CompletionHandler<String> handler) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            handler.complete(info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }
    /*
     *@desc 去更新
     */
    @JavascriptInterface
    public void updateVerision(Object obj, CompletionHandler<Integer> handler){
//        Uri uri = Uri.parse("https://hzgsyy.ichengdu.com/");
//        MainActivity.mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
//        handler.complete(1);
        try {
            OkHttp3.showDownloadAPK(context, "https://xtinterc.ichengdu.com/pack/ichengdu.apk");
            handler.complete(1);
        } catch (Exception e) {
            handler.complete(-1);
            e.printStackTrace();
        }
    }
    /*
    *@发送验证码
     */
    @JavascriptInterface
    public void sendMS(Object obj, CompletionHandler<Integer> handler){
        try {
            JSONObject jsonObject = new JSONObject(obj.toString());
            final String phone = jsonObject.getString("phone");
             OkHttp3.postXML("http://authcont.ichengdu.com:9090/SmartTownInterface/Online/queryCurrsession",
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:onl=\"http://localhost:8080/interface/Online\">\n" +
                            "<soapenv:Header/>\n" +
                            "<soapenv:Body>\n" +
                            "<onl:queryPassword>\n" +
                            "<onl:in0><![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<Query><PhoneNo>"+phone+"</PhoneNo></Query>]]></onl:in0>\n" +
                            "</onl:queryPassword>\n" +
                            "</soapenv:Body>\n" +
                            "</soapenv:Envelope>", phone);
                handler.complete(1);
        } catch (JSONException e) {
            e.printStackTrace();
            handler.complete(-1);
        }
    }

}
