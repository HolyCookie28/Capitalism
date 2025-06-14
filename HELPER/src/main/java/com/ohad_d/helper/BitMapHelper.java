package com.ohad_d.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BitMapHelper {

    public static String encodeTobase64(Bitmap image) {
        if (image != null) {
            Bitmap immagex = image;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            immagex.compress(Bitmap.CompressFormat.PNG, 90, baos);
            byte[] b = baos.toByteArray();
            String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
            return imageEncoded;
        } else
            return null;
    }

    public static String encodeTobase64(byte[] bytes){
        return encodeTobase64(byteArrayToBitmap(bytes));
    }

    public static Bitmap decodeBase64(String input) {
        if (input != null && !input.isEmpty()) {
            byte[] decodedByte = Base64.decode(input, 0);
            return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        } else
            return null;
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap byteArrayToBitmap(byte[] b) {
        return (b == null || b.length == 0) ? null : BitmapFactory
                .decodeByteArray(b, 0, b.length);
    }

    public static Bitmap uriToBitmap(Uri uri, Context context) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.getContentResolver(), uri));
        } else {
            //return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
        }
    }


}