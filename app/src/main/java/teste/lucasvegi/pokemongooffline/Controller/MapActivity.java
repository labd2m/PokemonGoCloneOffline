package teste.lucasvegi.pokemongooffline.Controller;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import teste.lucasvegi.pokemongooffline.Model.Aparecimento;
import teste.lucasvegi.pokemongooffline.Model.ControladoraFachadaSingleton;
import teste.lucasvegi.pokemongooffline.R;

public class MapActivity extends FragmentActivity implements LocationListener, GoogleMap.OnMarkerClickListener,Runnable {
    public GoogleMap map;
    public LocationManager lm;
    public Criteria criteria;
    public String provider;

    public int TEMPO_REQUISICAO_LATLONG = 5000;
    public int DISTANCIA_MIN_METROS = 0;
    public int intervaloEntreSorteiosEmMinutos = 1;     //USADO PARA DETERMINAR INTERVALO DE TEMPO ENTRE SORTEIOS DE POKEMON
    public double distanciaMinimaParaBatalhar = 150.0;  //USADO PARA DETERMINAR A DISTÂNCIA MINIMA NECESSÁRIA PARA BATALHAR COM UM POKEMON

    public Marker eu;
    public WebView webViewLoader;
    public boolean primeiraPosicao = true;
    public boolean continuaSorteando = true;

    //usado no onActivityResult
    public final static int MENU_PERFIL = 1;
    public final static int MENU_MAPA = 2;
    public final static int MENU_POKEDEX = 3;

    public List<Aparecimento> aparecimentos;
    public Map<Marker,Aparecimento> aparecimentoMap; //dicionário para ajudar no momento de clicar em pontos

    public Marker LatMin;
    public Marker LatMax;
    public Marker LongMin;
    public Marker LongMax;

    public Location posicaoAtual;

