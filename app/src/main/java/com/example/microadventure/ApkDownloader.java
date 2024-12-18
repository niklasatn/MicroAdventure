package com.example.microadventure;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApkDownloader extends AsyncTask<String, Void, Boolean> {

    private static Context context;

    public ApkDownloader(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String downloadUrl = params[0];
        File outputFile = new File(context.getFilesDir(), "MicroAdventure.apk");

        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }

            fileOutputStream.close();
            inputStream.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getApkVersion(String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_META_DATA);
        if (packageInfo != null) {
            return packageInfo.versionName;
        }
        return null;
    }

    public static String getCurrentAppVersion() {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isNewVersionAvailable(String apkVersion, String currentAppVersion) {
        if (apkVersion != null && currentAppVersion != null) {
            return compareVersions(apkVersion, currentAppVersion) > 0;
        }
        return false;
    }

    private static int compareVersions(String version1, String version2) {
        String[] v1Parts = version1.split("\\.");
        String[] v2Parts = version2.split("\\.");

        int length = Math.max(v1Parts.length, v2Parts.length);

        for (int i = 0; i < length; i++) {
            int v1Part = (i < v1Parts.length) ? Integer.parseInt(v1Parts[i]) : 0;
            int v2Part = (i < v2Parts.length) ? Integer.parseInt(v2Parts[i]) : 0;

            if (v1Part > v2Part) {
                return 1;
            } else if (v1Part < v2Part) {
                return -1;
            }
        }

        return 0;  // Gleich
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success) {
            Toast.makeText(context, "Neue Version heruntergeladen", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "App Up-To-Date", Toast.LENGTH_SHORT).show();
        }
    }
}
