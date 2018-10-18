package br.luke.rfacial.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import br.luke.rfacial.R;
import br.luke.rfacial.entity.Consulta;
import br.luke.rfacial.servico.Connection;
import br.luke.rfacial.servico.Service;

public class ConsultaAct extends AppCompatActivity {

    private EditText edtCdPessoa;
    private Connection conn;
    private String cdPessoa;
    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta);

        getPermissions();
        this.conn = new Connection(this);

        edtCdPessoa = (EditText) findViewById(R.id.edt_cd_pessoa);
    }

    public void consultar(View view) {

        cdPessoa = edtCdPessoa.getText().toString();

        if(cdPessoa.equals("")){
            Toast.makeText(this, "CD_PESSOA não pode ser vazio", Toast.LENGTH_SHORT).show();
        }else{
            new ConsultarTask().execute(cdPessoa);
        }

    }

    public class ConsultarTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callLoader("Aguarde!", "Consultando se possui imagem cadastrada.");
        }

        @Override
        protected String doInBackground(String... params) {

            String retorno = null;

            Gson json = new Gson();
            HashMap<String, String> mapRetorno = new Service().enviaRequisicaoPost(
                    Service.URL_CONSULTA,
                    json.toJson(new Consulta(params[0], "N", "AndroidApp")));

            if(mapRetorno != null){
                try{
                    JSONObject objectJson = new JSONObject(mapRetorno.get("mensagem"));
                    retorno = objectJson.getString("cod");
                }catch (JSONException e){
                    e.getStackTrace();
                }
            }

            return retorno;
        }

        @Override
        protected void onPostExecute(String strResposta) {
            super.onPostExecute(strResposta);
            if(strResposta != null && !strResposta.equals("")){
                loader.dismiss();
                if(strResposta.equals("4")){
                    Toast.makeText(getApplicationContext(), "Usuário não possui foto cadastrada", Toast.LENGTH_SHORT).show();
                }else{
                    startActivity(new Intent(ConsultaAct.this, VerificacaoAct.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .putExtra("cd_pessoa", cdPessoa)
                    );
                    Toast.makeText(getApplicationContext(), "Imagem Encontrada!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void getPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    public void callLoader(String title, String msg) {
        loader = new ProgressDialog(this);
        loader.setTitle(title);
        loader.setMessage(msg);
        loader.setCancelable(false);
        loader.show();
    }
}
