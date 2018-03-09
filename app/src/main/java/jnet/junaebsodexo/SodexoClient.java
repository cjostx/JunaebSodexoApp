package jnet.junaebsodexo;

import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class SodexoClient {
    
    public HttpClient client = null;
    public boolean login;
    
    public SodexoClient(String username, String password) throws UnsupportedEncodingException, IOException {
        
        //Obtener Token-Wp-nonce
        Document TokenWP = Jsoup.parse(getContentHTML("http://www.becajunaebsodexo.cl/"));
        Elements TokenAlone = TokenWP.select("script");
        String TokenHTML = "";
            int i = 0;
            boolean noncefound = false;
            for (Element element : TokenAlone ){                
                    for (DataNode node : element.dataNodes()) {
                        if (node.getWholeData().contains("nonce")) {
                        noncefound = true;
                        TokenHTML = node.getWholeData();
                        }
                        
                        if (!noncefound) {
                            i++;
                        }
                    }
            }
        String Token;
        Token = TokenHTML.substring(TokenHTML.indexOf('"')+1);
        Token = Token.substring(0, Token.indexOf('"'));
        Token = new String(Base64.decode(Token, Base64.DEFAULT));



        URI LOGIN_URL = URI.create("http://www.becajunaebsodexo.cl/beneficiarios/"); 
        
        client = new DefaultHttpClient();
        
        HttpPost post = new HttpPost(LOGIN_URL);
        
        String infodb = Base64.encodeToString(Token.getBytes(), Base64.DEFAULT).trim();
        String Time = String.valueOf(System.currentTimeMillis());
        Time = Time.substring(0, Time.length()-3) + "000";
        String info = URLEncoder.encode(infodb, "UTF-8") + Time;
        String enc = sha1(infodb + Time + password).toLowerCase();
        String passwordEnc = Base64.encodeToString(enc.getBytes(), Base64.DEFAULT);

        try 
        {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
                        
            nameValuePairs.add(new BasicNameValuePair("rut", username));
            nameValuePairs.add(new BasicNameValuePair("info", info));
            nameValuePairs.add(new BasicNameValuePair("password", ""));
            nameValuePairs.add(new BasicNameValuePair("passwordEnc", passwordEnc));
            nameValuePairs.add(new BasicNameValuePair("ingresar", "Ingresar"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs);
            entity.setContentType("application/x-www-form-urlencoded");

            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = client.execute(post);

            BufferedReader reader = 
                  new BufferedReader(
                  new InputStreamReader(response.getEntity().getContent()));
            
            String sOutput = "";
            String line;
            while ((line = reader.readLine()) != null) 
            {
                sOutput += line;
            }
            login = sOutput.length() >= 0;
        }
        catch (IOException e) {
            login =false;
        }
    }
    
    public String ExtractData (String sURL) throws IOException {
        URI PROFILE_URL = URI.create(sURL);

        HttpGet get = new HttpGet(PROFILE_URL);
        HttpResponse response;
        String Data = "";
        try 
        {
            response = client.execute(get);
            BufferedReader reader = 
                   new BufferedReader(
                   new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = reader.readLine()) != null) 
            {
                Data += line;
            }
        } 
        catch (ClientProtocolException e) {}  
            return Data;

    }
    
    public Map getInfoToken () throws IOException {
        Document Beneficiarios = Jsoup.parse(ExtractData("http://www.becajunaebsodexo.cl/beneficiarios/?a=balance"));
        Elements ScriptVars = Beneficiarios.getElementsByTag("script");
               
        Map<String, String> Session = new Hashtable<>();
        String res;
        for (Element element : ScriptVars ){
            res= "";
            if (element.data().contains("var token=")) {
                res = element.data().substring(element.data().indexOf("=")+2);
                res = res.substring(0, res.length()-1);
                Session.put("token", res);
            } else if (element.data().contains("var clientid=")) { // 1
                res = element.data().substring(element.data().indexOf("=")+2);
                res = res.substring(0, res.length()-1);
                Session.put("clientid", "1");
            } else if (element.data().contains("var serviceid=")) { // 15
                res = element.data().substring(element.data().indexOf("=")+2);
                res = res.substring(0, res.length()-1);
                Session.put("serviceid", "15");
            } else if (element.data().contains("var userid=")) {
                //userid
                res = element.data().substring(element.data().indexOf("=")+2);
                Session.put("userid", res.substring(0, res.indexOf(";")-1));
                //user_mobile
                res = element.data().substring(element.data().indexOf("var user_mobile")+19);
                Session.put("user_mobile", res.substring(0, res.indexOf(";")-1));
                //Email
                res = element.data().substring(element.data().indexOf("var user_email")+18);
                Session.put("user_email", res.substring(0, res.indexOf(";")-1));
            }
        }        
        return Session;
    }
    
    public String sha1(String input) {
        String sha1 = null;
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(input.getBytes("UTF-8"), 0, input.length());
            sha1 = new BigInteger(1 ,msdDigest.digest()).toString(16);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            
        }
        return sha1;
    }
    /*public String base64encode(String input) {
        String b64;
        byte[] bytesEncoded = Base64.encodeBase64(input.getBytes());
        b64 = new String(bytesEncoded);
        return b64;
    }
    
    public String base64decode(String input) {
        String b64;
        byte[] bytesEncoded = Base64.decodeBase64(input);
        b64 = new String(bytesEncoded);
        return b64;
    }*/

    public static String getContentHTML(String URL) throws IOException {
        URL url = new URL(URL);
        URLConnection uc = url.openConnection();
        uc.connect();
        String contenido;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()))) {
            String inputLine;
            contenido = "";
            while ((inputLine = in.readLine()) != null) {
                contenido += inputLine;
            }
        }
        return contenido;
    }
        
}