    MediaPlayer mp; //música do mapa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //configura o mapa
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa)).getMap();
        map.setMyLocationEnabled(true);
        map.setBuildingsEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.setOnMarkerClickListener(this); //marcadores clicaveis

        configuraCriterioLocation();

        //aloca lista e Map
        aparecimentos = new ArrayList<Aparecimento>();
        aparecimentoMap = new HashMap<Marker, Aparecimento>();

        //Configura web view loader sorteio de pokemon
        webViewLoader = (WebView) findViewById(R.id.imgLoader);
        webViewLoader.loadUrl("file:///android_asset/loading.gif");
        webViewLoader.setBackgroundColor(Color.TRANSPARENT);
        webViewLoader.setVisibility(View.GONE);

        //Escolhe imagem do botão de perfil de acordo com o sexo do usuário
        ImageButton imgPerfil = (ImageButton) findViewById(R.id.botaoPerfil);
        if(ControladoraFachadaSingleton.getInstance().getUsuario().getSexo().equals("M"))
            imgPerfil.setImageResource(R.drawable.male_profile);
        else
            imgPerfil.setImageResource(R.drawable.female_profile);

        //Configura o nome do usuário abaixo do botão de perfil
        TextView txtNomeUser = (TextView) findViewById(R.id.txtNomeUser);
        txtNomeUser.setText(ControladoraFachadaSingleton.getInstance().getUsuario().getLogin());
    }

    @Override
    protected void onStart() {
        super.onStart();

        iniciaGeolocation(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            //pausa a música
            mp.pause();
        }catch (Exception e){
            Log.e("MAPA", "ERRO: " + e.getMessage());
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            //reinicia a música
            mp.start();
        }catch (Exception e){
            Log.e("MAPA", "ERRO: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        lm.removeUpdates(this);
        Log.d("PROVEDOR", "Provedor " + provider + " parado!");

        continuaSorteando = false; //para a thread de sorteio

        try {
            //entrega o recurso de música para o sistema
            mp.release();
        }catch (Exception e){
            Log.e("MAPA", "ERRO: " + e.getMessage());
        }

        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        posicaoAtual = location;

        LatLng personagem = new LatLng(location.getLatitude(),location.getLongitude());

        //Remove o personagem para atualizar a sua posição
        if(eu != null) {
            eu.remove();
        }

        //Escolhe imagem do personagem de acordo com o sexo
        if(ControladoraFachadaSingleton.getInstance().getUsuario().getSexo().equals("M"))
            eu = map.addMarker(new MarkerOptions().position(personagem).icon(BitmapDescriptorFactory.fromResource(R.drawable.male)));
        else
            eu = map.addMarker(new MarkerOptions().position(personagem).icon(BitmapDescriptorFactory.fromResource(R.drawable.female)));

        //centraliza a camera na primeira vez que obtiver a posição
        if(primeiraPosicao) {
            //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(personagem, 18);

            CameraUpdate c = CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(personagem)
                            .tilt(60)
                            .zoom(18)
                            .build());

            map.animateCamera(c);
            primeiraPosicao = false;

            //inicia a thread de sorteio de pokemon
            new Thread(this).start();

            //inicia a música do mapa - rota 1 quando obtem localização do usuário
            mp = MediaPlayer.create(getBaseContext(), R.raw.tema_rota_1);
            mp.setLooping(true);
            mp.start();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void configuraCriterioLocation() {
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();

        PackageManager packageManager = getPackageManager();
        boolean hasGPS = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);

        if (hasGPS) {
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            Log.i("LOCATION", "usando GPS");
        } else {
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            Log.i("LOCATION", "usando WI-FI ou dados");
        }
    }

    public void iniciaGeolocation(Context ctx) {
        //obtem o melhor provedor habilitado com o critério
        provider = lm.getBestProvider(criteria, true);

        if (provider == null) {
            Log.e("PROVEDOR", "Nenhum provedor encontrado");
        } else {
            Log.i("PROVEDOR", "Esta sendo utilizado o provedor " + provider);

            lm.requestLocationUpdates(provider, TEMPO_REQUISICAO_LATLONG, DISTANCIA_MIN_METROS, (LocationListener) ctx);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if(!marker.equals(eu)){
            double distanciaPkmn = getDistanciaPkmn(eu,marker);
            double distanciaMin = distanciaMinimaParaBatalhar;

            //TODO: diminuir a distância entre treinador e pokemon
            if( distanciaPkmn <= distanciaMin) {
                try {
                    //pausa a música
                    mp.pause();

                    Aparecimento ap = aparecimentoMap.get(marker);
                    Intent it = new Intent(this, CapturaActivity.class);
                    it.putExtra("pkmn", ap);

                    startActivity(it);

                    marker.remove();
                }catch (Exception e){
                    Log.e("CliqueMarker","Erro: " + e.getMessage());
                }
            }else{
                DecimalFormat df = new DecimalFormat("0.##");
                Toast.makeText(this,"Você está a " + df.format(distanciaPkmn) + " metros do " + marker.getTitle() + ".\n" +
                        "Aproxime-se pelo menos " + df.format(distanciaPkmn - distanciaMin) + " metros!", Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }

    public void limparMarcadores(){
        try{
            //itera no dicionário de marcadores de aparecimentos
            for (Map.Entry<Marker, Aparecimento> entry : aparecimentoMap.entrySet()){
                Log.d("LimparMarker", "Pokemon: " + entry.getKey().getTitle());
                entry.getKey().remove();
            }

            //limpa o dicionário de marcadores de aparecimentos
            aparecimentoMap.clear();
        }catch (Exception e){
            Log.e("LimparMarker","ERRO: " + e.getMessage());
        }
    }

    public void plotarMarcadores(){
        //Plota Marcadores
        try {
            Aparecimento [] apVet = ControladoraFachadaSingleton.getInstance().getAparecimentos();

            for(int i = 0; i < apVet.length; i++){
                Log.d("PlotarMarker", "Pokemon: " + apVet[i].getPokemon().getNome() + " Lat: " + apVet[i].getLatitude() + " Long: " + apVet[i].getLongitude());

                Marker pokePonto = map.addMarker(new MarkerOptions().
                        icon(BitmapDescriptorFactory.fromResource(apVet[i].getPokemon().getIcone())).
                        position(new LatLng(apVet[i].getLatitude(), apVet[i].getLongitude())).
                        title(apVet[i].getPokemon().getNome()));

                //adiciona marcador no dicionário
                aparecimentoMap.put(pokePonto,apVet[i]);
            }
        }catch (Exception e){
            Log.e("PlotarMarker","ERRO: " + e.getMessage());
        }
    }

    public double getDistanciaPkmn(Marker treinador, Marker pkmn){
        //cria location do treinador para ver distância
        Location trainer = new Location(provider);
        trainer.setLatitude(treinador.getPosition().latitude);
        trainer.setLongitude(treinador.getPosition().longitude);

        //cria location do pokemon para ver distância
        Location poke = new Location(provider);
        poke.setLatitude(pkmn.getPosition().latitude);
        poke.setLongitude(pkmn.getPosition().longitude);

        return trainer.distanceTo(poke);
    }

    public void calcularLatLongMinMaxParaSorteio(Location location){
        double kmInLongitudeDegree = 111.320 * Math.cos( location.getLatitude() / 180.0 * Math.PI);
        double radiusInKm = 0.3;
        double deltaLat = radiusInKm / 111.1;
        double deltaLong = radiusInKm / kmInLongitudeDegree;

        double minLat = location.getLatitude() - deltaLat;
        double maxLat = location.getLatitude() + deltaLat;
        double minLong = location.getLongitude() - deltaLong;
        double maxLong = location.getLongitude() + deltaLong;

        //Soteia os aparecimentos de pokemon na controladora geral
        ControladoraFachadaSingleton.getInstance().sorteiaAparecimentos(minLat, maxLat, minLong, maxLong);

        //atualiza o mapa na thread principal
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                limparMarcadores();
                plotarMarcadores();
            }
        });


        //plotarMarcadorLatLongMinMax(location,minLat,maxLat,minLong,maxLong);
    }

    public void plotarMarcadorLatLongMinMax(Location location, double minLat, double maxLat, double minLong, double maxLong){
        if(LatMin != null && LatMax != null && LongMin != null && LongMax != null){
            LatMin.remove();
            LatMax.remove();
            LongMin.remove();
            LongMax.remove();
        }
        LatMin = map.addMarker(new MarkerOptions().position(new LatLng(minLat, location.getLongitude())).title("LatMin"));
        LatMax = map.addMarker(new MarkerOptions().position(new LatLng(maxLat, location.getLongitude())).title("LatMax"));
        LongMin = map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), minLong)).title("LongMin"));
        LongMax = map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), maxLong)).title("LongMax"));
    }

    public void clickPokedex(View v){
        //Toast.makeText(this,"Pokedex",Toast.LENGTH_SHORT).show();

        Intent it = new Intent(this, PokedexActivity.class);
        startActivityForResult(it, MENU_POKEDEX);
    }

    public void clickPerfil(View v){
        //Toast.makeText(this,"Perfil",Toast.LENGTH_SHORT).show();

        Intent it = new Intent(this, PerfilActivity.class);
        startActivityForResult(it,MENU_PERFIL);
    }

    public void clickMapaCaptura(View v){
        //Toast.makeText(this,"Mapa de Capturas",Toast.LENGTH_SHORT).show();

        Intent it = new Intent(this, MapCapturasActivity.class);
        startActivityForResult(it, MENU_MAPA);
    }

    @Override
    public void run() {
        try {
            while (continuaSorteando) {
                Log.d("SORTEIO","Acordou a Thread de sorteio!");

                //atualiza o mapa na thread principal deixando loader visível
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webViewLoader.setVisibility(View.VISIBLE);
                    }
                });

                //dorme 3 segundos para deixar o loader sendo exibido
                TimeUnit.SECONDS.sleep(3);

                //atualiza o mapa na thread principal deixando loader invisível
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webViewLoader.setVisibility(View.GONE);
                    }
                });

                //Calcula raio em relação à localização e sorteia pokemons
                calcularLatLongMinMaxParaSorteio(posicaoAtual);
                TimeUnit.MINUTES.sleep(intervaloEntreSorteiosEmMinutos);
            }
        }catch (Exception e){
            Log.e("SORTEIO","ERRO: " + e.getMessage());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MENU_PERFIL && resultCode == MENU_PERFIL){
            //Logout
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
