package jnet.junaebsodexo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Calendar;

import static jnet.junaebsodexo.SodexoClient.getContentHTML;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPref = null;


    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sharedPref = getSharedPreferences("SodexoJunaeb", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();


        final TextView rLabelAmount = findViewById(R.id.amountLabel);
        final TextView rUser = findViewById(R.id.user_rut_label);
        final TextView rDate = findViewById(R.id.dateRefresh);

        rLabelAmount.setText("$"+sharedPref.getString("balance", "000000"));
        String User = sharedPref.getString("user", "123456789");
        rDate.setText(sharedPref.getString("actualDate", ""));
        User = User.substring(0, User.length()-1)+"-"+User.substring(User.length()-1);
        rUser.setText(User);

        final FloatingActionButton settingB = findViewById(R.id.settingButton);
        settingB.setOnClickListener(view -> {
            Intent i = new Intent(view.getContext(), setting.class);
            startActivity(i);
        });

        FloatingActionButton refresh = findViewById(R.id.refreshButton);
        refresh.setOnClickListener(view -> {

            Calendar cal = Calendar.getInstance();

            StringBuilder URLBalance = new StringBuilder();
            URLBalance.append("http://www.becajunaebsodexo.cl/wp-content/plugins/beneficiarios/api.php?action=balance");
            URLBalance.append("&token=");
            URLBalance.append(sharedPref.getString("token", null));
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

            JSONParser parser = new JSONParser();
            System.out.println(URLBalance.toString());
            JSONObject Data;
            try {
                Data = (JSONObject) parser.parse(getContentHTML(URLBalance.toString()));
                JSONObject ReturnBalance = (JSONObject) Data.get("result");
                JSONObject Balance = (JSONObject) ReturnBalance.get("return");
                editor.putString("balance", Balance.get("amountBalance").toString());
                editor.putString("actualDate", cal.getTime().toString());
                editor.apply();
                rLabelAmount.setText("$" + sharedPref.getString("balance", "000000"));
                rDate.setText(sharedPref.getString("actualDate", ""));
                String User1 = sharedPref.getString("user", "123456789");
                User1 = User1.substring(0, User1.length() - 1) + "-" + User1.substring(User1.length() - 1);
                rUser.setText(User1);

            } catch (ParseException | IOException | NullPointerException e) {
                e.printStackTrace();
            }
        });

    }



}
