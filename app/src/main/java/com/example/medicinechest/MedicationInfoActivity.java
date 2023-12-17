package com.example.medicinechest;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MedicationInfoActivity extends AppCompatActivity {

    private DbHelper mDBHelper;
    private SQLiteDatabase mDb;

    private int id;
    private String name;
    private String form;
    private String storage;
    private String url;
    private String startTime;
    private String isactive;

    private EditText editData;
    private EditText editText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_info);

        mDBHelper = new DbHelper(this);
        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
        try {
            mDb = mDBHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }

        //Получаем Intent, который запустил этот экран
        Intent intent = getIntent();

        //Получаем идентификатор пользователя из Intent
        int userId = intent.getIntExtra("_id", -1);

        //Получаем данные из БД по выбранному Id
        Cursor cursor = mDb.rawQuery("SELECT * FROM allmedication WHERE _id = " + userId + "", null);
        cursor.moveToFirst();

        //Пробегаем по всем клиентам
        while (!cursor.isAfterLast()) {

            // Заполняем клиента
            id = (cursor.getInt(0));
            name = (cursor.getString(1));
            form = (cursor.getString(2));
            storage = (cursor.getString(3));
            url = (cursor.getString(4));
            startTime = (cursor.getString(7));

            //Переходим к следующему клиенту
            cursor.moveToNext();
        }
        cursor.close();

        // Выводим идентификатор пользователя на экран
        TextView userIdTextView = findViewById(R.id.textViewTitle);
        userIdTextView.setText("Название медикамента: \n'" + name +
                "'\nФорма выпуска: \n'" +
                form);


        editText = findViewById(R.id.editText);
        editText.setText(storage);

        editData = findViewById(R.id.editData);
        editData.setText(startTime);
        // Обработчик нажатия для вызова TimePickerDialog
        editData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });


    }
    //Открытие ссылки по клику
    public void onLinkClick(View view) {
        Intent openURL = new Intent(android.content.Intent.ACTION_VIEW);
        openURL.setData(Uri.parse(url));
        startActivity(openURL);
        //Проверка можем ли открыть
    }

    public void onSaveButtonClick(View view) throws ParseException {
        int count = Integer.parseInt(editText.getText().toString());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String date = editData.getText().toString();
        Date enteredDate = dateFormat.parse(date);
        Date currentDate = new Date();  // Текущая дата
        if(enteredDate.after(currentDate)){
            Toast.makeText(getApplicationContext(), "Дата изготовления не может быть выше теущей", Toast.LENGTH_SHORT).show();
        }
        else {
            // Создаем объект ContentValues для хранения новых данных
            ContentValues values = new ContentValues();
            values.put("storage", count);
            values.put("startTime", String.valueOf(editData.getText()));

            // Выполняем обновление записи в базе данных
            mDb.update("allmedication", values, "_id=?", new String[]{String.valueOf(id)});

            Toast.makeText(getApplicationContext(), "Данные изменены!", Toast.LENGTH_SHORT).show();

            finish();
        }
    }

    public void onEditTextClick(View view) {
        if (view.getId() == R.id.editData) {
            showDatePickerDialog();
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Получите выбранную дату и обновите ваш EditText
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        editData.setText(selectedDate);
                    }
                },
                year, month, dayOfMonth
        );

        datePickerDialog.show();
    }
}
