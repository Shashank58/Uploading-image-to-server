package com.cybrilla.shashank.liborgadmin;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

public class MainActivity extends AppCompatActivity {
    private EditText isbnNumber, authorName, bookName;
    private String author, book, isbn;
    private ImageView bookImage;
    private TextView uploadImage;
    private Button send;
    private Bitmap bm, scaledBitmap;
    private static final int PIC_CAPTURED = 0;
    private static final int CROPPED_PIC = 2;
    private int x1, y1, x2, y2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isbnNumber = (EditText) findViewById(R.id.isbnNumber);
        authorName = (EditText) findViewById(R.id.authorName);
        bookName = (EditText) findViewById(R.id.bookName);

        send = (Button) findViewById(R.id.send);
        uploadImage = (TextView) findViewById(R.id.uploadImage);
        bookImage = (ImageView) findViewById(R.id.bookImage);

        isbnNumber.setKeyListener(null);

        uploadImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, PIC_CAPTURED);
            }
        });

        isbnNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                integrator.initiateScan();
            }
        });

        send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                author = authorName.getText().toString();
                book = bookName.getText().toString();
                isbn = isbnNumber.getText().toString();
                Log.e("Main activity", "Book Image: "+bookImage.toString());
                if(author.equals("") || book.equals("")){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Alert")
                            .setMessage("One of the fields is empty")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                } else {
                    SendData s = new SendData(author, book, isbn, scaledBitmap, MainActivity.this);
                    s.sendAllData();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("BitmapImage", scaledBitmap);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        Bitmap image = savedInstanceState.getParcelable("BitmapImage");
        bookImage.setImageBitmap(image);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if(requestCode == PIC_CAPTURED){
                Uri bp = data.getData();
                performCrop(bp);
            }
            else if (requestCode == CROPPED_PIC){
                bm = data.getExtras().getParcelable("data");
                int height = bm.getHeight();
                Log.e("Main activity", "Height: "+height);
                int width = bm.getWidth();
                int finalWidth;
                int finalHeight ;
                if(height >= width){
                    finalHeight = 480;
                    finalWidth = (finalHeight * width) / height;
                }else {
                    finalWidth = 480;
                    finalHeight = (finalWidth * height) / width;
                }
                scaledBitmap = Bitmap.createScaledBitmap(bm, finalWidth, finalHeight, true);
                bookImage.setImageBitmap(scaledBitmap);
            }
            else{
                String code = data.getStringExtra("SCAN_RESULT");
                isbnNumber.setText(code);
            }
        }
    }

    private void performCrop(Uri bp){
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");

            cropIntent.setDataAndType(bp, "image/*");

            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", x1);
            cropIntent.putExtra("aspectY", y1);
            cropIntent.putExtra("outputX", x2);
            cropIntent.putExtra("outputY", y2);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, CROPPED_PIC);
        } catch (ActivityNotFoundException e){
            Toast toast = Toast.makeText(getApplicationContext(), "Your device doesnot support cropping an image", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}