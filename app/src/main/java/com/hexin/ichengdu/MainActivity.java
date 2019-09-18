package com.hexin.ichengdu;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.ghca.MobelWlan.LoginOut;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import wendu.dsbridge.DWebView;
import wendu.dsbridge.OnReturnValue;

public class  MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    public static  MainActivity mainActivity;
    DWebView dWebView;
    View errorView;
    Button reloadBtn;
    Boolean loadError = false;
    public final static int REQUEST_CODE_INFO_OF_PHONE_SETTINGS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainActivity = this;
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        this.requestPermission();
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_main);
        StatusBarUtil.setStatusBarMode(this, true, R.color.c_ffffff);
        errorView = findViewById(R.id.errorView);
        reloadBtn = findViewById(R.id.reloadBtn);
        dWebView = findViewById(R.id.h5WebView);
        dWebView.getSettings().setTextZoom(100);
        dWebView.addJavascriptObject(new JsApi(), null);
        dWebView.loadUrl("https://xtintera.ichengdu.com/");
        WebSettings settings = dWebView.getSettings();
        int screenDensity = getResources().getDisplayMetrics().densityDpi;
        WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM;
        //设置缓存模式
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
//        switch (screenDensity) {
//            case DisplayMetrics.DENSITY_LOW:
//                zoomDensity = WebSettings.ZoomDensity.CLOSE;
//                break;
//            case DisplayMetrics.DENSITY_MEDIUM:
//                zoomDensity = WebSettings.ZoomDensity.MEDIUM;
//                break;
//            case DisplayMetrics.DENSITY_HIGH:
//                zoomDensity = WebSettings.ZoomDensity.FAR;
//                break;
//        }
//        settings.setDefaultZoom(zoomDensity);
        errorView.setVisibility(View.GONE);
        DWebView.setWebContentsDebuggingEnabled(true);
//        new LoginOut().UserLoginOut(this, new Handler(){
//            @Override
//            public void handleMessage(@NonNull Message msg) {
//                super.handleMessage(msg);
//                System.out.println("log out log out");
//                System.out.println(msg.what);
//            }
//        });
        dWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                dWebView.setVisibility(View.GONE);
                errorView.setVisibility(View.VISIBLE);
                loadError = true;
                System.out.println(request.getUrl());
                System.out.println("***********error********");
            }


            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
//                dWebView.setVisibility(View.GONE);
//                errorView.setVisibility(View.VISIBLE);
//                loadError = true;
                System.out.println(request.getUrl());
                System.out.println("********http error**********");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(!loadError) {
                    errorView.setVisibility(View.GONE);
                    dWebView.setVisibility(View.VISIBLE);
                    dWebView.callHandler("invokeH5GetWifi", new OnReturnValue<String>() {
                        @Override
                        public void onValue(String retValue) {
                            System.out.println(retValue);
                        }
                    });
                }

            }

        });
        reloadBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(Util.isFastClick()) {
                    loadError = false;
                    dWebView.reload();
                }
            }
        });

    }

    protected  void requestPermission() {
        String[] perms = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // 已经申请过权限，做想做的事

        } else {
            // 没有申请过权限，现在去申请
            EasyPermissions.requestPermissions(this, "需要申请权限",0 ,perms);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
            dWebView.callHandler("invokeH5GetWifi", new OnReturnValue<String>() {
                @Override
                public void onValue(String retValue) {
                }
            });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (dWebView.canGoBack()) {
                dWebView.goBack();//返回上一浏览页面
                return true;
            } else if(event.getRepeatCount() == 0){
                dialog();
            }
        }
        return false;
    }
    @Override
    public void onBackPressed() {
        // super.onBackPressed(); 	不要调用父类的方法
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode){
            case 0:
                Toast.makeText(this, "已获取WRITE_EXTERNAL_STORAGE权限", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(this, "已获取WRITE_EXTERNAL_STORAGE和WRITE_EXTERNAL_STORAGE权限", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        //处理权限名字字符串
        StringBuffer sb = new StringBuffer();
        for (String str : perms){
            sb.append(str);
            sb.append("\n");
        }
        sb.replace(sb.length() - 2,sb.length(),"");
        switch (requestCode){
            case 0:
                Toast.makeText(this, "已拒绝权限" + perms.get(0), Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(this, "已拒绝WRITE_EXTERNAL_STORAGE和WRITE_EXTERNAL_STORAGE权限"+ perms.get(0), Toast.LENGTH_SHORT).show();
                break;
        }
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Toast.makeText(this, "已拒绝权限" + sb + "并不再询问" , Toast.LENGTH_SHORT).show();
            new AppSettingsDialog
                    .Builder(this)
                    .setRationale("此功能需要" + sb + "权限，否则无法正常使用，是否打开设置")
                    .setPositiveButton("是")
                    .setNegativeButton("否")
                    .build()
                    .show();
        }
    }
    protected  void  dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("确认退出？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
