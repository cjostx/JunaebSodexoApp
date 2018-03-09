package jnet.junaebsodexo;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

public class setting extends AppCompatActivity {

    SharedPreferences sharedPref = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        sharedPref = getSharedPreferences("SodexoJunaeb", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        final EditText user = (EditText) findViewById(R.id.userID);
        final EditText pass = (EditText) findViewById(R.id.passID);
        final TextView rtokenLabel = (TextView) findViewById(R.id.token);
        Button ConnectButton = (Button) findViewById(R.id.connectionButton);

        ConnectButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onClick(View view) {
                SodexoClient uClient = null;
                try {
                    uClient = new SodexoClient(user.getText().toString(), pass.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    if (uClient.login) {
                        Map getToken = null;
                        try {
                            getToken = uClient.getInfoToken();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Calendar cal = Calendar.getInstance();
                        editor.putString("user", user.getText().toString());
                        editor.putString("pass", pass.getText().toString());
                        StringBuffer URLBalance = new StringBuffer();
                        URLBalance.append("http://www.becajunaebsodexo.cl/wp-content/plugins/beneficiarios/api.php?action=balance");
                        URLBalance.append("&token="+getToken.get("token"));
                        URLBalance.append("&from=01/"+ String.format("%02d", cal.get(Calendar.MONTH) +1) +"/"+cal.get(Calendar.YEAR));
                        URLBalance.append("&to=01/"+ String.format("%02d", Calendar.MONTH + 2)  +"/"+cal.get(Calendar.YEAR));
                        URLBalance.append("&clientid=1");
                        URLBalance.append("&serviceid=15");
                        System.out.println(URLBalance.toString());
                        JSONParser parser = new JSONParser();
                        JSONObject Data = null;
                        try {
                        Data = (JSONObject) parser.parse(uClient.ExtractData(URLBalance.toString()));
                        JSONObject ReturnBalance = (JSONObject) Data.get("result");
                        JSONObject Balance = (JSONObject) ReturnBalance.get("return");
                        editor.putString("token", (String) getToken.get("token"));
                        editor.putString("clientid", "1");
                        editor.putString("serviceid", "15");
                        editor.putString("balance", Balance.get("amountBalance").toString());
                        editor.apply();
                        rtokenLabel.setText(getToken.get("token")+ ". SALDO: "+Balance.get("amountBalance"));
                        } catch (ParseException | NullPointerException | IOException e) {
                            e.printStackTrace();
                        }

                    }
            }
        });



    }


}
