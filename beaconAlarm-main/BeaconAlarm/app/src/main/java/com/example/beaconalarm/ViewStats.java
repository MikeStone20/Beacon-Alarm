package com.example.beaconalarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ViewStats extends AppCompatActivity {

    private AnyChartView WakeupGraphView;
    private AnyChartView TTEGraphView;
    private AnyChartView ExerciseGraphView;
    private Cartesian WakeupGraph;
    private Cartesian TTEGraph;
    private Cartesian ExerciseGraph;
    private TextView  AvgWakeupTime;
    private TextView  AvgTTE;
    private TextView MostCommonExercise;
    private RequestQueue queue;
    private ArrayList<String> dates;
    private ArrayList<Integer> alarmTime;
    private ArrayList<Integer> exerciseStart;
    private ArrayList<String> exercise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stats);

        WakeupGraphView = (AnyChartView) findViewById(R.id.WakeupGraph);

        TTEGraphView = (AnyChartView) findViewById(R.id.TTEGraph);

        ExerciseGraphView = (AnyChartView) findViewById(R.id.ExerciseGraph);


        MostCommonExercise = (TextView) findViewById(R.id.MostCommonExercise);
        AvgTTE = (TextView) findViewById(R.id.AvgTTE);
        AvgWakeupTime = (TextView) findViewById(R.id.AvgWakeupTime);

        queue = Volley.newRequestQueue(this);

        Spinner dropdown = findViewById(R.id.date_range_spinner);
        String[] items = new String[]{"Week", "Month", "Year"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                updateGraphs((String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });


        String url ="http://ec2-52-200-107-77.compute-1.amazonaws.com:8080/data";
        JSONObject json = new JSONObject();

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                response -> {
                    parseJson(response);
                }, error -> {
            if (error instanceof TimeoutError) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, "Server timed out try again", duration).show();
                System.out.println("TIMEOUT");
            } else {
                error.printStackTrace();
            }
        } );
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                1000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.

        queue.add(jsonRequest);
    }

    public ArrayList<String> getStringArray(JSONArray json) throws JSONException {
        ArrayList<String> l = new ArrayList<>();
        for(int i = 0; i < json.length(); i++){
            l.add(json.getString(i));
        }
        return l;
    }
    public ArrayList<Integer> getIntArray(JSONArray json) throws JSONException{
        ArrayList<Integer> l = new ArrayList<>();
        for(int i = 0; i < json.length(); i++){
            l.add(json.getInt(i));
        }
        return l;
    }

    public void parseJson(JSONObject json) {

        try {
            dates = getStringArray(json.getJSONArray("date"));
            alarmTime = getIntArray(json.getJSONArray("alarmTime"));
            exerciseStart = getIntArray(json.getJSONArray("exerciseStart"));
            exercise = getStringArray(json.getJSONArray("exerciseChoice"));
        } catch( JSONException e) {}

        updateGraphs("Week");
    }
    public void updateGraphs(String mode) {
        if (dates == null)
            return;
        Calendar calendar = Calendar.getInstance();
        int date_index = calendar.get(Calendar.DAY_OF_YEAR);
        int mode_amount;
        int real_index;
        System.out.println(mode);
        if (mode == "Week")
            mode_amount = 7;
        else if (mode == "Month")
            mode_amount = 30;
        else mode_amount = 365;
        System.out.println(mode_amount);
        System.out.println(date_index);
        String[] d = new String[mode_amount];
        int[] tte = new int[mode_amount];
        int[] wakeup = new int[mode_amount];
        int averageWakeup = 0;
        int averageTTE = 0;

        int[] exercise_count = new int[3];
        String most_common;
        String curr_ex;

        for (int i = date_index; i < date_index + mode_amount; i++) {
            real_index = i % 365;

            d[i- date_index] = dates.get(real_index);
            tte[i- date_index] = exerciseStart.get(real_index);
            wakeup[i-date_index] = alarmTime.get(real_index);
            averageWakeup += alarmTime.get(real_index);
            averageTTE += exerciseStart.get(real_index);

            curr_ex = exercise.get(real_index);
            if (curr_ex.equals("JumpingJack"))
                exercise_count[0] += 1;
            else if (curr_ex.equals("Squat"))
                exercise_count[1] += 1;
            else
                exercise_count[2] += 1;

        }

        if (exercise_count[0] >= exercise_count[1] && exercise_count[0] >= exercise_count[2])
            most_common = "Jumping Jacks";
        else if (exercise_count[1] >= exercise_count[0] && exercise_count[1] >= exercise_count[2])
            most_common = "Squats";
        else most_common = "Twists";

        MostCommonExercise.setText("Most common exercise is: " + most_common);
        AvgTTE.setText("Average Time to Exercise: " + Float.toString(averageTTE/mode_amount));
        AvgWakeupTime.setText("Average Wakeup Time: "+ Float.toString(averageWakeup / mode_amount));

        List<DataEntry> data = new ArrayList<>();
        if (ExerciseGraph == null) {
            APIlib.getInstance().setActiveAnyChartView(ExerciseGraphView);
            ExerciseGraph = getBarGraph(exercise_count);
            ExerciseGraphView.setChart(ExerciseGraph);

            APIlib.getInstance().setActiveAnyChartView(TTEGraphView);
            TTEGraph = getLineGraph(d, tte, "Time to Exercise", "Time (minutes)");
            TTEGraphView.setChart(TTEGraph);

            APIlib.getInstance().setActiveAnyChartView(WakeupGraphView);
            WakeupGraph = getLineGraph(d, wakeup, "Wake Up Time", "Time (hours)");
            WakeupGraphView.setChart(WakeupGraph);
        } else {
            System.out.println("I'm right here");
            data.add(new ValueDataEntry("Jumping Jacks", exercise_count[0]));
            data.add(new ValueDataEntry("Squats", exercise_count[1]));
            data.add(new ValueDataEntry("Twists", exercise_count[2]));
            APIlib.getInstance().setActiveAnyChartView(ExerciseGraphView);
            ExerciseGraph.data(data);

            data = new ArrayList<>();
            for (int i = 0; i < d.length; i++) {
                data.add(new ValueDataEntry(d[i], tte[i]));
            }
            APIlib.getInstance().setActiveAnyChartView(TTEGraphView);
            TTEGraph.data(data);

            data = new ArrayList<>();
            for (int i = 0; i < d.length; i++) {
                data.add(new ValueDataEntry(d[i], wakeup[i]));
            }
            APIlib.getInstance().setActiveAnyChartView(WakeupGraphView);
            WakeupGraph.data(data);
        }

    }


    public Cartesian getBarGraph(int[] exercises){
        Cartesian cartesian = AnyChart.column();
        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Jumping Jacks", exercises[0]));
        data.add(new ValueDataEntry("Squats", exercises[1]));
        data.add(new ValueDataEntry("Twists", exercises[2]));

        Column column = cartesian.column(data);

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d)
                .format("{%Value}{groupsSeparator: }");

        cartesian.animation(true);
        cartesian.title("Exercise Choices");

        cartesian.yScale().minimum(0d);

        //cartesian.yAxis(0).labels().format("{%Value}");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title("Exercise");
        cartesian.yAxis(0).title("Occurrences");
        return cartesian;
    }

    public Cartesian getLineGraph(String[] k, int[] v, String title, String ytitle) {
        Cartesian cartesian = AnyChart.line();
        List<DataEntry> data = new ArrayList<>();
        for (int i = 0; i < k.length; i++) {
            data.add(new ValueDataEntry(k[i], v[i]));
        }

        cartesian.title(title);
        cartesian.animation(true);
        cartesian.line(data);
        cartesian.xAxis(0).title("Date");
        cartesian.yAxis(0).title(ytitle);
        return cartesian;
    }
}