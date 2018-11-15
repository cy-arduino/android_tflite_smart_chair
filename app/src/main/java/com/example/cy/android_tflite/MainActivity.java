package com.example.cy.android_tflite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import org.tensorflow.lite.Interpreter;

/*
Gama , Gama_正坐_122525.332.jpg , 正坐 , 1 , 10955 , 5022 , 10736 , 11032 , 8167 , 9912 , 8848 , 10091 , 14687 , 13471 , 15536
Gama , Gama_左傾斜_122531.789.jpg , 左傾斜 , 2 , 13453 , 17 , 11520 , 8992 , 9008 , 7779 , 10044 , 4804 , 14933 , 10104 , 15552
Gama , Gama_右傾斜_122545.448.jpg , 右傾斜 , 3 , 2154 , 8728 , 9096 , 11800 , 1626 , 10259 , 2906 , 11244 , 12472 , 13575 , 15536
Gama , Gama_左翹腳_122550.332.jpg , 左翹腳 , 4 , 12363 , 15 , 12192 , 10360 , 25 , 10347 , 12286 , 9207 , 14729 , 11573 , 15544
Gama , Gama_右翹腳_122554.720.jpg , 右翹腳 , 5 , 11084 , 10640 , 9104 , 12328 , 11267 , 7374 , 7302 , 12447 , 11530 , 13098 , 15536
Gama , Gama_懶人斜躺_122601.982.jpg , 懶人斜躺 , 6 , 13997 , 9587 , 7152 , 7648 , 10133 , 11637 , 7651 , 11651 , 9241 , 8134 , 15536
Gama , Gama_前傾_122608.820.jpg , 前傾 , 7 , 9986 , 15 , 7176 , 8272 , 8825 , 10304 , 40 , 5940 , 9502 , 7929 , 4448
*/
public class MainActivity extends AppCompatActivity {

    private Interpreter tflite;
    private List<String> labelList;
    //private ByteBuffer inputData = null;
    private float[][] labelProbArray = null;
    private ByteBuffer intputData = null;


    private final String TAG = "android_rflite";

    private final String ModelFile = "Keras_CY_smart_chair.tflite";
    private final String LabelFile = "Keras_CY_smart_chair.tflite.labels";

    private final String TestDataFile = "Test_X";
    private final String TestLabelFile = "Test_Y";
    private List<Integer> TestLabel;
    private List<List<Integer>> TestData;

    private TextView outputText;
    private Button startBtn;
    private Button testAllBtn;
    private EditText et_s0, et_s1, et_s2, et_s3, et_s4, et_s5, et_s6, et_s7, et_s8, et_s9, et_s10;
    private EditText et_normal_s0, et_normal_s1, et_normal_s2, et_normal_s3, et_normal_s4, et_normal_s5, et_normal_s6, et_normal_s7, et_normal_s8, et_normal_s9, et_normal_s10;

    /** Memory-map the model file in Assets. */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(ModelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        Log.d(TAG, "loadModelFile finished");
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /** Reads label list from Assets. */
    private List<String> loadLabelList(Activity activity) throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(activity.getAssets().open(LabelFile)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();

        Log.d(TAG, "loadLabelList finished");
        return labelList;
    }

    /** Reads test data from Assets. */
    private List<Integer> loadTestLabel(Activity activity) throws IOException {
        List<Integer> labelList = new ArrayList<Integer>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(activity.getAssets().open(TestLabelFile)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(Integer.parseInt(line));
        }
        reader.close();

        Log.d(TAG, "loadTestLabel finished");
        return labelList;
    }
    private List<List<Integer>> loadTestData(Activity activity) throws IOException {
        List<List<Integer>> dataList = new ArrayList<List<Integer>>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(activity.getAssets().open(TestDataFile)));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] datas = line.split(",") ;
            List<Integer> l = new ArrayList<Integer>();
            for(int i=0; i<datas.length; i++){
                l.add(Integer.parseInt(datas[i]));
            }

