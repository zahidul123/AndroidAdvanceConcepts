package app.jomorder.freehandimagecropping;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public class CropActivity extends Activity {

    ImageView compositeImageView;
    boolean crop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            crop = extras.getBoolean("crop");
        }
        int widthOfscreen = 0;
        int heightOfScreen = 0;

        DisplayMetrics dm = new DisplayMetrics();
        try {
            getWindowManager().getDefaultDisplay().getMetrics(dm);
        } catch (Exception ex) {
        }
        widthOfscreen = dm.widthPixels;
        heightOfScreen = dm.heightPixels;

        compositeImageView = (ImageView) findViewById(R.id.imagegview_crop_image);




        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),
                R.drawable.humming_bird);

        //set aspect ratio
        int imageHeightRatio=(widthOfscreen * bitmap2.getHeight()) / bitmap2.getWidth();
        int imageWidthRatio=(heightOfScreen * bitmap2.getWidth()) / bitmap2.getHeight();

        Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap2,  imageWidthRatio, imageHeightRatio, true);

       /* Bitmap resultingImage = Bitmap.createBitmap(widthOfscreen,
                heightOfScreen, bitmap2.getConfig());*/

        Bitmap resultingImage = Bitmap.createBitmap(widthOfscreen,
                heightOfScreen, scaledBitmap.getConfig());

        Canvas canvas = new Canvas(resultingImage);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Path path = new Path();
        for (int i = 0; i < SomeView.points.size(); i++) {
            path.lineTo(SomeView.points.get(i).x, SomeView.points.get(i).y);
        }
        canvas.drawPath(path, paint);
        if (crop) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        } else {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        }
        canvas.drawBitmap(bitmap2, 0, 0, paint);
        compositeImageView.setImageBitmap(resultingImage);
    }
}