package br.luke.rfacial.servico;

import android.content.Context;
import android.net.ConnectivityManager;

public class Connection {

    private Context context;

    public Connection(Context context) {
        this.context = context;
    }

    public boolean varifyConnections(){
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {
            conectado = false;
        }
        return conectado;
    }
}
