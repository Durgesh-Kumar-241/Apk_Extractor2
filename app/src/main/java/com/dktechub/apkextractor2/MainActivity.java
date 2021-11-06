package com.dktechub.apkextractor2;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    RecyclerView stored,installed;
    com.google.android.material.button.MaterialButtonToggleGroup buttonToggleGroup;
    TextView empty;
    AppAdapter adapter,dapter2;
    ProgressBar progressBar;
    boolean installedMode = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        installed = findViewById(R.id.installed);
        stored=findViewById(R.id.stored);
        empty=findViewById(R.id.empty);
        buttonToggleGroup = findViewById(R.id.toggle);
        progressBar=findViewById(R.id.pbar);
        adapter =  new AppAdapter(this::showMoreOptions);
        installed.setAdapter(adapter);
        installed.setLayoutManager(new LinearLayoutManager(this));
        installed.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        dapter2 = new AppAdapter(this::showMoreOptions);
        stored.setAdapter(dapter2);
        stored.setLayoutManager(new LinearLayoutManager(this));
        stored.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        buttonToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if(isChecked)
            {
                showInstalled(checkedId==R.id.apps);
                installedMode=checkedId==R.id.apps;
                Toast.makeText(this, checkedId==R.id.apps?"Installed Apps":"Saved Apks", Toast.LENGTH_SHORT).show();
            }
        });
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M||checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            loadApps();
        }else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED)
        { loadApps(); }
        else
        Snackbar.make(this,findViewById(R.id.main),"Storage permission is necessary to work this app properly",Snackbar.LENGTH_INDEFINITE)
                .setAction("Grant", v -> requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100)).show();
    }

    public void loadApps()
    {
        new Apploader(this, apps -> {
            adapter.apps.addAll(apps);
            progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            buttonToggleGroup.check(R.id.apps);

        }).execute();

        new SavedAsynkLoader(all -> {
            dapter2.apps.addAll(all);
            progressBar.setVisibility(View.GONE);
            dapter2.notifyDataSetChanged();
        }, getContentResolver(),getPackageManager()).execute();


    }

    public void showInstalled(boolean yes)
    {
        if(yes) {
        stored.setVisibility(View.GONE);
        if(adapter.getItemCount()!=0)
        {
            installed.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
        }else empty.setVisibility(View.VISIBLE);
        }
        else {
            installed.setVisibility(View.GONE);
            if(dapter2.getItemCount()!=0)
            {
                stored.setVisibility(View.VISIBLE);
                empty.setVisibility(View.GONE);
            }else {
                empty.setVisibility(View.VISIBLE);
            }
        }
    }

    public void save(App app)
    {
        new Szr(app, savedP -> Toast.makeText(getApplicationContext(), "saved to " + savedP, Toast.LENGTH_SHORT).show()).execute();
    }


    public void showMoreOptions(App app)
    {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.layout_more);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();

        TextView share = bottomSheetDialog.findViewById(R.id.share);
        TextView save = bottomSheetDialog.findViewById(R.id.save);

        if(save!=null)
        {
            if(app.pathTobinary.contains(Environment.getExternalStorageDirectory().getAbsolutePath()))
            {
                save.setText("Delete");
                save.setOnClickListener(v -> {
                    delete(app);
                    bottomSheetDialog.dismiss();
                });
            }else {
                save.setOnClickListener(view -> {
                    save(app);
                    bottomSheetDialog.dismiss();
                });
            }
        }
           
        if(share!=null)
        {
            share.setOnClickListener(view -> {
                share(app);
                bottomSheetDialog.dismiss();
            });
        }

        


    }
    
    public void delete(App app)
    {
        Snackbar.make(this,findViewById(R.id.main),"Delete?",Snackbar.LENGTH_INDEFINITE).setAction("Yes", v -> {
            if(new File(app.pathTobinary).delete())
                Toast.makeText(getApplicationContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
        }).show();
        
    }

    public void share(App app)
    {
        new Szr(app, savedP -> {
            try {

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("*/*");
                Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", new File(savedP));
                //Log.d("Invite", uri.toString());
                i.putExtra(Intent.EXTRA_STREAM, uri);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(i, "Choose where to send"));

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }




    public class Szr extends AsyncTask<Void,Void,String> {
        App app;
        OnSaved onSaved;
        public Szr(App app,OnSaved onSaved)
        {   this.onSaved=onSaved;
            this.app=app;
        }
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Saving...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Apk Extractor/");
            String dest=f.getAbsolutePath()+"/"+app.name;
            f.mkdirs();
            byte[] buffer = new byte[512];
            try{
                FileInputStream is = new FileInputStream(app.pathTobinary);
                FileOutputStream os = new FileOutputStream(dest);
                int read;
                while ((read=is.read(buffer))>0)
                {
                    os.write(buffer,0,read);
                }
                os.flush();
                os.close();
                is.close();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            addToGallery(new File(dest));
            return dest;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            onSaved.onSaved(s);
            progressDialog.dismiss();
        }
        private void addToGallery(File f)
        {
            Intent m = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(f);
            m.setData(contentUri);
            MainActivity.this.sendBroadcast(m);
        }
    }

    public interface OnSaved{
        void onSaved(String savedP);
    }


}