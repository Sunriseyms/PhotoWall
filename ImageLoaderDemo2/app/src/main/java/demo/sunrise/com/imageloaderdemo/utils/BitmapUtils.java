package demo.sunrise.com.imageloaderdemo.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by sunrise on 3/1/18.
 */

public class BitmapUtils {

    public static int calculateSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round(((float) height / (float) reqHeight));
            final int widthRatio = Math.round(((float) width / (float) reqWidth));

            inSampleSize = heightRatio > widthRatio ? widthRatio : heightRatio;
        }
        return inSampleSize;
    }


    public static Bitmap decodeSampledBitmapFromResource(Resources res,int resId,int reqWidth,int
            reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res,resId,options);

        options.inSampleSize = calculateSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res,resId,options);
    }
}
