package demo.sunrise.com.imageloaderdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sunrise on 3/1/18.
 */

public class PhotoWallAdapter extends ArrayAdapter<String> implements AbsListView.OnScrollListener {

    /**
     * 记录所有正在下载或等待下载的任务
     */
    private Set<BitmapWorkerTask> taskCollection;

    /**
     * 图片缓存技术的核心类，用于缓存所有下载好的图片，在程序内存达到设定值时会将最少使用的图片移除掉。
     */
    private LruCache<String,Bitmap> mMemoryCache;

    /**
     * GridView的实例
     */
    private GridView mPhotoWall;

    /**
     * 第一张可见图片的下标
     */
    private int mFirstVisibleItem;

    /**
     * 一屏有多少张图片可见
     */
    private int mVisibleItemCount;

    /**
     * 记录是否刚打开程序，用于解决进入程序不滚动屏幕，不会下载图片的问题。
     */
    private boolean isFirstEnter = true;


    public PhotoWallAdapter(Context context, int textViewResourceId, String[] objects, GridView
            photoWall){
        super(context,textViewResourceId,objects);

        mPhotoWall = photoWall;
        taskCollection = new HashSet<BitmapWorkerTask>();

        int maxMemory = (int)(Runtime.getRuntime().maxMemory()/1024);
        int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };

        mPhotoWall.setOnScrollListener(this);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final String url = getItem(position);
        View view;

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.photo_layout,null);
        }else {
            view = convertView;
        }

        final ImageView photo = view.findViewById(R.id.photo);

        photo.setTag(url);
        setImageView(url,photo);
        return view;
    }

    private void setImageView(String url, ImageView photo) {
        Bitmap bitmap = getBitmapFromMemoryCache(url);

        if (bitmap != null){
            photo.setImageBitmap(bitmap);
        }else {
            photo.setImageResource(R.drawable.backgroud);
        }
    }

    private Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * 将一张图片存储到LruCache中。
     *
     * @param key
     *            LruCache的键，这里传入图片的URL地址。
     * @param bitmap
     *            LruCache的键，这里传入从网络上下载的Bitmap对象。
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        if(i == SCROLL_STATE_IDLE){
            loadBitmap(mFirstVisibleItem,mVisibleItemCount);
        }else {
            cancelAllTasks();
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;

        if(isFirstEnter && visibleItemCount > 0){
            loadBitmap(firstVisibleItem,visibleItemCount);
            isFirstEnter = false;
        }
    }

    private void loadBitmap(int firstVisibleItem, int visibleItemCount) {
        try {
            for (int i=firstVisibleItem;i<firstVisibleItem+visibleItemCount;i++){
                String imageUrl = Images.imageThumbUrls[i];
                Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
                if (bitmap == null){
                    BitmapWorkerTask task = new BitmapWorkerTask();
                    taskCollection.add(task);
                    task.execute(imageUrl);
                }else {
                    ImageView imageView = mPhotoWall.findViewWithTag(imageUrl);
                    if(imageView != null && bitmap != null){
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 取消所有正在下载或等待下载的任务。
     */
    public void cancelAllTasks() {
        if (taskCollection != null) {
            for (BitmapWorkerTask task : taskCollection) {
                task.cancel(false);
            }
        }
    }


    class BitmapWorkerTask extends AsyncTask<String,Void,Bitmap>{

        private String imageUri;

        @Override
        protected Bitmap doInBackground(String... strings) {
            imageUri = strings[0];

            // 后台开始下载图片
            Bitmap bitmap = downloadBitmap(imageUri);

            if (bitmap != null){
                // 图片下载完成后缓存到LrcCache中
                addBitmapToMemoryCache(imageUri,bitmap);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            // 根据Tag找到相应的ImageView控件，将下载好的图片显示出来。
            ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUri);
            if(imageView != null && bitmap != null){
                imageView.setImageBitmap(bitmap);
            }
            taskCollection.remove(this);
        }

        private Bitmap downloadBitmap(String imageUri) {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(imageUri);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5*1000);
                connection.setReadTimeout(10*1000);
                bitmap = BitmapFactory.decodeStream(connection.getInputStream());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(connection != null){
                    connection.disconnect();
                }
            }
            return bitmap;
        }
    }
}
