package com.example.mydocsapp.apputils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.TemplateObject;
import com.example.mydocsapp.services.AppService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PDFMaker {
    private static final int CREATE_MODE_PASSPORT = 1;
    private static final int CREATE_MODE_TEMPLATE = 3;
    private static final int PDF_WIDTH = 595*2;
    private static final int PDF_HEIGHT = 842*2;

    public static Boolean createPassportPDF(Passport passport, int id) {
        return createPDF(CREATE_MODE_PASSPORT, passport, null, id);
    }
    public static Boolean createTemplatePDF(List<TemplateObject> templateObjects, Map<TemplateObject, View> templateViews, int id) {
        return createPDF(CREATE_MODE_TEMPLATE, templateViews, templateObjects, id);
    }
    public static Boolean createPDF(int mode, Object object, Object objectSet, int id) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PDF_WIDTH, PDF_HEIGHT, 1).create();

        PdfDocument.Page page1 = pdfDocument.startPage(pageInfo);
        Canvas canvas1 = page1.getCanvas();

        Paint paint = new Paint();
        paint.setTextSize(24f);

        Paint boldPaint = new Paint(paint);
        boldPaint.setTypeface(Typeface.DEFAULT_BOLD);

        float x = 100;
        float y = 100;
        String fileName;
        switch (mode){
            case CREATE_MODE_PASSPORT:
                Passport passport = (Passport) object;
                fileName = "/passport" + id + "_" +passport.Id + ".pdf";
                makePassport(pdfDocument, pageInfo, page1, canvas1, paint, boldPaint, x, y, passport);
                break;
            case CREATE_MODE_TEMPLATE:
                Map<TemplateObject, View> templateViews = (Map<TemplateObject, View>) object;
                List<TemplateObject> templateObjects = (List<TemplateObject>) objectSet;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    fileName = "/passport" + id + "_" +templateViews.keySet().stream().findAny().get().TemplateId + ".pdf";
                else
                    fileName = "/template" + id + "_"  + ".pdf";
                makeTemplate(pdfDocument, pageInfo, page1, canvas1, paint, boldPaint, x, y, templateViews, templateObjects);
                break;
            default:
                fileName = "";
                break;
        }
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        String filePath = directory + fileName;
        File file = new File(filePath);
        Boolean result;
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        pdfDocument.close();
        return result;
    }

    private static void makeTemplate(PdfDocument pdfDocument, PdfDocument.PageInfo pageInfo, PdfDocument.Page page1, Canvas canvas1, Paint paint, Paint boldPaint, float x, float y, Map<TemplateObject, View> templateViews, List<TemplateObject> templateObjects) {
        for (TemplateObject templateObject : templateObjects) {
            if(y<PDF_HEIGHT-200) {
                Log.e("dedPerded", templateObject.Title);
                Log.e("dedPerded", y+"");
                Log.e("dedPerded", PDF_HEIGHT+"");
                View view = templateViews.get(templateObject);
                Log.e("TemplateDed",templateObject.Title );
                if (templateObject.Type.equals("EditText") | templateObject.Type.equals("NumberText")) {
                    EditText editText = (EditText) view;
                    String value = editText.getText().toString();
                    y = getYMakeText(canvas1, paint, boldPaint, x, y, templateObject.Title+": ", value);
                } else if (templateObject.Type.equals("CheckBox")) {
                    CheckBox checkBox = (CheckBox) view;
                    String value = checkBox.isChecked() ? "да" : "нет";
                    y = getYMakeText(canvas1, paint, boldPaint, x, y, templateObject.Title+": ", value);
                }else if (templateObject.Type.equals("Photo")) {
                    ImageView imageView = (ImageView) view;
                    Drawable photoDrawable = imageView.getDrawable();
                    if(photoDrawable==null)
                        continue;
                    Bitmap photo = addPhoto(ImageService.drawableToBitmap(photoDrawable),500,500);
                    int imageHeight = photo.getHeight();
                    if(imageHeight + y > PDF_HEIGHT){
                        pdfDocument.finishPage(page1);
                        PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(PDF_WIDTH, PDF_HEIGHT, 1).create();
                        PdfDocument.Page page = pdfDocument.startPage(pageInfo2);
                        page1 = page;
                        canvas1 = page1.getCanvas();
                        y = x = 100;
                    }
                    canvas1.drawText(templateObject.Title, x, y, paint);
                    y += 40;
                    canvas1.drawBitmap(photo, x, y, null);
                    y += imageHeight + 40;
                }
            }
            else{
                pdfDocument.finishPage(page1);
                PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(PDF_WIDTH, PDF_HEIGHT, 1).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo2);
                page1 = page;
                canvas1 = page1.getCanvas();
                y = x = 100;
            }
        }
        pdfDocument.finishPage(page1);
    }

    private static void makePassport(PdfDocument pdfDocument, PdfDocument.PageInfo pageInfo, PdfDocument.Page page1, Canvas canvas1, Paint paint, Paint boldPaint, float x, float y, Passport passport) {
        Bitmap photo = addPhoto(passport.FacePhoto,400,300);
        if(photo!=null) {
            canvas1.drawBitmap(photo, x, y, null);
            int imageHeight = photo.getHeight();
            y += imageHeight+40;
        }
        y = getYMakeText(canvas1, paint, boldPaint, x, y, "Серия и номер: ", passport.SeriaNomer);
        y = getYMakeText(canvas1, paint, boldPaint, x, y, "Код подразделения: ", passport.DivisionCode);
        y = getYMakeText(canvas1, paint, boldPaint, x, y, "Дата выдачи: ", passport.GiveDate);
        y = getYMakeText(canvas1, paint, boldPaint, x, y, "Кем выдан: ", passport.ByWhom);
        y = getYMakeText(canvas1, paint, boldPaint, x, y, "ФИО: ", passport.FIO);
        y = getYMakeText(canvas1, paint, boldPaint, x, y, "Дата рождения: ", passport.BirthDate);
        y = getYMakeText(canvas1, paint, boldPaint, x, y, "Пол: ", passport.Gender);
        y = getYMakeText(canvas1, paint, boldPaint, x, y, "Место рождения: ", passport.BirthPlace);
        getYMakeText(canvas1, paint, boldPaint, x, y, "Место жительства: ", passport.ResidencePlace);
        pdfDocument.finishPage(page1);

        PdfDocument.Page page2 = pdfDocument.startPage(pageInfo);
        Canvas canvas2 = page2.getCanvas();
        x = y = 100;
        photo = addPhoto(passport.PhotoPage1,500,500);
        if(photo!=null) {
            canvas2.drawText("Лицевая сторона: ", x, y, paint);
            canvas2.drawBitmap(photo, x, y +40, null);
            int imageHeight = photo.getHeight();
            y += imageHeight + 40;
        }
        photo = addPhoto(passport.PhotoPage2,500,500);
        if(photo!=null) {
            canvas2.drawText("Обратная сторона: ", x, y, paint);
            canvas2.drawBitmap(photo, x, y +40, null);
        }
        if(!isNullOrEmpty(passport.PhotoPage1) & !isNullOrEmpty(passport.PhotoPage2))
            pdfDocument.finishPage(page2);
    }

    private static float getYMakeText(Canvas canvas1, Paint paint, Paint boldPaint, float x, float y, String s, String seriaNomer) {
        canvas1.drawText(s, x, y, boldPaint);
        canvas1.drawText(isNullOrEmpty(seriaNomer) ? "нет данных" : seriaNomer, x + boldPaint.measureText(s), y, paint);
        y += 40;
        return y;
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
    private static Bitmap addPhoto(String photoPath, int width, int height){
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        if(photoPath==null)
            return null;
        if(photoPath.equalsIgnoreCase(""))
            return null;
        File outputFile = new File(directory + "/photo_copy");
        File encFile = new File(photoPath);
        Bitmap photo;
        try {
            MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
            photo = ImageService.fileToBitmap(outputFile);
            outputFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return addPhoto(photo,width,height);
    }
    private static Bitmap addPhoto(Bitmap photo, int width, int height){
        if(width==height)
            photo = bitmapToSquare(photo);
        int imageWidth = photo.getWidth();
        int imageHeight = photo.getHeight();
        float scaleFactor = Math.min((float) width / imageWidth, (float) height / imageHeight);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleFactor, scaleFactor);
        Bitmap scaledPhotoBitmap = Bitmap.createBitmap(photo, 0, 0, imageWidth, imageHeight, matrix, true);
        scaledPhotoBitmap = Bitmap.createScaledBitmap(scaledPhotoBitmap, (int) (imageWidth * scaleFactor), (int) (imageWidth * scaleFactor), true);
        return scaledPhotoBitmap;
    }
    private static Bitmap bitmapToSquare(Bitmap originalBitmap){
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        int squareSize = Math.max(originalWidth, originalHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(squareSize, squareSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resizedBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(originalBitmap, (squareSize - originalWidth) / 2, (squareSize - originalHeight) / 2, null);
        return resizedBitmap;
    }
}
