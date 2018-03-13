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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

public class setting extends AppCompatActivity {

    SharedPreferences sharedPref = null;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        sharedPref = getSharedPreferences("SodexoJunaeb", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        final EditText user = findViewById(R.id.userID);
        final EditText pass = findViewById(R.id.passID);
        final TextView rTokenLabel = findViewById(R.id.token);
        Button ConnectButton = findViewById(R.id.connectionButton);


        ConnectButton.setOnClickListener((View view) -> {
            String sUser = user.getText().toString().replace(".", "").replace("-", "");
            SodexoClient uClient = null;
            try {
                uClient = new SodexoClient(sUser, pass.getText().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert uClient != null;
            if (uClient.login) {
                Map getToken = null;
                try {
                    getToken = uClient.getInfoToken();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Calendar cal = Calendar.getInstance();
                editor.putString("user", sUser);
                editor.putString("pass", pass.getText().toString());
                StringBuilder URLBalance = new StringBuilder();
                URLBalance.append("http://www.becajunaebsodexo.cl/wp-content/plugins/beneficiarios/api.php?action=balance");
                assert getToken != null;
                URLBalance.append("&token=");
                URLBalance.append(getToken.get("token"));
                URLBalance.append("&from=01/");
                URLBalance.append(String.format("%02d", cal.get(Calendar.MONTH) + 1));
                URLBalance.append("/");
                URLBalance.append(cal.get(Calendar.YEAR));
                URLBalance.append("&to=01/");
                URLBalance.append(String.format("%02d", Calendar.MONTH + 2));
                URLBalance.append("/");
                URLBalance.append(cal.get(Calendar.YEAR));
                URLBalance.append("&clientid=1");
                URLBalance.append("&serviceid=15");
                System.out.println(sUser + " - " + URLBalance.toString());
                JSONParser parser = new JSONParser();
                JSONObject Data;
                try {
                    Data = (JSONObject) parser.parse(uClient.ExtractData(URLBalance.toString()));
                    JSONObject ReturnBalance = (JSONObject) Data.get("result");
                    JSONObject Balance = (JSONObject) ReturnBalance.get("return");
                    editor.putString("token", (String) getToken.get("token"));
                    editor.putString("clientid", "1");
                    editor.putString("serviceid", "15");
                    editor.putString("balance", Balance.get("amountBalance").toString());
                    editor.putString("actualDate", cal.getTime().toString());
                    editor.apply();
                    rTokenLabel.setText(getToken.get("token") + ". SALDO: " + Balance.get("amountBalance"));

                } catch (ParseException | NullPointerException | IOException e) {
                    e.printStackTrace();
                }

            }
        });


    }


}
