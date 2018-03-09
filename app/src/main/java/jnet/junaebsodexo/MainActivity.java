package jnet.junaebsodexo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static jnet.junaebsodexo.SodexoClient.getContentHTML;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPref = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sharedPref = getSharedPreferences("SodexoJunaeb", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();


        final TextView rLabelAmount = (TextView) findViewById(R.id.amountLabel);
        final TextView rUser = (TextView) findViewById(R.id.user_rut_label);

        rLabelAmount.setText("$"+sharedPref.getString("balance", "000000"));
        String User = sharedPref.getString("user", "123456789");
        User = User.substring(0, User.length()-1)+"-"+User.substring(User.length()-1);
        rUser.setText(User);

        final FloatingActionButton settingB = (FloatingActionButton) findViewById(R.id.settingButton);
        settingB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), setting.class);
                startActivity(i);
            }
        });

        FloatingActionButton refresh = (FloatingActionButton) findViewById(R.id.refreshButton);
        refresh.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View view) {

                Calendar cal = Calendar.getInstance();

                StringBuilder URLBalance = new StringBuilder();
                URLBalance.append("http://www.becajunaebsodexo.cl/wp-content/plugins/beneficiarios/api.php?action=balance");
                URLBalance.append("&token="+sharedPref.getString("token", null));
                URLBalance.append("&from=01/"+ String.format("%02d", cal.get(Calendar.MONTH)+1) +"/"+cal.get(Calendar.YEAR));
                URLBalance.append("&to=01/"+ String.format("%02d", Calendar.MONTH + 2)  +"/"+cal.get(Calendar.YEAR));
                URLBalance.append("&clientid=1");
                URLBalance.append("&serviceid=15");
                JSONParser parser = new JSONParser();
                System.out.println(URLBalance.toString());
                JSONObject Data = null;
                try {
                    Data = (JSONObject) parser.parse(getContentHTML(URLBalance.toString()));
                    JSONObject ReturnBalance = (JSONObject) Data.get("result");
                    JSONObject Balance = (JSONObject) ReturnBalance.get("return");
                    editor.putString("balance", Balance.get("amountBalance").toString());
                    editor.apply();
                    rLabelAmount.setText("$"+sharedPref.getString("balance", "000000"));
                    String User = sharedPref.getString("user", "123456789");
                    User = User.substring(0, User.length()-1)+"-"+User.substring(User.length()-1);
                    rUser.setText(User);

                } catch (ParseException | IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

    }



}
