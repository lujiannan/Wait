package com.example.wait.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.dmallcott.dismissibleimageview.DismissibleImageView;
import com.example.wait.R;
import com.example.wait.models.Matter;
import com.example.wait.models.MatterList;
import com.github.zagum.switchicon.SwitchIconView;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;

import org.litepal.LitePal;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

import info.hoang8f.widget.FButton;

public class AddNewEventActivity extends AppCompatActivity {

    private ImageView addEventBack_imageView;
    private SwitchIconView addEventTitle_iconView;
    private MaterialEditText addEventTitle_editText;
    private SwitchIconView addEventDate_iconView;
    private MaterialEditText addEventPickDate_editText;
    private SwitchIconView addEventPhoto_iconView;
    private ImageButton addEventDeletePhoto_imageView;
    private ImageButton addEventPickPhoto_imageView;
    private DismissibleImageView addEventPhoto_imageView;
    private FButton addEventSubmit_btn;

    private Date selectedDate;
    private MatterList sMatterList;
    private Matter sMatter;
    private String imageSourcePath = null;

    private static final int REQUEST_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_add_new_event);

        Intent intent = getIntent();
        sMatterList = (MatterList) intent.getSerializableExtra("matterList");
        sMatter = (Matter) intent.getSerializableExtra("matter");

        baseInit();

        if (sMatter != null){
            updateAttr();
        }
    }

    public void baseInit(){
        SQLiteDatabase db = LitePal.getDatabase();

        addEventBack_imageView = findViewById(R.id.addEventBack_imageView);
        addEventTitle_iconView = findViewById(R.id.addEventTitle_iconView);
        addEventTitle_editText = findViewById(R.id.addEventTitle_editText);
        addEventDate_iconView = findViewById(R.id.addEventDate_iconView);
        addEventPickDate_editText = findViewById(R.id.addEventPickDate_editText);
        addEventPhoto_iconView = findViewById(R.id.addEventPhoto_iconView);
        addEventDeletePhoto_imageView = findViewById(R.id.addEventDeletePhoto_imageView);
        addEventPickPhoto_imageView = findViewById(R.id.addEventPickPhoto_imageView);
        addEventPhoto_imageView = findViewById(R.id.addEventPhoto_imageView);
        addEventSubmit_btn = findViewById(R.id.addEventSubmit_btn);

        addEventSubmit_btn.setButtonColor(0xFFFFFFFF);
        addEventSubmit_btn.setCornerRadius(30);

        addEventTitle_editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (Objects.requireNonNull(addEventTitle_editText.getText()).toString().equals("")) {
                    addEventTitle_iconView.switchState();
                }
            }
        });

        addEventBack_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventBack_imageView.setAlpha(0.5f);
                onSupportNavigateUp();
            }
        });

        addEventPickDate_editText.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                datePicker();
            }
        });


        addEventPickPhoto_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhotoBtnOnClick();
            }
        });

        addEventDeletePhoto_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventPhoto_imageView.setImageDrawable(null);
                imageSourcePath = null;
                addEventPhoto_iconView.switchState();
            }
        });

        addEventSubmit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!addEventTitle_editText.validateWith(new RegexpValidator("???????????????1~20??????", "^.{1,20}$")) |
                        !addEventPickDate_editText.validateWith(new RegexpValidator("??????????????????", "^.{10,20}$"))){
                    return;
                }
