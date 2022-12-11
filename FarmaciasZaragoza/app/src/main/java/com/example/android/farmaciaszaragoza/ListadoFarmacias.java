package com.example.android.farmaciaszaragoza;

import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.farmaciaszaragoza.adapter.FarmaciaAdapter;
import com.example.android.farmaciaszaragoza.base.Farmacia;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ListadoFarmacias extends Activity implements AdapterView.OnItemClickListener {

    private ListView lista;
    private ArrayList<Farmacia> listaFarmacias;
    private FarmaciaAdapter adapter;

    private static final String URL = "http://www.zaragoza.es/georref/json/hilo/farmacias_Equipamiento";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_farmacias);

        lista = (ListView) findViewById(R.id.lvFarmacias);
        lista.setOnItemClickListener(this);
        listaFarmacias = new ArrayList<Farmacia>();
        adapter = new FarmaciaAdapter(this, R.layout.fila, listaFarmacias);
        lista.setAdapter(adapter);

        cargarRestaurantes();
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    private class TareaDescargaDatos extends AsyncTask<String, Void, Void> {

        private boolean error = false;

        // Este método no puede acceder a la interfaz
        @Override
        protected Void doInBackground(String... urls) {

            InputStream is = null;
            String resultado = null;
            JSONObject json = null;
            JSONArray jsonArray = null;

            try {
                HttpClient clienteHttp = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(urls[0]);
                HttpResponse respuesta = clienteHttp.execute(httpPost);
                HttpEntity entity = respuesta.getEntity();
                is = entity.getContent();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String linea = null;

                while ((linea = br.readLine()) != null)
                    sb.append(linea + "\n");

                is.close();
                resultado = sb.toString();

                json = new JSONObject(resultado);
                jsonArray = json.getJSONArray("features");

                String nombre = null;
                String descripcion = null;
                String categoria = null;
                String link = null;
                String coordenadas = null;
                Farmacia farmacia = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    nombre = jsonArray.getJSONObject(i).getJSONObject("properties").getString("title");
                    descripcion = jsonArray.getJSONObject(i).getJSONObject("properties").getString("description");
                    categoria = jsonArray.getJSONObject(i).getJSONObject("properties").getString("category");
                    link = jsonArray.getJSONObject(i).getJSONObject("properties").getString("link");
                    coordenadas = jsonArray.getJSONObject(i).getJSONObject("geometry").getString("coordinates");
                    coordenadas = coordenadas.substring(1, coordenadas.length() - 1);
                    String latlong[] = coordenadas.split(",");

                    farmacia = new Farmacia();
                    farmacia.setNombre(nombre);
                    farmacia.setDescripcion(descripcion);
                    farmacia.setCategoria(categoria);
                    farmacia.setLink(link);
                    farmacia.setLatitud(Float.parseFloat(latlong[0]));
                    farmacia.setLongitud(Float.parseFloat(latlong[1]));
                    listaFarmacias.add(farmacia);
                }

            } catch (ClientProtocolException cpe) {
                cpe.printStackTrace();
                error = true;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                error = true;
            } catch (JSONException jse) {
                jse.printStackTrace();
                error = true;
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            adapter.clear();
            listaFarmacias = new ArrayList<Farmacia>();
        }

        @Override
        protected void onProgressUpdate(Void... progreso) {
            super.onProgressUpdate(progreso);

            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void resultado) {
            super.onPostExecute(resultado);

            if (error) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_message), Toast.LENGTH_SHORT).show();
                return;
            }

            adapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.datos_message), Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarFarmacias() {

        TareaDescargaDatos tarea = new TareaDescargaDatos();
        tarea.execute(URL);
    }

    public void onItemClick(AdapterView<?> arg0, View view, int posicion, long id) {

        if (posicion == ListView.INVALID_POSITION)
            return;

        Farmacia farmacia = listaFarmacias.get(posicion);

        Intent i = new Intent(this, Mapa.class);
        i.putExtra("latitud", farmacia.getLatitud());
        i.putExtra("longitud", farmacia.getLongitud());
        i.putExtra("nombre", farmacia.getNombre());
        startActivity(i);

    }

}