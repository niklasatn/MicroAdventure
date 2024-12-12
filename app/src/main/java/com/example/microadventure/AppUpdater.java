package com.example.microadventure;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

public class AppUpdater extends AppCompatActivity {

    private final String fileUrl;
    private final int appVersionCode;
    private final OkHttpClient client;

    public AppUpdater(String fileUrl, int appVersionCode) {
        this.fileUrl = fileUrl;
        this.appVersionCode = appVersionCode;
        this.client = new OkHttpClient();
    }

    // Methode, um den Versionscode aus dem Dateinamen zu extrahieren
    private int extractVersionCodeFromFileName(String fileName) {
        try {
            String versionPart = fileName.replaceAll(".*_v(\\d+)\\.apk", "$1");
            return Integer.parseInt(versionPart);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Methode, um zu prüfen, ob eine neue Version verfügbar ist
    public boolean isNewVersionAvailable() {
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            int fileVersionCode = extractVersionCodeFromFileName(fileName);

            if (fileVersionCode > 0) {
                return fileVersionCode > appVersionCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean downloadFileToApksFolder(Context context) {
        Request request = new Request.Builder()
                .url(fileUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {

                File apksDir = new File(context.getFilesDir(), "APKs");
                if (!apksDir.exists()) {
                    apksDir.mkdir();
                }

                String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
                File outputFile = new File(apksDir, fileName);

                try (InputStream inputStream = response.body().byteStream();
                     FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
