package com.cybrilla.shashank.liborgadmin;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import cz.msebera.android.httpclient.Header;

/**
 * Created by shashankm on 30/11/15.
 */
public class SendData {
    private String authorName, bookName, isbn;
    private Bitmap bookImage;
    private Context mContext;
    private String fileSrc;
    private File myFile;
    public final static String APP_PATH_SD_CARD = "/CompressedPicLiborg/";
    public final static String APP_THUMBNAIL_PATH_SD_CARD = "thumbnails";

    public SendData(String authorName, String bookName, String isbn, Bitmap bookImage, Context c) {
        this.authorName = authorName;
        this.bookName = bookName;
        this.isbn = isbn;
        this.bookImage = bookImage;
        mContext = c;
    }

    public void sendAllData() {
        String Url = "https://liborgs-1139.appspot.com/admin/upload_books";
        String username = "admin";
        String password = "cybrilla";

        Cursor cursor = mContext.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{Media.DATA, Media.DATE_ADDED, MediaStore.Images.ImageColumns.ORIENTATION}, Media.DATE_ADDED, null, "date_added ASC");
        if (cursor != null && cursor.moveToLast()) {
            Uri fileURI = Uri.parse(cursor.getString(cursor.getColumnIndex(Media.DATA)));
            fileSrc = fileURI.toString();
            cursor.close();
        }

        // Bitmap compressedImage = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(username, password);
        RequestParams params = new RequestParams();
        params.put("title", bookName);
        params.put("author", authorName);
        params.put("isbn", isbn);
        try {
            params.put("pic", storeImage());
        } catch (FileNotFoundException e) {
            Log.d("MyApp", "File not found!!!" + fileSrc);
        }
        client.post(Url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {
                String message = "";
                try {
                    message = responseBody.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new AlertDialog.Builder(mContext)
                        .setTitle("Sending")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable error) {
                JSONObject responseBody = null;
                try {
                    responseBody = new JSONObject(errorResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String message = "";
                try {
                    message = responseBody.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new AlertDialog.Builder(mContext)
                        .setTitle("Sending")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        });

    }

    private File storeImage() {
        String filename = "bookImage";
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        OutputStream outStream = null;

        File file = new File(extStorageDirectory, filename + ".jpg");
        try {
            outStream = new FileOutputStream(file);
            bookImage.compress(CompressFormat.JPEG, 80, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
