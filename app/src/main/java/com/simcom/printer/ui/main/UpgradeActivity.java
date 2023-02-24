package com.simcom.printer.ui.main;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.simcom.printer.PrintApplication;
import com.simcom.printer.R;
import com.simcom.printer.poscommand.PrintCMD;
import com.simcom.printer.utils.PrintUtil;
import com.simcom.printer.utils.UpgradeCon;
import com.simcom.printerlib.utils.DataUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class UpgradeActivity extends AppCompatActivity {

    private static final String TAG = "UpgradeActivity";

    private static final int CHOOSE_FILE_CODE = 1000;

    private byte[][] bytes = new byte[256][192];

    private Button upgrade, file;

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);
        upgrade = findViewById(R.id.upgrade_btn);
        file = findViewById(R.id.file_btn);
        mContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        file.setOnClickListener(v -> {
            chooseFile();
        });

        upgrade.setOnClickListener(v -> {
            Thread thread = new Thread(runnable);
            thread.start();
        });
    }

//    @SuppressLint("NewApi")
//    public void sendData(byte[] buffer) {
//        byte[] receiveBuf = new byte[64];
//        if (myDeviceConnection == null)
//            return;
//        int res = myDeviceConnection.bulkTransfer(epBulkOut, buffer, buffer.length, 0);
//        Log.d("Sent", res + "");
//        if (res >= 0) {
//            try {
//                Thread.sleep(50);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            int readRes = myDeviceConnection.bulkTransfer(epBulkIn, receiveBuf, 64, 50);
//            Log.d("Read", readRes + ": " + byteToHexStr(receiveBuf[0]));
//            Log.d("Received: ", byteToHexStr(receiveBuf[0]));
//        } else {
//            Log.d("Send", "send failed");
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CHOOSE_FILE_CODE) {
                Uri uri = data.getData();
                byte[] bs = readFile(uriToFileApiQ(uri, this));
                if (bs.length > 0) {
                    file.setText("文件读取成功");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static File uriToFileApiQ(Uri uri, Context context) {
        File file = null;
        if (uri == null)
            return null;
        //android10以上转换
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            String displayName = System.currentTimeMillis() + Math.round((Math.random() + 1) * 1000)
                    + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));

            try {
                InputStream is = contentResolver.openInputStream(uri);
                File cache = new File(context.getCacheDir().getAbsolutePath(), displayName);
                FileOutputStream fos = new FileOutputStream(cache);
                FileUtils.copy(is, fos);
                file = cache;
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static byte[] readFile(File f) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int)f.length());
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 100000;
            byte[] buffer = new byte[buf_size];
            boolean var6 = false;

            int len;
            while(-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }

            return bos.toByteArray();
        } catch (IOException var7) {
            var7.printStackTrace();
            return null;
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                byte[] res;
//                PrintApplication.getInstance().getPrinterPort().sendMsg(PrintCMD.requestUpdateFW());
//                Thread.sleep(5000);
//                PrintApplication.getInstance().init();
//                PrintApplication.getInstance().getPrinterPort().readMsg();
                PrintApplication.getInstance().getPrinterPort().sendMsg(PrintCMD.getFirstFrame());
                PrintApplication.getInstance().getPrinterPort().readMsg();

                UpgradeCon upgradeCon = new UpgradeCon();
                upgradeCon.go(DataUtils.readFileFromAssets(mContext, null,
                        "s05_1.2.1(1).bin"));           // s05_1.2.1.bin
                Log.d("SEND SIZE", upgradeCon.packages + "");
                for (int i = 0; i <= upgradeCon.packages; i++) {
                    Log.d("Send Index", i + "");
                    PrintApplication.getInstance().getPrinterPort().sendMsg(upgradeCon.getBs()[i]);
                    Thread.sleep(50);
                    res = PrintApplication.getInstance().getPrinterPort().readMsg();
                    Log.d(TAG, "index: " + i  + " " + PrintUtil.byteToHexStr(res[0]));
                    Thread.sleep(50);
                }
                Thread.sleep(200);
                PrintApplication.getInstance().getPrinterPort().sendMsg(PrintCMD.getEndFrame());
                Thread.sleep(50);
                res = PrintApplication.getInstance().getPrinterPort().readMsg();
                Log.d(TAG, "end res: " + PrintUtil.byteToHexStr(res[0]));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    private void handleBytes(byte[] bytes) {

    }

    private void reset() {
        // 升级失败，重置变量

    }

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public String byteToHexStr(byte b) {
        StringBuilder sb = new StringBuilder();
        sb.append(hexCode[(b >> 4) & 0xF]);
        sb.append(hexCode[(b) & 0xF]);
        return sb.toString();
    }

    // 调用系统文件管理器，选择文件
    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Choose File"),
                    CHOOSE_FILE_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "亲，木有文件管理器啊-_-!!", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private byte[] getBytes(String path) {
        byte[] bytes = new byte[0];
        try {
            bytes = Files.readAllBytes(new File(path).toPath());
        } catch (IOException | OutOfMemoryError | SecurityException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}