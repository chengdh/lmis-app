package com.lmis.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lmis.MainActivity;
import com.lmis.R;
import com.lmis.support.LmisDialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by chunsoft on 15/12/2.
 */
public class UpdateManager {
    private static final String URL_VERSION_XML = "http://114.115.130.233:9090/assets/update_yujiusuyun_app.xml";
    //下载中
    private static final int DOWNLOAD = 1;
    //下载结束
    private static final int DOWNLOAD_FINISH = 2;
    //保存解析的XML
    HashMap<String, String> mHashMap;
    //下载保存路径
    private String mSavePath;
    // 记录进度条数量
    private int progress;
    //是否取消更新
    private boolean cancelUpdate = false;

    private Context mContext;
    //更新进度条
    private ProgressBar mProgress;
    private Dialog mDownloadDialog;
    private InputStream mInputStream = null;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 正在下载
                case DOWNLOAD:
                    // 设置进度条位置
//                    mProgress.setProgress(progress);
                    break;
                case DOWNLOAD_FINISH:
                    // 安装文件
                    installAPK();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    public UpdateManager(Context context) {
        this.mContext = context;
        RetrieveVersionTask task = new RetrieveVersionTask();
        task.execute((Void) null);
    }

    /**
     * 检测软件更新
     */
    public void checkUpdate() {
        if (isUpdate()) {
            // 显示提示对话框
            showNoticeDialog();
        } else {
            Toast.makeText(mContext, R.string.update_no, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 检查软件是否有更新版本
     */
    private boolean isUpdate() {
        // 获取当前软件版本
        int versionCode = getVersionCode(mContext);
        // 把version.xml放到网络上，然后获取文件信息
        //InputStream inStream = ParseXmlService.class.getClassLoader().getResourceAsStream("version.xml");
        // 解析XML文件。 由于XML文件比较小，因此使用DOM方式进行解析

        ParseXmlService service = new ParseXmlService();
        try {
            mHashMap = service.parseXml(mInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != mHashMap) {
            int serviceCode = Integer.valueOf(mHashMap.get("version"));
            // 版本判断
            if (serviceCode > versionCode) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取软件版本号
     */
    private int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {
        //构造对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.update_title);
        builder.setMessage(R.string.update_info);

        //更新
        builder.setPositiveButton(R.string.update_update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //显示下载对话框
                        showDownloadDialog();
                    }
                }
        );
        //稍后更新
        builder.setNegativeButton(R.string.update_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        //构建软件下载对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        builder.setTitle(R.string.update_updating);
//        //给对话框添加进度条
//        final LayoutInflater inflater = LayoutInflater.from(mContext);
//        View v = inflater.inflate(R.layout.update_progress, null);
//        mProgress = (ProgressBar) v.findViewById(R.id.pb_update);
//        builder.setView(v);
//        builder.setNegativeButton(R.string.update_cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                //设置取消状态
//                cancelUpdate = true;
//            }
//        });
//        mDownloadDialog = builder.create();
        if (mDownloadDialog != null) {
            mDownloadDialog.dismiss();
        }
        mDownloadDialog = new LmisDialog(mContext, false, "正在更新...");
        mDownloadDialog.show();
        //下载文件
        downloadAPK();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mDownloadDialog.dismiss();
        }
    };

    /**
     * 下载APK文件
     */
    private void downloadAPK() {

        new DownloadAPKThread().start();
    }

    /**
     * 下载文件线程
     */
    private class DownloadAPKThread extends Thread {
        @Override
        public void run() {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //获取存储卡的路径
                String sdpath = Environment.getExternalStorageDirectory() + "/";
                mSavePath = sdpath + "download";
                try {
                    URL url = new URL(mHashMap.get("url"));
                    //创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    //获取文件大小
                    int length = conn.getContentLength();
                    //创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    //判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, mHashMap.get("name"));
                    FileOutputStream fos = new FileOutputStream(apkFile);

                    int count = 0;
                    byte buf[] = new byte[1024];
                    //写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        //计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        //更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0) {
                            //下载完成
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        //写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);
                    fos.close();
                    is.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            handler.sendEmptyMessage(0);
//            mDownloadDialog.dismiss();
        }
    }

    /**
     * 安装APK文件
     */
    private void installAPK() {
        File apkfile = new File(mSavePath, mHashMap.get("name"));
        if (!apkfile.exists()) {
            return;
        }
        //通过Intent安装APK文件
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private class RetrieveVersionTask extends AsyncTask<Void, Void, Boolean> {

        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL(URL_VERSION_XML);
                URLConnection urlConnection = url.openConnection();
                mInputStream = new BufferedInputStream(urlConnection.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
                mInputStream = null;
                return false;
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                checkUpdate();
            }
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }
}
