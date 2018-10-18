package br.luke.rfacial.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import br.luke.rfacial.R;
import br.luke.rfacial.entity.Verificacao;
import br.luke.rfacial.servico.Service;

public class VerificacaoAct extends AppCompatActivity {

    private ImageView imgFoto;
    private Button btnAction;
    private boolean status = true;
    private File tempFile;
    private SimpleDateFormat dataFormatada = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
    private String caminhoFoto;
    private String cdPessoa;
    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificacao);

        Intent it = getIntent();
        cdPessoa = it.getStringExtra("cd_pessoa");
        initUi();
    }

    @Override
    protected void onResume() {
        super.onResume();

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status){
                    capturaImagem();
                }else{
                    verificarBioFacial(cdPessoa);
                }
            }
        });
    }

    private void initUi(){
        imgFoto = (ImageView) findViewById(R.id.img_foto);
        btnAction = (Button) findViewById(R.id.btn_action);
    }

    private void capturaImagem(){

        String diretorio = Environment.getExternalStorageDirectory() + "/rfacial/";
        File file = new File(diretorio);

        if(!file.exists()){
            file.mkdirs();
        }

        String dataArquivo = dataFormatada.format(new Date());
        tempFile = new File(file, dataArquivo + ".png");
        Uri temp = FileProvider.getUriForFile(this, "br.luke.rfacial.fileprovider", tempFile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        allowPermissionProvider(temp, cameraIntent);

        if(cameraIntent.resolveActivity(this.getPackageManager()) != null){
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, temp);
            startActivityForResult(cameraIntent, 1);
        }
    }

    // Permissão necessária em tempo de execussão para utilização da câmera e armazenamento Android 4.4 >=...
    private void allowPermissionProvider(Uri uri, Intent intent){
        List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            switch (resultCode){
                case Activity.RESULT_OK:
                    if(tempFile.exists()){
                        caminhoFoto = tempFile.toString();
                        imgFoto.setImageDrawable(redimsImage(tempFile.toString()));
                        btnAction.setText("VALIDAR");
                        status = false;
                    }
                break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(getApplicationContext(), "Cancelado..", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    public Drawable redimsImage(String imgFile){
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        try {
            bm = BitmapFactory.decodeStream(new FileInputStream(imgFile.toString()), null, options);
            FileOutputStream out = new FileOutputStream(imgFile);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Drawable drawable = new BitmapDrawable(getResources(), bm);

        return drawable;
    }

    private void verificarBioFacial(String cdPessoa){
        new VerificarTask().execute(cdPessoa, caminhoFoto);
    }

    public class VerificarTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callLoader("Aguarde!", "Validando reconhecimento facial.");
        }

        @Override
        protected String doInBackground(String... params) {

            String resposta = null;

            Gson json = new Gson();
            HashMap<String, String> mapRetorno = new Service()
                    .enviaRequisicaoPost(
                            Service.URL_VERIFICACAO,
                            json.toJson(new Verificacao(params[0], new String[]{redimsEConverteBase64(params[1])})));

            if(mapRetorno != null){
                try{
                    JSONObject objectJson = new JSONObject(mapRetorno.get("mensagem"));
                    resposta = objectJson.getString("cod");
                }catch (JSONException e){
                    e.getStackTrace();
                }
            }

            return resposta;
        }

        @Override
        protected void onPostExecute(String strRetorno) {
            super.onPostExecute(strRetorno);
            loader.dismiss();
            deleteFiles(caminhoFoto);
            if(strRetorno != null && !strRetorno.equals("")){
                btnAction.setText("CAPTURAR");
                status = true;
                if(strRetorno.equals("2")){
                    Toast.makeText(getApplicationContext(), "Validada com Sucesso!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Foto não validada", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String redimsEConverteBase64(String filePath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap novoBitmap = BitmapFactory.decodeFile(filePath, options);

        return encodeTobase64(novoBitmap);
    }

    private static String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return reparoBase64(imageEncoded);
    }

    public static String reparoBase64(String imgGerada){
        String fotoReparada = imgGerada.replace("\n", "");
        return fotoReparada;
    }

    public static void deleteFiles(String path) {

        File file = new File(path);

        if (file.exists()) {
            String deleteCmd = "rm -r " + path;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) {Log.e("ERR", e.getMessage()); }
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
