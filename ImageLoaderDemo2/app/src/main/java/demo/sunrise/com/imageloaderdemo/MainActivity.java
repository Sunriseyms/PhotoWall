package demo.sunrise.com.imageloaderdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.ImageView;

import demo.sunrise.com.imageloaderdemo.utils.BitmapUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Sunrise";

    private GridView mPhotoWall;

    private PhotoWallAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(checkPermission(Manifest.permission.INTERNET, Process.myPid(), Process.myUid()) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},10);
            return;
        }
        initView();
    }

    private void initView() {
        mPhotoWall = findViewById(R.id.photo_wall);
        adapter = new PhotoWallAdapter(this,0,Images.imageThumbUrls,mPhotoWall);
        mPhotoWall.setAdapter(adapter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cancelAllTasks();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 10){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                initView();
            }else {
                finish();
            }
        }
    }
}
