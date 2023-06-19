package com.example.mydocsapp.apputils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;

import com.example.mydocsapp.services.AppService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageService {
    public static String fileToBase64(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            byte[] fileBytes = baos.toByteArray();
            fis.close();
            baos.close();
            return Base64.encodeToString(fileBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
    public static Bitmap addStrokeToBitmap(Bitmap bitmap, int strokeColor, int strokeWidth) {
        int width = bitmap.getWidth() + strokeWidth * 2;
        int height = bitmap.getHeight() + strokeWidth * 2;
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Draw the stroke
        paint.setColor(strokeColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawRect(0, 0, width, height, paint);

        // Draw the original bitmap
        canvas.drawBitmap(bitmap, strokeWidth, strokeWidth, null);

        return resultBitmap;
    }
    public static Bitmap getBitmapFormUri(Activity ac, Uri uri) throws FileNotFoundException, IOException {
        InputStream input = ac.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        //Image resolution is based on 480x800
        float hh = 800f;//The height is set as 800f here
        float ww = 480f;//Set the width here to 480f
        //Zoom ratio. Because it is a fixed scale, only one data of height or width is used for calculation
        int be = 1;//be=1 means no scaling
        if (originalWidth > originalHeight && originalWidth > ww) {//If the width is large, scale according to the fixed size of the width
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//If the height is high, scale according to the fixed size of the width
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //Proportional compression
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//Set scaling
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = ac.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return compressImage(bitmap);//Mass compression again
    }

    public static Uri bitmapToUri(Bitmap bitmap, Context context) throws FileNotFoundException {
        // Save the bitmap to a temporary file
        String fileName = "temp_image.png";
        File tempFile = new File(context.getExternalCacheDir(), fileName);
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        tempFile.delete();
        // Insert the image into the MediaStore and return the content Uri
        return Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), tempFile.getAbsolutePath(), fileName, null));
    }
    public static Bitmap fileToBitmap(File file) {
        Bitmap bitmap = null;

        try {
            // Decode the file into a bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    public static String createImageFromBitmap(Bitmap bitmap, Context context) {
        String fileName = "myImage";//no .png or .jpg needed
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            // remember close file output
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }
    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//Quality compression method, here 100 means no compression, store the compressed data in the BIOS
        int options = 50;
        while (baos.toByteArray().length / 1024 > 100) {  //Cycle to determine if the compressed image is greater than 100kb, greater than continue compression
            baos.reset();//Reset the BIOS to clear it
            //First parameter: picture format, second parameter: picture quality, 100 is the highest, 0 is the worst, third parameter: save the compressed data stream
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//Here, the compression options are used to store the compressed data in the BIOS
            options -= 10;//10 less each time
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//Store the compressed data in ByteArrayInputStream
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//Generate image from ByteArrayInputStream data
        return bitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    public static byte[] compressImage(File outputFile){
        Bitmap photoBitmap = BitmapFactory.decodeFile(outputFile.getPath());
        int maxWidth = 800;
        int maxHeight = 800;
        float scale = Math.min(((float) maxWidth / photoBitmap.getWidth()), ((float) maxHeight / photoBitmap.getHeight()));
        int scaledWidth = Math.round(photoBitmap.getWidth() * scale);
        int scaledHeight = Math.round(photoBitmap.getHeight() * scale);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(photoBitmap, scaledWidth, scaledHeight, true);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] photoByte = outputStream.toByteArray();

        photoBitmap.recycle();
        scaledBitmap.recycle();
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return photoByte;
    }

    public static String getPhoto(String photoPath) {
        if(photoPath==null)
            return null;
        File outputFile = new File(photoPath+"_copy");
        File filePath = new File(photoPath);
        try {
            MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(filePath), new FileOutputStream(outputFile));
        }  catch (IOException e) {
            e.printStackTrace();
        }
        String photoString = ImageService.fileToBase64(outputFile);
        outputFile.delete();
        return photoString;
    }
    public static String getPhotoFile(String photoString, String photoPath) {
        if(photoString==null)
            return null;
        File imgFile = new File(photoPath);
        if (imgFile.exists())
            imgFile.delete();
        try {
            imgFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] decodedBytes = Base64.decode(photoString, Base64.DEFAULT);
        InputStream is = new ByteArrayInputStream(decodedBytes);
        try {
            MyEncrypter.encryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), is, new FileOutputStream(imgFile));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return imgFile.getAbsolutePath();
    }
}
