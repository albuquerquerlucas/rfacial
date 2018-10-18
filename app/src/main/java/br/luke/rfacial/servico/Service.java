package br.luke.rfacial.servico;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

public class Service {

    public static final String URL_CONSULTA = "";
    public static final String URL_VERIFICACAO = "";

    public HashMap<String, String> enviaRequisicaoPost(String urlRequest, String json){

        HashMap<String, String> map = null;

        try{
            URL url = new URL(urlRequest);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setConnectTimeout(50000);
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("POST");

            OutputStream os = urlConnection.getOutputStream();
            os.write(json.getBytes("UTF-8"));
            os.flush();
            os.close();

            BufferedReader br = null;
            String msgRetorno = "";
            int responseCode = urlConnection.getResponseCode();

            if(responseCode == 200){
                br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            }else{
                br = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
            }

            StringBuilder sBuilder = new StringBuilder();
            String output = "";
            while((output = br.readLine()) != null){
                sBuilder.append(output);
            }
            msgRetorno = sBuilder.toString();

            map = new HashMap<String, String>();
            map.put("status", responseCode + "");
            map.put("mensagem", msgRetorno);

            if (urlConnection != null) {urlConnection.disconnect();}
            urlConnection.disconnect();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return map;
    }
}