//                addEventPickDate_editText.validateWith(new RegexpValidator("??????????????????", "^(([0-9]{4})-(0[1-9]|1[0-2])-(0[1-9]|1[0-9]|2[0-9]|3[0-1]))$"));
                if (sMatter == null) {
                    saveEvent();
                }else{
                    updateEvent();
                }
            }
        });
    }

    /**
     * rewrite the back button on the action bar to make its functionality works better
     * @return false to return to the activity before
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //???????????????????????????????????????view?????????????????????????????????
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //???????????????????????????view
            View v = getCurrentFocus();
            //????????????????????????????????????
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        //?????????activity???????????????????????????????????????????????????????????????????????????
        return super.dispatchTouchEvent(ev);
    }

    //???????????????????????????
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        //?????????????????????????????????view???editText?????????????????????????????????
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            // ??????EditText????????????????????????
            return !(event.getX() > left) || !(event.getX() < right)
                    || !(event.getY() > top) || !(event.getY() < bottom);
        }
        // ??????????????????EditText?????????????????????????????????????????????????????????????????????EditText???
        return false;
    }

    //?????????????????????editText????????????
    private void hideKeyboard(IBinder token) {
        addEventTitle_editText.clearFocus();
        if (token != null) {
            //???????????????InputMethodManager???????????????????????????????????????
            //InputMethodManager?????????????????????????????????manager
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (im != null) {
                im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * Opens date picker dialog, when date button is clicked
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void datePicker() {
        SwitchDateTimeDialogFragment instance = SwitchDateTimeDialogFragment.newInstance(
                "?????????????????????",
                "??????",
                "??????"
        );
        instance.startAtCalendarView();
        instance.set24HoursMode(true);
        instance.setMinimumDateTime(new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime());
        instance.setMaximumDateTime(new GregorianCalendar(2099, Calendar.DECEMBER, 31).getTime());
        instance.setDefaultDateTime(new Date());

        // Define new day and month format
        try {
            instance.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("dd MMMM", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            Log.e(TAG, e.getMessage());
        }

        // Set listener
        instance.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                if (Objects.requireNonNull(addEventPickDate_editText.getText()).toString().equals("")){
                    addEventDate_iconView.switchState();
                }
                // Date is get on positive button click
                selectedDate = date;
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd  hh:mm");
                addEventPickDate_editText.setText(sdf.format(date));
            }

            @Override
            public void onNegativeButtonClick(Date date) {
                // Date is get on negative button click
            }
        });

        // Show
        instance.show(getSupportFragmentManager(), "dialog_time");
    }

    /**
     * Handles what happens when we click on the pick date button
     */
    public void pickPhotoBtnOnClick(){
        // dynamically apply permission of read/write the disk
        if (ContextCompat.checkSelfPermission(AddNewEventActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddNewEventActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }
        if (ContextCompat.checkSelfPermission(AddNewEventActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            // open the album
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            // Intent.ACTION_GET_CONTENT = "android.intent.action.GET_CONTENT"
            intent.setType("image/*");
            AddNewEventActivity.this.startActivityForResult(intent, REQUEST_PERMISSION_CODE); // ????????????
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (resultCode == RESULT_OK) { // ???????????????????????????
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4?????????????????????????????????????????????
                        handleImageOnKitKat(data);
                    } else {
                        // 4.4??????????????????????????????????????????
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // ?????????document?????????Uri????????????document id??????
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                // ????????????????????????id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content: //downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }else if ("com.android.externalstorage.documents".equals(uri.getAuthority())){
                // ??????????????????
                final String[] divide = docId.split(":");
                final String type = divide[0];
                if ("primary".equals(type)) {
                    imagePath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat(divide[1]);
                } else {
                    imagePath = "/storage/".concat(type).concat("/").concat(divide[1]);
                }

            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // ?????????content?????????Uri??????????????????????????????
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // ?????????file?????????Uri?????????????????????????????????
            imagePath = uri.getPath();
        }
        imageSourcePath = imagePath;
        // ??????????????????????????????
        displayImage(imagePath);
    }

    /**
     * android 4.4?????????????????????
     * @param data
     */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        imageSourcePath = imagePath;
        displayImage(imagePath);
    }

    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // ??????Uri???selection??????????????????????????????
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            addEventPhoto_imageView.setImageBitmap(bitmap);
            addEventPhoto_iconView.switchState();
        } else {
            Toast.makeText(this, "????????????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateAttr(){
        addEventTitle_editText.setText(sMatter.getTitle());
        addEventTitle_iconView.switchState();
        selectedDate = sMatter.getTargetDate();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd  hh:mm");
        addEventPickDate_editText.setText(sdf.format(selectedDate));
        addEventDate_iconView.switchState();
        try {
            File file = new File(sMatter.getImagePath());
            addEventPhoto_imageView.setImageURI(Uri.fromFile(file));
            addEventPhoto_iconView.switchState();
        }catch (NullPointerException e){
        }
    }

    public void saveEvent(){
        Matter matter = new Matter();
        if (Objects.requireNonNull(addEventTitle_editText.getText()).toString().length() > 0 &&
                Objects.requireNonNull(addEventPickDate_editText.getText()).length() > 0){
            matter.setTitle(Objects.requireNonNull(addEventTitle_editText.getText()).toString());
            matter.setTargetDate(selectedDate);
            matter.setImagePath(imageSourcePath);
            boolean success = matter.save();
            if (success) {
                sMatterList.addMatter(matter);
                Toast.makeText(AddNewEventActivity.this, "??????????????????!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                if (sMatterList.hasMatter(matter)){
                    Toast.makeText(AddNewEventActivity.this, "??????????????????!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(AddNewEventActivity.this, "??????????????????!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void updateEvent(){
        Matter matter = new Matter();
        if (Objects.requireNonNull(addEventTitle_editText.getText()).toString().length() > 0 &&
                Objects.requireNonNull(addEventPickDate_editText.getText()).length() > 0) {
            matter.setTitle(Objects.requireNonNull(addEventTitle_editText.getText()).toString());
            matter.setTargetDate(selectedDate);
            matter.setImagePath(imageSourcePath);
                if (imageSourcePath == null){
                    matter.setToDefault("imagePath");
                }
                int affectedRows = matter.updateAll("title = ?", sMatter.getTitle());
                if (affectedRows == 1){
                    Toast.makeText(AddNewEventActivity.this, "??????????????????!", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    Toast.makeText(AddNewEventActivity.this, "??????????????????!", Toast.LENGTH_SHORT).show();
                }
        }
    }
}