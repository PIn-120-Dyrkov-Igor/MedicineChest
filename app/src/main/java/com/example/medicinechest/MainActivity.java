package com.example.medicinechest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Кнопки
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);

        //Устанавливаем цвет фона
        button1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        button2.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        button3.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));


        //Обработчик клика по кнопке 1
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Создаем Intent для перехода на новую активность
                Intent intent = new Intent(MainActivity.this, MedicationListActivity.class);
                //Запускаем новую активность
                startActivity(intent);
            }
        });

        //Обработчик клика по кнопке 2
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Создаем Intent для перехода на новую активность
                Intent intent = new Intent(MainActivity.this, MyListActivity.class);
                //Запускаем новую активность
                startActivity(intent);
            }
        });

        //Обработчик клика по кнопке 3
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Создаем Intent для перехода на новую активность
                Intent intent = new Intent(MainActivity.this, UserListActivity.class);
                //Запускаем новую активность
                startActivity(intent);
            }
        });
    }
}