package com.dktechub.apkextractor2;

import android.content.ContentResolver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SavedAsynkLoader extends AsyncTask<Void,Void,ArrayList<App>> {

    OnLoadCompleteListener onLoadCompleteListener;
    ContentResolver contentResolver;
    PackageManager packageManager;

    public SavedAsynkLoader(OnLoadCompleteListener onLoadCompleteListener, ContentResolver contentResolver,PackageManager packageManager) {
        this.onLoadCompleteListener = onLoadCompleteListener;
        this.contentResolver=contentResolver;
        this.packageManager=packageManager;
    }


    public ArrayList<App> getAllVideos() {
        ArrayList<App> apps = new ArrayList<>();
        Uri uri;
        String dataString;
        String mimeType;
        String[] selectionArgs;
        String selectionQuery;
        uri = MediaStore.Files.getContentUri("external");
        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("apk");
        selectionArgs = new String[]{ mimeType };
        selectionQuery = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        dataString=MediaStore.Files.FileColumns.DATA;
        try (Cursor cursor = contentResolver.query(uri, null, selectionQuery, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String data = cursor.getString(cursor.getColumnIndex(dataString));
                    File f = new File(data);
                    String name = f.getName();
                    if(!f.isFile())
                        continue;
                    Drawable ic = null;
                    try {
                        PackageInfo ai = packageManager.getPackageArchiveInfo(f.getAbsolutePath(), 0);
                        ai.applicationInfo.sourceDir = data;
                        ai.applicationInfo.publicSourceDir = data;
                     ic =   ai.applicationInfo.loadIcon(packageManager);
                     //name = (String) ai.applicationInfo.loadLabel(packageManager);
                    }catch (Exception e)
                    {

                    }
                    Log.d("Apps",data);

                    apps.add(new App(name,data,ic,f.length()));
                } while (cursor.moveToNext());
            }
        }
        Collections.sort(apps,new Comparator());
        return apps;
    }





    @Override
    protected ArrayList<App> doInBackground(Void... voids) {
        return getAllVideos();

    }

    @Override
    protected void onPostExecute(ArrayList<App> apps) {
        super.onPostExecute(apps);
        onLoadCompleteListener.onLoaded(apps);
    }

    public interface OnLoadCompleteListener {
        void onLoaded(List<App> all);

    }

    public static class Comparator implements java.util.Comparator<App> {
        @Override
        public int compare(App o1, App o2) {
            return o1.name.compareToIgnoreCase(o2.name);
        }
    }


}

