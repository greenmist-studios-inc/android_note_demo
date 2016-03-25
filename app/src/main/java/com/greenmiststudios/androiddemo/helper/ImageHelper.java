package com.greenmiststudios.androiddemo.helper;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.SparseIntArray;
import com.greenmiststudios.demolibrary.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * User: geoffpowell
 * Date: 11/10/15
 */
public class ImageHelper {

    private static final int SCALED_WIDTH = 2048;
    private static final SparseIntArray EXIF_ROTATIONS = new SparseIntArray() {{
        put(1, 0);
        put(3, 180);
        put(6, 90);
        put(8, 270);
    }};

    private static int getRotation(String imgPath) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(imgPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            return EXIF_ROTATIONS.get(orientation);
        } catch (IOException e) {
            return 0;
        }
    }

    public static File getImage(Context context, Intent data) {
        File temp = getImageFile(context, data);
        if (temp == null) {
            new AlertDialog.Builder(context, R.style.AppTheme_Alert)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_image)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
        return temp;
    }

    private static File getImageFile(Context activity, Intent data) {
        try {
            if (data != null && data.getData() != null) {
                if (isContentPath(data.getData())) {
                    return new File(getContentPath(activity, data.getData()));
                } else {
                    return new File(data.getData().getPath());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File saveFile(Context context, String path) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File imageFile = new File(context.getCacheDir(), "IMG_" + timeStamp + ".jpg");

        //scale down image first
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        //get size of bitmap
        @SuppressWarnings("UnusedAssignment")
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        int outWidth = options.outWidth;
        int scaleFactor = 1;

        while (outWidth / 2 > SCALED_WIDTH) {
            outWidth /= 2;
            scaleFactor *= 2;
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = scaleFactor;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        bitmap = BitmapFactory.decodeFile(path, options);
        Bitmap orientedBitmap = bitmap;

        int rotation = getRotation(path);
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            orientedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
        orientedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return imageFile;
        } catch (IOException e) {
            new AlertDialog.Builder(context, R.style.AppTheme_Alert)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_image)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return null;
        }
    }

    private static boolean isContentPath(Uri uri) {
        return uri.getScheme().equals("content");
    }

    private static String getContentPath(Context activity, Uri uri) throws IOException {
        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();
        cursor = activity.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        assert cursor != null;
        cursor.moveToFirst();
        int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        if (index > cursor.getCount()) {
            throw new IOException();
        }

        String path = cursor.getString(index);
        cursor.close();

        return path;
    }

    public static Intent getImageIntents(File tempGetFilePath, Context context) {
        List<Intent> extraIntents = new ArrayList<>();
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempGetFilePath));
        PackageManager packageManager = context.getPackageManager();

        Intent fileIntent = new Intent();
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("image/*");

        List<ResolveInfo> listFile = packageManager.queryIntentActivities(fileIntent, 0);
        for (ResolveInfo res : listFile) {
            String packageName = res.activityInfo.packageName;
            Intent intent = new Intent(fileIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            extraIntents.add(intent);
        }

        Intent chooserIntent = Intent.createChooser(captureIntent, context.getString(R.string.intent_title));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents.toArray(new Parcelable[extraIntents.size()]));
        return chooserIntent;
    }
}