            dataList.add(l);
        }
        reader.close();

        Log.d(TAG, "loadTestData finished");
        return dataList;
    }

    private void setupWidget(){
        et_s0 = findViewById(R.id.et_s0);
        et_s1 = findViewById(R.id.et_s1);
        et_s2 = findViewById(R.id.et_s2);
        et_s3 = findViewById(R.id.et_s3);
        et_s4 = findViewById(R.id.et_s4);
        et_s5 = findViewById(R.id.et_s5);
        et_s6 = findViewById(R.id.et_s6);
        et_s7 = findViewById(R.id.et_s7);
        et_s8 = findViewById(R.id.et_s8);
        et_s9 = findViewById(R.id.et_s9);
        et_s10 = findViewById(R.id.et_s10);

        et_normal_s0 = findViewById(R.id.et_normal_s0);
        et_normal_s1 = findViewById(R.id.et_normal_s1);
        et_normal_s2 = findViewById(R.id.et_normal_s2);
        et_normal_s3 = findViewById(R.id.et_normal_s3);
        et_normal_s4 = findViewById(R.id.et_normal_s4);
        et_normal_s5 = findViewById(R.id.et_normal_s5);
        et_normal_s6 = findViewById(R.id.et_normal_s6);
        et_normal_s7 = findViewById(R.id.et_normal_s7);
        et_normal_s8 = findViewById(R.id.et_normal_s8);
        et_normal_s9 = findViewById(R.id.et_normal_s9);
        et_normal_s10 = findViewById(R.id.et_normal_s10);

        outputText = findViewById(R.id.tv_output);
        startBtn = findViewById(R.id.btn_start);
        testAllBtn = findViewById(R.id.btnTestAll);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "startBtn click!");

                intputData.rewind();
                intputData.putFloat(Float.valueOf(et_s0.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_s1.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_s2.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_s3.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_s4.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_s5.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_s6.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_s7.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_s8.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_s9.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_s10.getText().toString()) / 65536);

                intputData.putFloat(Float.valueOf(et_normal_s0.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_normal_s1.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_normal_s2.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_normal_s3.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_normal_s4.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_normal_s5.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_normal_s6.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_normal_s7.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_normal_s8.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_normal_s9.getText().toString()) / 65536);
                intputData.putFloat(Float.valueOf(et_normal_s10.getText().toString()) / 65536);

                String outputStr = "";

                long startTime = SystemClock.uptimeMillis();
                tflite.run(intputData, labelProbArray);
                long endTime = SystemClock.uptimeMillis();
                Log.d(TAG, "Time cost to predict : " + Long.toString(endTime - startTime) + " ms");
                outputStr =  "Time cost to predict : " + Long.toString(endTime - startTime) + " ms";

                int maxProbIdx = 0;
                float prob = 0;
                for(int i = 0; i<labelList.size(); i++){
                    if(labelProbArray[0][i] > prob){
                        prob = labelProbArray[0][i];
                        maxProbIdx = i;
                    }
                    Log.d(TAG, "pose " + labelList.get(i) + ": " +Float.toString(labelProbArray[0][i]));
                }
                outputStr += "\npredict: " + labelList.get(maxProbIdx) + " - " + Float.toString(labelProbArray[0][maxProbIdx]);

                outputText.setText(outputStr);
            }
        });

        testAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d(TAG, "testAllBtn click!");

                int fail = 0;
                int pass = 0;

                long startTime = SystemClock.uptimeMillis();
                for(int i: TestLabel){
                    intputData.rewind();
                    List<Integer> d = TestData.get(i);
                    for(int j=0; j < d.size(); j++){
                        intputData.putFloat(Float.valueOf(d.get(j)) / 65536);
                    }

                    tflite.run(intputData, labelProbArray);

                    int maxProbIdx = 0;
                    float prob = 0;
                    for(int k = 0; k<labelList.size(); k++){
                        if(labelProbArray[0][k] > prob){
                            prob = labelProbArray[0][k];
                            maxProbIdx = i;
                        }
                        //Log.d(TAG, "pose " + labelList.get(i) + ": " +Float.toString(labelProbArray[0][i]));
                    }

                    Log.d(TAG, "label:" + i + ", predict: " + labelList.get(maxProbIdx) + "("+ maxProbIdx + ") - " + Float.toString(labelProbArray[0][maxProbIdx]) + "\n");
                    if(i == maxProbIdx)
                        pass++;
                    else
                        fail++;
                }
                long endTime = SystemClock.uptimeMillis();
                Log.d(TAG, "pass: " + pass + ", fail: " + fail + ", total " + Long.toString(endTime - startTime) + "ms\n");
                String outputStr = "pass: " + pass + ", fail: " + fail + ", total " + Long.toString(endTime - startTime) + "ms\n";
                outputText.setText(outputStr);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupWidget();

        try{
            tflite = new Interpreter(loadModelFile(this));
            labelList = loadLabelList(this);
            labelProbArray = new float[1][labelList.size()];
            intputData = ByteBuffer.allocateDirect(22*4); //11+11 sensors, float: 4bytes
            intputData.order(ByteOrder.nativeOrder());

            TestLabel = loadTestLabel(this);
            TestData = loadTestData(this);

            //Log.d(TAG, "labelList.size=" + labelList.size());


        }catch(Exception e){
            Log.e(TAG, "got exception: " + e);
        }

    }
}

