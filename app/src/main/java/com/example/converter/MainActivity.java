package com.example.converter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Runnable runnable;
    private Thread thread;
    private ListView listView;
    private String TAG = "logText";
    private List<ListItemClass> arrayItem;
    private JSONObject jsonDoc;
    private CustomArrayAdapter adapter;
    private ArrayAdapter<String> adapterSpinner;

    private Spinner spinner;
    private Button button;
    private TextView textViewConvert;
    private TextView numberValue;

    private String choice;

    private String[] arrayValute = {"AUD", "AZN", "GBP", "AMD", "BYN", "BGN", "BRL", "HUF", "HKD", "DKK", "USD",
                                    "EUR", "INR", "KZT", "CAD", "KGS", "CNY", "MDL", "NOK", "PLN", "RON", "XDR",
                                    "SGD", "TJS", "TRY", "TMT", "UZS", "UAH", "CZK", "SEK", "CHF", "ZAR", "KRW", "JPY"};

    private void init() {
        listView = findViewById(R.id.listView);
        arrayItem = new ArrayList<>();
        adapter = new CustomArrayAdapter(this, R.layout.item_list, arrayItem, getLayoutInflater());
        listView.setAdapter(adapter);

        spinner = (Spinner) findViewById(R.id.spinner);
        adapterSpinner = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, arrayValute);
        adapterSpinner.setDropDownViewResource(com.google.android.material.R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);

        button = (Button) findViewById(R.id.button);
        numberValue = (TextView) findViewById(R.id.editTextNumber);
        textViewConvert = (TextView) findViewById(R.id.textViewConvert);

        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(this);

        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    getWeb();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread = new Thread(runnable);
        thread.start();
    }

    private void getWeb() throws IOException, JSONException {
        URL url = new URL("https://www.cbr-xml-daily.ru/daily_json.js");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        Log.d(TAG, "Parse Code: " + connection.getResponseCode());

        StringBuilder content;

        try (BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            content = new StringBuilder();
            while ((line = input.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
        } finally {
            Log.d(TAG, "End Parse");
            connection.disconnect();
        }

        jsonDoc = new JSONObject(content.toString());

        for (String ul : arrayValute) {
            ListItemClass items = new ListItemClass();
            items.setName(ul);
            items.setValue(jsonDoc.getJSONObject("Valute").getJSONObject(ul).getString("Value"));
            arrayItem.add(items);
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberValue.getText().toString().length() == 0) {
                    Toast.makeText(MainActivity.this, "Введите значение 'RUB'", Toast.LENGTH_SHORT).show();
                } else {
                    Double answer;
                    Double content;
                    Double valuteValue = null;
                    String answerStr;

                    try {
                        valuteValue = jsonDoc.getJSONObject("Valute").getJSONObject(choice).getDouble("Value");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    content = Double.valueOf((String) numberValue.getText().toString());
                    answer = content * valuteValue;

                    answerStr = String.format("%.3f", answer);

                    textViewConvert.setText(answerStr);
                }
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        choice = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}