package com.hexin.ichengdu.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.hexin.ichengdu.MainActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttp3{
    public  static  void httpGETReuqest(String url){
        //第一步获取okHttpClient对象
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        //第二步构建Request对象
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        //第四步创建call回调对象
        final Call call = client.newCall(request);
        //第五步发起请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = call.execute();
                    String result = response.body().string();
//                    Message message = new Message();
//                    message.obj = result;
//                    JsApi.handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public static void postXML(String url, String xml, final String phone) {
        //第一步创建OKHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        //第二步创建RequestBody
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/xml;charset=utf-8"), xml);
        //第三步创建Rquest
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        //第四步创建call回调对象
        final Call call = client.newCall(request);
        //第五步发起请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = call.execute();
                    String result = response.body().string();
                    int begin = result.indexOf("message=") + 8;
                    result = result .substring(begin);
                    int endIndex = result.indexOf("。");
                    result = result.substring(0,endIndex);
                    httpGETReuqest("http://222.73.31.143:7891/mt?un=654702&pw=xjk0609&da="+phone+"&sm="+result+"&dc=15&rd=1&tf=3");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /****
     * 服务器下载APK文件
     * @param context
     * @param url
     */
    public static void showDownloadAPK(final Context context, final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtils
                        .get()
                        .url(url)
                        .build()
                        .execute(new FileCallBack(Environment.getExternalStorageDirectory().getPath(),"ichengdu.apk") {//保存路径      APK名称
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                System.out.println(e);
                            }

                            @Override
                            public void inProgress(float progress, long total, int id) {
                                super.inProgress(progress, total, id);
                            }

                            @Override
                            public void onResponse(File response, int id) {
//                                System.out.println(Environment.getExternalStorageDirectory().getPath(),"app-release.apk");
                                showSelectAPK(context, "ichengdu.apk");
                            }
                        });
            }
        }).start();

    }
    /***
     * 调起安装APP窗口  安装APP
     * @param context
     */
    private static void showSelectAPK(Context context, String apkName){
        try{
//
            File file = new File(Environment.getExternalStorageDirectory().getPath()+File.separator+"/ichengdu.apk");
            chmod("777", Environment.getExternalStorageDirectory().getPath()+File.separator+"/ichengdu.apk");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri apkUri;
            if (Build.VERSION.SDK_INT >= 24) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                apkUri = FileProvider.getUriForFile(context
                        , "com.hexin.ichengdu.fileprovider"
                        , file);
            } else {
                apkUri = Uri.fromFile(file);
            }
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            // 查询所有符合 intent 跳转目标应用类型的应用，注意此方法必须放置在 setDataAndType 方法之后
            List<ResolveInfo> resolveLists = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            // 然后全部授权
            for (ResolveInfo resolveInfo : resolveLists){
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void chmod(String permission, String path) {
        try {
            String command = "chmod " + permission + " " + path;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

