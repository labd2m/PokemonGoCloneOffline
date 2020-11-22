package teste.lucasvegi.pokemongooffline.Controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import teste.lucasvegi.pokemongooffline.Model.Aparecimento;
import teste.lucasvegi.pokemongooffline.Model.ControladoraFachadaSingleton;
import teste.lucasvegi.pokemongooffline.Model.Pokemon;
import teste.lucasvegi.pokemongooffline.Model.PokemonCapturado;
import teste.lucasvegi.pokemongooffline.R;
import teste.lucasvegi.pokemongooffline.Util.MyApp;
import teste.lucasvegi.pokemongooffline.View.AdapterPokedex;
import teste.lucasvegi.pokemongooffline.View.AdapterTrocaPokemonsList;

public class TrocaListaPokemonActivity extends Activity implements AdapterView.OnItemClickListener{

    private List<Pokemon> pokemons;

    static final int REQUEST_ENABLE_BT = 1;

    protected static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice device;
    private BluetoothSocket socket;

    private ListView listView;

    private Pokemon ofertado = null;
    private Pokemon recebido = null;
//    boolean pode_alterar_oferta = true;
    private boolean outro_aceitou = false;

    private Button aceitar;
    private Button rejeitar;
    private ImageView euAceitei;
    private ImageView outroAceitou;


    private AdapterTrocaPokemonsList adapterPokedex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_troca_lista_pokemons);

        //obtem referências das views

        try {
            //Prepara a listview customizada da pokedex
            pokemons = ControladoraFachadaSingleton.getInstance().getPokemons();
            listView = (ListView) findViewById(R.id.listaTrocaPokemons);

            adapterPokedex = new AdapterTrocaPokemonsList(pokemons, this);
            listView.setAdapter(adapterPokedex);
            listView.setOnItemClickListener(this);

            aceitar  = (Button) findViewById(R.id.botaoAceitar);
            rejeitar = (Button) findViewById(R.id.botaoRejeitar);
            euAceitei = (ImageView) findViewById(R.id.euAceitei);
            outroAceitou = (ImageView) findViewById(R.id.outroAceitou);
            rejeitar.setEnabled(false);


        }catch (Exception e){
            Log.e("POKEDEX", "ERRO: " + e.getMessage());
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        socket = MyApp.getBluetoothSocket();

        if(socket != null)
            Log.e("TROCA", "Socket encontrado");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
//            if(!pode_alterar_oferta) {
//                Context context = getApplicationContext();
//                CharSequence text = "Você não pode trocar o Pokémon que está oferecendo! Aperte Rejeitar para trocar sua oferta.";
//                int duration = Toast.LENGTH_SHORT;
//
//                Toast toast = Toast.makeText(context, text, duration);
//                toast.show();
//
//                return;
//            }

            //Click em um item da listView customizada
            ofertado = (Pokemon) parent.getAdapter().getItem(position);

            ImageView pokemon_selecionado = (ImageView) findViewById(R.id.meu_pokemon_selecionado);
            pokemon_selecionado.setImageResource(ofertado.getIcone());
            adapterPokedex.setSelected(position);

            //teste, depois tirar
            int pos2 = position+1;
            if(pos2 > pokemons.size())
                pos2=1;
            recebido = (Pokemon) parent.getAdapter().getItem(pos2);
            ImageView outroPoke = (ImageView) findViewById(R.id.outro_pokemon_selecionado);
            outroPoke.setImageResource(recebido.getIcone());

            outro_aceitou = true;
            outroAceitou.setImageResource(android.R.drawable.checkbox_on_background);
            //end teste

        }catch (Exception e){
            Log.e("POKEDEX", "ERRO no click: " + e.getMessage());
        }

    }

    public void aceitarTroca(View v){
        if(ofertado == null) {
            Context context = getApplicationContext();
            CharSequence text = "Você não fazer uma troca sem ofertar algum Pokémon! Ofereça algum Pokémon da sua coleção.";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        if(recebido == null) {
            Context context = getApplicationContext();
            CharSequence text = "Você não fazer uma troca sem receber algum Pokémon! Espere o outro treinador fazer a oferta dele.";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        if(outro_aceitou) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Context ctx = this;

            PackageManager packageManager = getPackageManager();
            boolean hasGPS = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);

            if (hasGPS) {
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                Log.i("LOCATION", "usando GPS");
            } else {
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                Log.i("LOCATION", "usando WI-FI ou dados");
            }

            String provider = lm.getBestProvider(criteria, true);

            if (provider == null) {
                Log.e("TROCA", "Nenhum provedor encontrado");

                Context context = getApplicationContext();
                CharSequence text = "Não pudemos obter sua posição geográfica. Tente novamente.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            } else {
                Log.i("TROCA", "Esta sendo utilizado o provedor " + provider);

                lm.requestLocationUpdates(provider, 5000, 10, (LocationListener) ctx);
//                lm.requestSingleUpdate(provider, , null );
//                lm.requestSingleUpdate(provider, );
            }

            double lat = lm.getLastKnownLocation(provider).getLatitude();
            double lon = lm.getLastKnownLocation(provider).getLongitude();
            Aparecimento ap = new Aparecimento();
            ap.setLatitude(lat); ap.setLongitude(lon);
            ap.setPokemon(recebido);
//            ControladoraFachadaSingleton.getInstance().getUsuario().capturar(ap);

            for (PokemonCapturado capt: ControladoraFachadaSingleton.getInstance().getUsuario().getPokemons().get(ofertado) ) {
                if(!capt.getFoiTrocado())
                    capt.setFoiTrocado(true);
            }

            Context context = getApplicationContext();
            CharSequence text = "Troca realizada com sucesso!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finish();
        }
        else {
//            pode_alterar_oferta = false;
            aceitar.setEnabled(false);
            rejeitar.setEnabled(true);
            adapterPokedex.setAreAllEnabled(false);
            euAceitei.setImageResource(android.R.drawable.checkbox_on_background);

        }

    }

    public void rejeitarTroca(View v){
//        pode_alterar_oferta = true;
        aceitar.setEnabled(true);
        rejeitar.setEnabled(false);
        euAceitei.setImageResource(android.R.drawable.checkbox_off_background);
        adapterPokedex.setAreAllEnabled(true);
        adapterPokedex.notifyDataSetChanged();
    }

    public void clickVoltar(View v){
        finish();
    }


}