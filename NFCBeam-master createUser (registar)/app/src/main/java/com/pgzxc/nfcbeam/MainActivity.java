package com.pgzxc.nfcbeam;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.nfc.NdefRecord.createExternal;
import static android.nfc.NfcAdapter.getDefaultAdapter;


public class MainActivity extends AppCompatActivity implements
        NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {
    public String sendThisToServer = "";
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private scannedIdEntry idEntry=new scannedIdEntry();
    String response="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNfcAdapter = getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()), 0);
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        Log.d("message", "complete");
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        try{
            byte[] data=idEntry.generateJsonStr().getBytes("UTF-8");
            NdefRecord record=createExternal ("shen.com","json",data);
            NdefMessage ndefMessage = new NdefMessage(record);
            return ndefMessage;
        }
        catch (UnsupportedEncodingException ignored){ }
        return null;

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null,
                    null);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        processIntent(intent);
    }

    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent
                .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        try {
            String jsonStr=new String(msg.getRecords()[0].getPayload(),"UTF-8");
            Toast.makeText(this,jsonStr , Toast.LENGTH_LONG).show();
            sendThisToServer = jsonStr;
            HttpTask httpTask=new HttpTask();
            httpTask.execute();
        }catch(UnsupportedEncodingException ignored){}
    }

    public void sendHttpReq(View view) {
        EditText editText1 = findViewById(R.id.editText);
        EditText editText2 = findViewById(R.id.editText2);
        String name = editText1.getText().toString();
        String email = editText2.getText().toString();
        sendThisToServer = "{\"name\":\"" + name + "\",\"email\":\"" + email + "\"}";
        HttpTask httpTask=new HttpTask();
        httpTask.execute();
    }

    private class HttpTask extends AsyncTask<String, Integer, String> {
        private String res="";
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://protected-u.appspot.com/createuser?data=" + sendThisToServer);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                conn.setConnectTimeout(6*1000);
                conn.setReadTimeout(6 * 1000);
                InputStream in = conn.getInputStream();
                res=readStream(in);
                conn.disconnect();
                return new String(res);
            } catch (MalformedURLException ignore) {
            } catch (IOException ignore) { }
            return null;
        }


        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            TextView textView = findViewById(R.id.http_res);
            textView.setText(res);
        }
        // Converting InputStream to String
        private String readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuffer response = new StringBuffer();
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response.toString();
        }
    }

}



