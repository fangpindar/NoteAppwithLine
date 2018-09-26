package com.example.david.todo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.json.Json;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends Activity {

    List<TaskData> tdata;
    TodoListAdapter adapter;
    SharedPreferences pref;
    RecyclerView list;

    private DrawerLayout drawerLayout;
    private NavigationView navigation_view;

    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private Boolean speeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.blue_statusbar));
        window.setNavigationBarColor(getColor(R.color.nevbar));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        FetchApi f = new FetchApi();
        f.execute();

        pref = getSharedPreferences("tasks", MODE_PRIVATE);

        list = findViewById(R.id.mainList);
        tdata = new ArrayList<>();

        navigation_view = (NavigationView) findViewById(R.id.navigation_view);
        navigation_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout = findViewById(R.id.drawerLayout);
                drawerLayout.closeDrawers();
                int id = item.getItemId();
                if (id == R.id.action_about) {
                    Toast.makeText(MainActivity.this, "關於", Toast.LENGTH_SHORT).show();
                }
                else if (id == R.id.action_help) {
                    Toast.makeText(MainActivity.this, "使用說明", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, calendarApiActivity.class);
                    startActivity(i);
                }
                else if (id == R.id.action_settings) {
                    Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(i);
                }
                return true;
            }
        });
        ImageView recordBtn = findViewById(R.id.addBtn);
        recordBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speechOnClick(v);
                return true;
            }
        });
        recordBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    // do nothing
                } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    if (speeking) {
                        mSpeechRecognizer.stopListening();
                        speeking = false;
                    }
                }
                return false;
            }
        });
    }

    public void speechOnClick(View v) {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onBeginningOfSpeech() {
                // Auto-generated method stub
            }
            @Override
            public void onBufferReceived(byte[] arg0) {
                // Auto-generated method stub
            }
            @Override
            public void onEndOfSpeech() {
                // Auto-generated method stub
            }
            @Override
            public void onError(int arg0) {
                // Auto-generated method stub
            }
            @Override
            public void onEvent(int arg0, Bundle arg1) {
                // Auto-generated method stub
            }
            @Override
            public void onPartialResults(Bundle partialResults) {
                // Auto-generated method stub
            }
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Auto-generated method stub
            }
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matchString = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                // Detect date, add to task list
                String pattern = ".*?(\\d*)月(\\d*)[日, 號].*?";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(matchString.get(0));
                if (m.find()) {
                    String n;
                    if (matchString.get(0).indexOf("日") > 0)
                        n = matchString.get(0).substring(matchString.get(0).indexOf("日") + 1);
                    else
                        n = matchString.get(0).substring(matchString.get(0).indexOf("號") + 1);
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date d = sdf.parse("2018-" + m.group(1) + "-" + m.group(2));
                        // TODO only for 2018

                        adapter.add(new TaskData(n, d));

                        // google calendar api
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, CalendarApi.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("taskname", n);
                        bundle.putLong("taskdate", d.getTime());
                        intent.putExtras(bundle);
                        startActivity(intent);

                        Toast.makeText(getBaseContext(), "GOT TASK: " + n + " at " + m.group(1) + "/" + m.group(2), Toast.LENGTH_SHORT).show();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else Toast.makeText(getBaseContext(), matchString.get(0), Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onRmsChanged(float rmsdB) {
                // Auto-generated method stub
            }
        });
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.TAIWAN.toString());
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());

        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        speeking = true;
    }

    class FetchApi extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {

            TextView tt = findViewById(R.id.title_today);
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.TAIWAN);
            tt.setText("Today, " + df.format(new Date()));

            // update weather
            String urlString = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-C0032-001?Authorization=CWB-A1029206-C82C-44F9-A24D-D3EDC0B3CBAE";
            HttpURLConnection connection = null;
            final TextView weathertext = findViewById(R.id.weathertext);
            final ImageView weatherIcon = findViewById(R.id.weatherIcon);

            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(1500);
                connection.setConnectTimeout(1500);
                connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36");
                connection.setInstanceFollowRedirects(true);

                if( connection.getResponseCode() == HttpsURLConnection.HTTP_OK ){
                    // 讀取網頁內容
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader  = new BufferedReader( new InputStreamReader(inputStream) );

                    String tempStr;
                    StringBuffer stringBuffer = new StringBuffer();

                    while( ( tempStr = bufferedReader.readLine() ) != null ) {
                        stringBuffer.append( tempStr );
                    }

                    bufferedReader.close();
                    inputStream.close();

                    String  mime = connection.getContentType();
                    boolean isMediaStream = false;

                    if( mime.indexOf("audio") == 0 ||  mime.indexOf("video") == 0 ){
                        isMediaStream = true;
                    }

                    String responseString = stringBuffer.toString();
                    JSONObject j = new JSONObject(responseString);

                    final int temp_low = j.getJSONObject("records").getJSONArray("location").getJSONObject(11).getJSONArray("weatherElement")
                            .getJSONObject(2).getJSONArray("time").getJSONObject(0).getJSONObject("parameter").getInt("parameterName");
                    final int temp_high = j.getJSONObject("records").getJSONArray("location").getJSONObject(11).getJSONArray("weatherElement")
                            .getJSONObject(4).getJSONArray("time" +
                                    "").getJSONObject(0).getJSONObject("parameter").getInt("parameterName");
                    final int rain = j.getJSONObject("records").getJSONArray("location").getJSONObject(11).getJSONArray("weatherElement")
                            .getJSONObject(1).getJSONArray("time").getJSONObject(0).getJSONObject("parameter").getInt("parameterName");
                    final int weatherType = j.getJSONObject("records").getJSONArray("location").getJSONObject(11).getJSONArray("weatherElement")
                            .getJSONObject(0).getJSONArray("time").getJSONObject(0).getJSONObject("parameter").getInt("parameterValue");

                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (weatherType == 1) weatherIcon.setImageResource(R.drawable.sun);
                            else if (weatherType == 7 || weatherType == 8 || weatherType == 43 || weatherType == 45 || weatherType == 46)
                                weatherIcon.setImageResource(R.drawable.suncloud);
                            else if (weatherType == 2 || weatherType == 3 || weatherType == 5 || weatherType == 6 || weatherType == 44 || weatherType == 49)
                                weatherIcon.setImageResource(R.drawable.cloud);
                            else if (weatherType == 12 || weatherType == 13 || weatherType == 17 || weatherType == 18 || weatherType == 24 || weatherType == 34)
                                weatherIcon.setImageResource(R.drawable.sunrain);
                            else weatherIcon.setImageResource(R.drawable.rain); // 4 26 31 36 57 58 59 60
                            weathertext.setText(temp_low + " - " + temp_high + " Degrees" + ", " + rain + "% Rain");
                        }
                    });
                    // TODO save weather state in phone for faster loadtime
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            finally {
                if( connection != null ) {
                    connection.disconnect();
                }
            }
            return null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.saveTask(pref);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String allTasks = pref.getString("tasks", null);
        Log.i("Basic", "onCreate: " + allTasks);
        tdata.clear();
        if (allTasks != null) {
            String[] arr = allTasks.split("\n");
            Log.i("Basic", "onCreate: arr.length = " + arr.length);
            for (int i = 0; i < arr.length; i++) {
                if (!arr[i].contains("-=-=-=-")) continue;
                String[] t = arr[i].split("-=-=-=-");
                tdata.add(new TaskData(t[0], new Date( Long.parseLong(t[1] ))));
            }
            pref.edit().clear().apply();
        }

        adapter = new TodoListAdapter(tdata, this);


        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(list);
    }

    public void addBtnOnClick(View v) {
        Log.i("Basic", "addBtnOnClick: add to list");
        showAddTaskDialog();
    }

    private void showAddTaskDialog()
    {
        final Dialog alert = new Dialog(this);
        LayoutInflater inflater = getLayoutInflater();

        alert.setTitle("Title");
        final View inputView = inflater.inflate(R.layout.input_dialog, null);
        alert.setContentView(inputView);
        final EditText input1 = inputView.findViewById(R.id.inputTaskname);
        final DatePicker input2 = inputView.findViewById(R.id.inputTaskdate);

        Button checkBtn = inputView.findViewById(R.id.checkBtn);
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String n = input1.getText().toString();
                Date d = getDateFromDatePicker(input2);
                alert.dismiss();

                if (n.isEmpty()) Toast.makeText(MainActivity.this, "Task name is empty", Toast.LENGTH_SHORT).show();
                else {
                    adapter.add(new TaskData(n, d));

                    // google calendar api
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, CalendarApi.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("taskname", n);
                    bundle.putLong("taskdate", d.getTime());
                    intent.putExtras(bundle);
                    startActivity(intent);

                }
            }
        });

        alert.show();
    }

    public static java.util.Date getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }


}
