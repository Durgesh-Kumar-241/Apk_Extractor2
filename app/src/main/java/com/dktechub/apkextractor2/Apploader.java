package com.dktechub.apkextractor2;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Apploader extends AsyncTask<Void,Void,ArrayList<App>> {
    Context context;
    private final OnLoadCompleteListener onLoadCompleteListener;
    public Apploader (Context context,OnLoadCompleteListener onLoadCompleteListener)
    {
        this.context=context;
        this.onLoadCompleteListener=onLoadCompleteListener;
    }
    @Override
    protected ArrayList<App> doInBackground(Void... voids) {
        return loadApps(true);
    }
    public ArrayList<App> loadApps(boolean showSystem)
    {
        ArrayList<App> loaded = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(0);
        for(ApplicationInfo temp:apps)
        {   if(showSystem||(temp.flags&ApplicationInfo.FLAG_SYSTEM)==0)
            {
                String appName = (String) packageManager.getApplicationLabel(temp);
                String packageName = temp.packageName;
                try{
                    PackageInfo packageInfo = packageManager.getPackageInfo(packageName,0);
                    String versionName = packageInfo.versionName;
                    String version = String.valueOf(packageInfo.versionCode);
                    appName = appName+'_'+versionName+'_'+version;
                }catch (Exception ignored)
                {
                }
                loaded.add(new App(appName +".apk", temp.sourceDir, temp.loadIcon(packageManager), new File(temp.sourceDir).length()));

            }

        }

        Collections.sort(loaded,new SavedAsynkLoader.Comparator());
        return loaded;
    }

    @Override
    protected void onPostExecute(ArrayList<App> apps) {
        super.onPostExecute(apps);
        onLoadCompleteListener.onLoadedItems(apps);
    }

    public interface OnLoadCompleteListener
    {
        void onLoadedItems(ArrayList<App> apps);
    }


}
