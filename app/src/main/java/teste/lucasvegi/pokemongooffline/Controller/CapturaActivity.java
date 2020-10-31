package teste.lucasvegi.pokemongooffline.Controller;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import teste.lucasvegi.pokemongooffline.Model.Aparecimento;
import teste.lucasvegi.pokemongooffline.Model.ControladoraFachadaSingleton;
import teste.lucasvegi.pokemongooffline.Model.Pokemon;
import teste.lucasvegi.pokemongooffline.R;
import teste.lucasvegi.pokemongooffline.Util.ViewUnitsUtil;
import teste.lucasvegi.pokemongooffline.View.CameraPreview;


public class CapturaActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensor;
    private Sensor accelerometer;
    public ImageView img;
    public ImageView pokebola;
    public int dimenX;  //dimensao horizontal da tela em pixel
    public int dimenY;  //dimensao vertical da tela em pixel
    public float centerX;   //centro horizontal ajustado
    public float centerY;   //centro vertical ajustado
    public float escalaX;   //usada para converter leituras em pixel
    public float escalaY;   //usada para converter leituras em pixel

    public float centerXpokeball;   //centro horizontal ajustado
    public float centerYpokeball;   //centro vertical ajustado

    public float grauXtotal = 0;
    public float grauYtotal = 0;
    public float grauZtotal = 0;

    public float grauXnovo = 0;
    public float grauYnovo = 0;
    public float grauZnovo = 0;

    public float grauXant = 0;
    public float grauYant = 0;
    public float grauZant = 0;

    float distanciaTopoY;
    float distanciaBaseY;
    float distanciaEsquerdaX;
    float distanciaDireitaX;

    public float percentImagePokemon = (float) 0.5;
    public boolean imagemPokemonPreparada = false;
    public float larguraImgPokemon = 0;
    public float alturaImgPokemon = 0;
    public float[] limitesPokemon;

    public float percentImagePokeball = (float) 0.15;
    public boolean imagemPokeballPreparada = false;
    public float larguraImgPokeball = 0;
    public float alturaImgPokeball = 0;
    public float[] limitesPokeball;

    public boolean capturou = false;

    public float xInicioTouch = 0;
    public float yInicioTouch = 0;
    public float xFimTouch = 0;
    public float yFimTouch = 0;
    public long tempoInicial = 0;
    public long tempoFinal = 0;
    float diferencaX;
    float diferencaY;
    long duracaoTouch;
    float velocidadeX; //pixel por milisegundo
    float velocidadeY; //pixel por milisegundo
    float velocidadeXoriginal;
    float velocidadeYoriginal;
    //public DeslocamentoPokebola deslocamentoPokebola;

    public MediaPlayer mp;
    public int countSound = 0; // initialise outside listener to prevent looping

    private CameraPreview mPreview;
    private Camera mCamera;

    private TextView nomePkmnCaptura;

    private Pokemon pkmn;
    private Aparecimento ap;

    private MediaPlayer mpBattle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.view_captura);
        preparaCamera();

        //Obtem gerenciador de sensores
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //Obtem sensores a serem utilizados
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        //Obtem a resolução da tela
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dimenX = size.x;
        dimenY = size.y;

        //Toast.makeText(this, "X: " + dimenX + "px Y: " + dimenY + "px", Toast.LENGTH_LONG).show();

        //Inicia tema da batalha pokemon
        mpBattle = MediaPlayer.create(getBaseContext(), R.raw.battle);
        mpBattle.setLooping(true);
        atribuirVolumeMediaPlayer(mpBattle, 90); //diminui o volume do tema da batalha para compensar audio alto
        mpBattle.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Recebe Intent com Aparecimento vindo do mapa
        Intent it = getIntent();
        ap = (Aparecimento) it.getSerializableExtra("pkmn");
        pkmn = ap.getPokemon();

        //TODO: RESOLVIDO - procura na lista de pokemons da controladora o pokemon recebido da tela anterior.
        //pkmn = ControladoraFachadaSingleton.getInstance().convertPokemonSerializableToObject(pkmn);

        //Define label avisando se pokemon é conhecido ou novo
        TextView labelPkmnNovo = (TextView) findViewById(R.id.labelPkmnNovo);
        if(ControladoraFachadaSingleton.getInstance().getUsuario().getQuantidadeCapturas(pkmn) > 0)
            labelPkmnNovo.setText("conhecido");
        else
            labelPkmnNovo.setText("novo");

        //seta label com o nome do pokemon a ser capturado
        nomePkmnCaptura = (TextView) findViewById(R.id.txtNomePkmnCaptura);
        //garante só rodar após as views estarem na tela
        nomePkmnCaptura.post(new Runnable() {
            @Override
            public void run() {
                nomePkmnCaptura.setText(pkmn.getNome());
                nomePkmnCaptura.measure(0,0);
                //Posiciona o nome do pokemon na lateral superior direita com margem de 8dp à direita
                nomePkmnCaptura.setX(dimenX - nomePkmnCaptura.getMeasuredWidth() - ViewUnitsUtil.convertDpToPixel(8));
            }
        });

        img = (ImageView) findViewById(R.id.pokemon);
        img.setImageResource(pkmn.getFoto());

        pokebola = (ImageView) findViewById(R.id.pokeball);

        configuraPokebola();
        configuraPokemon();

        if(sensor != null){
            //Começa a escutar os sensores utilizados
            sensorManager.registerListener(this, sensor,SensorManager.SENSOR_DELAY_GAME);
        }

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Para de escutar os sensores
        sensorManager.unregisterListener(this);
        imagemPokemonPreparada = false;
        imagemPokeballPreparada = false;

        //pause a musica da batalha
        mpBattle.pause();

        //mPreview.releaseCamera();
        //releaseCamera();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //continua a musica da batalha
        mpBattle.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //para a musica da batalha e devolve o recurso para o sistema
        mpBattle.pause();
        mpBattle.release();
    }

    private void animate(double fromDegrees, double toDegrees, long durationMillis, ImageView img) {
        final RotateAnimation rotate = new RotateAnimation((float) fromDegrees, (float) toDegrees,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        img.clearAnimation();
        rotate.setDuration(durationMillis);
        rotate.setFillEnabled(true);
        rotate.setFillAfter(true);
        img.startAnimation(rotate);
    }

    private HashMap<Integer, Long> timestamp = new HashMap<>();

    private double getSensorElapsedSeconds(SensorEvent event) {
        Long lastTimestamp = timestamp.put(event.sensor.getType(), event.timestamp);

        if (lastTimestamp == null)
            return 0;

        return (event.timestamp - lastTimestamp) / 1000000000f;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GYROSCOPE:
                onGyroscopeChanged(event);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                onAccelerationChanged(event);
                break;
            default:
                throw new IllegalStateException("Unreachable");
        }

    }

    double accelerationNoise, speed, distance;
    long accelerationSamples;

    private double clamp(double value, double min, double max) {
        return value < min ? min : (value > max ? max : value);
    }

    private void onAccelerationChanged(SensorEvent event) {
        double elapsed = getSensorElapsedSeconds(event);

        double accelerationSensor = -event.values[2];
        double accelerationSensorMagnitude = Math.abs(accelerationSensor);
        double accelerationSensorDirection = Math.signum(accelerationSensor);

        accelerationNoise += (1 / ++accelerationSamples) * (accelerationSensorMagnitude - accelerationNoise);

        if (imagemPokemonPreparada && imagemPokeballPreparada) {
            // Atenua o ruído na aceleração.
            double acceleration = Math.max(accelerationSensorMagnitude - accelerationNoise, 0) * accelerationSensorDirection;

            speed = clamp(speed + acceleration * elapsed, -0.25f, +0.25f);
            distance = clamp(distance + speed * elapsed, 0.25f, 0.75f);

            percentImagePokemon = 1f - (float) distance;
            configuraPokemon();

            Log.i("Accel", String.format("A=%.1f S=%.1f D=%.1f N=%.1f", acceleration, speed, distance, accelerationNoise));
        }
    }

    public void onGyroscopeChanged(SensorEvent event) {
        if(imagemPokemonPreparada && imagemPokeballPreparada) {
            float xNovo = img.getX();
            float yNovo = img.getY();

            //mantem tela ligada
            WindowManager.LayoutParams params = this.getWindow().getAttributes();
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            this.getWindow().setAttributes(params);

            //Leitura de valores acelerômetro - USAR QUANDO ORIENTAÇÃO FOR TRAVADA EM PORTRAIT
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            //obtem graus deslocados desde a ultima medição
            grauXnovo = (float) ((x * 57.2958) * 0.02); //0.02 segundos devido ao SENSOR_DELAY_GAME
            grauYnovo = (float) ((y * 57.2958) * 0.02); //0.02 segundos devido ao SENSOR_DELAY_GAME
            grauZnovo = (float) ((z * 57.2958) * 0.02); //0.02 segundos devido ao SENSOR_DELAY_GAME

            //total de graus deslocados desde o inicio
            grauXtotal += grauXnovo;
            grauYtotal += grauYnovo;
            grauZtotal += grauZnovo;

            Log.i("Posição", "X: " + x + " Y: " + y + " Z: " + z);
            Log.i("GrauNovo", "X: " + grauXnovo + " Y: " + grauYnovo + " Z: " + grauZnovo);
            Log.i("Grau", "X: " + grauXtotal + " Y: " + grauYtotal + " Z: " + grauZtotal);

            //atualiza a posição da imagem caso o dispositivo seja deslocado consideravelmente
            if (grauYtotal > grauYant + 0.01 || grauYtotal < grauYant - 0.01) {
                xNovo = img.getX() + (grauYnovo * escalaX);
                img.setX(xNovo);
            } else {
                grauYtotal = grauYant; //elimina pequenos ruidos do sensor com dispositivo parado
            }

            //atualiza a posição da imagem caso o dispositivo seja deslocado consideravelmente
            if (grauXtotal > grauXant + 0.01 || grauXtotal < grauXant - 0.01) {
                yNovo = img.getY() + (grauXnovo * escalaY);
                img.setY(yNovo);
            } else {
                grauXtotal = grauXant; //elimina pequenos ruidos do sensor com dispositivo parado
            }

            //atualiza a posição da imagem caso o dispositivo seja deslocado consideravelmente
            if (grauZtotal > grauZant + 0.01 || grauZtotal < grauZant - 0.01) {
                //rodarImagem(img,grauZtotal);
                //TODO - compensar a alteração de largura e altura da imagem após rotação evitando o corte da imagem

                //animate(0,grauZtotal,0,img);

                Log.d("Pivot", "X: " + img.getPivotX() + " Y: " + img.getPivotY());

                img.setRotation(grauZtotal);
            } else {
                grauZtotal = grauZant; //elimina pequenos ruidos do sensor com dispositivo parado
            }

            //guarda medição para comparar com a proxima
            grauXant = grauXtotal;
            grauYant = grauYtotal;
            grauZant = grauZtotal;

            Log.i("IMAGEM", "X: " + img.getX() + " Y: " + img.getY());

            //RETORNANDO IMAGEM PARA A TELA PARA COMPLETAR O GIRO DE 360°-------------------------------

            //girando horizontalmente para a direita
            if (grauYtotal < 0) {
                //obtem diferença em graus em relação ao centro
                if (Math.abs(Math.abs(grauYtotal) - 360) <= distanciaDireitaX / escalaX) {
                    img.setX(dimenX - 10); //compensa os limites para ser possível comparação ser verdadeira, uma vez que primeiro valor dela será sempre positivo
                    centerX = img.getX();
                    distanciaEsquerdaX = centerX;
                    distanciaDireitaX = dimenX - centerX;
                    grauYtotal = 0;
                }
            }

            //girando horizontalmente para a esquerda
            //arrumar reentrada compensando a largura da imagem - FEITO
            if (grauYtotal > 0) {
                //obtem diferença em graus em relação ao centro
                if (Math.abs(Math.abs(grauYtotal) - 360) <= (distanciaEsquerdaX + larguraImgPokemon) / escalaX) {
                    //if(Math.abs(Math.abs(grauYtotal) - 360) <= distanciaEsquerdaX/escalaX){

                    Log.d("Passei", "DE: " + distanciaEsquerdaX + " DimX: " + dimenX + " PI: " + percentImagePokemon + " EX: " + escalaX);
                    Log.d("Passei", "estive aqui horizontal " + grauYtotal + " " + (distanciaEsquerdaX + larguraImgPokemon) / escalaX);

                    //img.setX(5); //compensa os limites
                    //img.setX(-img.getMeasuredWidth());

                    img.setX(-larguraImgPokemon);
                    centerX = img.getX();
                    //distanciaEsquerdaX = centerX;
                    //distanciaEsquerdaX = escalaX; //para forçar quociente 1 após primeiro loop
                    distanciaEsquerdaX = escalaX - larguraImgPokemon; //para forçar quociente 1 após primeiro loop
                    distanciaDireitaX = dimenX - centerX;
                    grauYtotal = 0;
                }
            }

            //girando verticalmente para a cima
            //arrumar reentrada compensando a altura da imagem - FEITO
            if (grauXtotal > 0) {
                //obtem diferença em graus em relação ao centro
                if (Math.abs(Math.abs(grauXtotal) - 360) <= (distanciaTopoY + alturaImgPokemon) / escalaY) {
                    //if(Math.abs(Math.abs(grauXtotal) - 360) <= distanciaTopoY/escalaY){

                    Log.d("Passei", "estive aqui vertical " + grauXtotal + " " + (distanciaTopoY + alturaImgPokemon) / escalaY);

                    //img.setY(5); //compensa os limites
                    //img.setY(-img.getMeasuredHeight());

                    img.setY(-alturaImgPokemon);
                    centerY = img.getY();
                    //distanciaTopoY = centerY;
                    //distanciaTopoY = escalaY; //para forçar quociente 1 após primeiro loop
                    distanciaTopoY = escalaY - alturaImgPokemon; //para forçar quociente 1 após primeiro loop
                    distanciaBaseY = dimenY - centerY;
                    grauXtotal = 0;
                }
            }

            //girando verticalmente para a baixo
            if (grauXtotal < 0) {
                //obtem diferença em graus em relação ao centro
                if (Math.abs(Math.abs(grauXtotal) - 360) <= distanciaBaseY / escalaY) {
                    img.setY(dimenY - 10); //compensa os limites
                    centerY = img.getY();
                    distanciaTopoY = centerY;
                    distanciaBaseY = dimenY - centerY;
                    grauXtotal = 0;
                }
            }

            //obtem limites do pokemon
            // TODO: obter limites tambem fora do onSensorChanged para permitir celulares sem o giroscópio a jogarem
            limitesPokemon = getLeftRightTopBottomImage(img.getX(),img.getY(),alturaImgPokemon,larguraImgPokemon);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //mudou a precisão
    }

    public void preparaCamera(){
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_captura);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Create an instance of Camera
       // mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        frameLayout.addView(mPreview);

        AbsoluteLayout absolutLayoutControls = (AbsoluteLayout) findViewById(R.id.imagemAR);
        absolutLayoutControls.bringToFront();

    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public float[] getLeftRightTopBottomImage(float x, float y, float altura, float largura){
        float limites[] = new float[4];
        limites[0] = x;
        limites[1] = x + largura;
        limites[2] = y;
        limites[3] = y + altura;

        Log.d("Limites", "E: " + limites[0] + " D: " + limites[1] + " C: " + limites[2] + " B: " + limites[3]);
        return limites;
    }

    public void configuraPokemon(){
        //garante só rodar após as views estarem na tela
        img.post(new Runnable() {
            @Override
            public void run() {
                //define largura do pokemon em relação ao tamanho da tela
                larguraImgPokemon = dimenX * percentImagePokemon;
                //obtem propoção da imagem redimensionada
                float proporcaoPokemon = (larguraImgPokemon * 100) / img.getMeasuredWidth();
                //define altura do pokemon de forma proporcional
                alturaImgPokemon = img.getMeasuredHeight() * proporcaoPokemon / 100;

                //obtem o centro da tela
                //COLOCAR ELE RANDOMICO Mais pra frente
                centerX = dimenX / 2 - (((int) larguraImgPokemon) / 2);
                centerY = dimenY / 2 - (((int) alturaImgPokemon) / 2);

                //X: 1200 Y: 1834 CX: 300.0 CY: 459.0 IMG_X: 600 IMG_Y: 917

                //modifica o tamanho da imagem e centraliza o mesmo na tela
                AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams((int) larguraImgPokemon, (int) alturaImgPokemon, (int) centerX, (int) centerY);
                img.setLayoutParams(params);

                //calcula distâncias iniciais
                distanciaTopoY = centerY;
                distanciaBaseY = dimenY - centerY;
                distanciaEsquerdaX = centerX;
                distanciaDireitaX = dimenX - centerX;

                //calcula a escala
                escalaX = dimenX / 72; //cada grau vale escala pixel - 72º é o campo de visão considerado
                escalaY = dimenY / 72;

                Log.i("Dimensão", "X: " + dimenX + " Y: " + dimenY + " CX: " + centerX + " CY: " + centerY +
                        " IMG_X: " + (int) larguraImgPokemon + " IMG_Y: " + (int) alturaImgPokemon);

                imagemPokemonPreparada = true;
            }
        });
    }

    public void configuraEfeitoCaptura(){
        mp = MediaPlayer.create(getBaseContext(), R.raw.quicando);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            int maxCount = 3;
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(countSound < maxCount) {
                    countSound++;
                    mediaPlayer.seekTo(0); //TODO: pesquisar o que faz metodo seekTo MediaPlayer
                    mediaPlayer.start();

                    //anima a pokebola junto com o som de quique
                    if(countSound % 2 == 0) {
                        //pokebola.setRotation(0);
                        pokebola.animate().rotation(0).start();

                    } else {
                        //pokebola.setRotation(-20);
                        pokebola.animate().rotation(-20).start();
                    }


                }else{
                    countSound = 0;

                    MediaPlayer mp2 = MediaPlayer.create(getBaseContext(), R.raw.sucesso);
                    mp2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            //TODO: enviar captura para o servidor antes de fechar tela. Talvez fazer isso diretamento no método capturar
                            ControladoraFachadaSingleton.getInstance().getUsuario().capturar(ap);
                            ControladoraFachadaSingleton.getInstance().aumentaXp("captura");   //atualiza XP do usuário após uma captura
                            Toast.makeText(getBaseContext(),"Você ganhou " +
                                    ControladoraFachadaSingleton.getInstance().getXpEvento("captura") + " de XP",Toast.LENGTH_SHORT).show();
                            finish(); //fecha o módulo de captura quando a música de sucesso acaba
                        }
                    });

                    //pausa a musica da batalha e começa a do sucesso
                    mpBattle.pause();
                    mp2.start();

                    Toast.makeText(getBaseContext(),pkmn.getNome() + " foi capturado! \\o/",Toast.LENGTH_LONG).show();
                }
            }});
        //diminui o volume do tema da batalha antes da pokebola quicar
        atribuirVolumeMediaPlayer(mpBattle, 85);

        mp.start();

        //substituiu imagem do pokemon por explosão
        img.setImageResource(R.drawable.explosion);

        img.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(350);
                    img.setVisibility(View.INVISIBLE);
                } catch (Exception e) {

                }
            }
        });
    }

    public void atribuirVolumeMediaPlayer(MediaPlayer mediaPlayer, int volume){
        int maxVolume = 100;
        float log1=(float)(Math.log(maxVolume-volume)/Math.log(maxVolume));
        mediaPlayer.setVolume(1-log1,1-log1);
    }

    public void configuraPokebola(){
        //garante só rodar após as views estarem na tela
        pokebola.post(new Runnable() {
            @Override
            public void run() {
                //define largura da pokebola em relação ao tamanho da tela
                larguraImgPokeball = dimenX * percentImagePokeball;
                //obtem propoção da imagem redimensionada
                float proporcaoPokebola = (larguraImgPokeball * 100) / pokebola.getMeasuredWidth();
                //define altura da pokebola de forma proporcional
                alturaImgPokeball = pokebola.getMeasuredHeight() * proporcaoPokebola / 100;

                //obtem o centro da tela
                centerXpokeball = dimenX / 2 - (((int) larguraImgPokeball) / 2);
                centerYpokeball = dimenY - (int) alturaImgPokeball - 75; //menos 40 para compensar a barra de tarefas do android

                //X: 1200 Y: 1834 CX: 300.0 CY: 459.0 IMG_X: 600 IMG_Y: 917

                //modifica o tamanho da imagem e centraliza o mesmo na tela
                AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams((int) larguraImgPokeball, (int) alturaImgPokeball, (int) centerXpokeball, (int) centerYpokeball);
                pokebola.setLayoutParams(params);

                Log.i("Dimensão", "X: " + dimenX + " Y: " + dimenY + " CX_pokeball: " + centerXpokeball + " CY_pokeball: " + centerYpokeball +
                        " IMG_X_pokeball: " + (int) larguraImgPokeball + " IMG_Y_pokeball: " + (int) alturaImgPokeball);

                imagemPokeballPreparada = true;
            }
        });

        configuraTouchPokebola();
    }

    public void configuraTouchPokebola(){
        //configura o listener de toque na pokebola
        pokebola.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //TODO: PESQUISAR O COMPORTAMENTO EXATO DOS MÉTODOS getRawX() e getRawY()
                float x = event.getRawX();
                float y = event.getRawY();
                Log.d("Mover Pokebola", "X: " + x + " Y: " + y);

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        //tratamento se pressionou a imagem
                        mp = MediaPlayer.create(getBaseContext(), R.raw.arremesso);
                        mp.start();

                        tempoInicial = System.currentTimeMillis();
                        xInicioTouch = pokebola.getX();
                        yInicioTouch = pokebola.getY();

                        Log.d("Mover Pokebola", "Tocou na pokebola");
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        //tratamento se retirou o dado da imagem

                        tempoFinal = System.currentTimeMillis();
                        xFimTouch = pokebola.getX();
                        yFimTouch = pokebola.getY();

                        diferencaX = Math.abs(xInicioTouch - xFimTouch);
                        diferencaY = Math.abs(yInicioTouch - yFimTouch);
                        duracaoTouch = tempoFinal - tempoInicial;

                        velocidadeX = diferencaX / duracaoTouch; //pixel por milisegundo
                        velocidadeY = diferencaY / duracaoTouch; //pixel por milisegundo

                        //inicia a Thread
                        //deslocamentoPokebola = new DeslocamentoPokebola();
                        //deslocamentoPokebola.execute(); //MÉTODO doInBackground da class DeslocamentoPokebola é executado

                        velocidadeXoriginal = velocidadeX;
                        velocidadeYoriginal = velocidadeY;

                        while (velocidadeX > 0 && velocidadeY > 0 && !capturou) {
                            int tempo = 25; //compensa a velocidade para a pokebola acelerar mais
                            //pokebola arremessada para a direita
                            if (xFimTouch >= xInicioTouch) {
                                pokebola.setX(pokebola.getX() + (tempo * velocidadeX));
                            } else {
                                pokebola.setX(pokebola.getX() - (tempo * velocidadeX));
                            }

                            //pokebola arremessada para cima
                            if (yFimTouch <= yInicioTouch) {
                                pokebola.setY(pokebola.getY() - (tempo * velocidadeY));
                            } else {
                                pokebola.setY(pokebola.getY() + (tempo * velocidadeY));
                            }

                            //---------------VERIFICA CAPTURA-------------
                            //obtem limites da pokebola
                            limitesPokeball = getLeftRightTopBottomImage(pokebola.getX(), pokebola.getY(), alturaImgPokeball, larguraImgPokeball);
                            Log.d("LimPokeball", "E: " + limitesPokeball[0] + " D: " + limitesPokeball[1] + " C: " + limitesPokeball[2] + " B: " + limitesPokeball[3]);

                            //verifica se houve captura pela interseção de imagens
                            if (isCapturou(limitesPokeball, limitesPokemon, capturou)) {
                                capturou = true;
                                configuraEfeitoCaptura();
                                Log.w("Mover Pokebola", "Capturou por arremesso " + getTime());
                                break;
                            }
                            //--------------------------------------------

                            Log.d("Deslocamento", "Atualizei..VX: " + velocidadeX + " VY: " + velocidadeY);

                            //reduzindo a velocidade da pokebola
                            velocidadeX = velocidadeX - (velocidadeXoriginal * (float) 0.045);
                            velocidadeY = velocidadeY - (velocidadeYoriginal * (float) 0.045);
                        }

                        if(pokebola.getX() > dimenX || pokebola.getX() < 0 ||
                                pokebola.getY() > dimenY || pokebola.getY() < 0){

                            Toast.makeText(getBaseContext(),"Tente novamente...",Toast.LENGTH_SHORT).show();

                            pokebola.setX(centerXpokeball);
                            pokebola.setY(centerYpokeball);
                        }

                        Log.d("Mover Pokebola", "Retirou o dedo da pokebola");
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //tratamento para arrastar imagem
                        Log.d("Mover Pokebola", "Moveu a pokebola");

                        //pokebola.setX(x - (larguraImgPokeball / 2));
                        //pokebola.setY(y - (alturaImgPokeball / 2));

                        pokebola.setX(x - (larguraImgPokeball / 2));
                        pokebola.setY((y - (alturaImgPokeball / 3)) - (alturaImgPokeball / 2)); //-(alturaImgPokeball / 3) para suavizar o movimento

                        //---------------VERIFICA CAPTURA-------------
                        //obtem limites da pokebola
                        limitesPokeball = getLeftRightTopBottomImage(pokebola.getX(), pokebola.getY(), alturaImgPokeball, larguraImgPokeball);
                        Log.d("LimPokeball", "E: " + limitesPokeball[0] + " D: " + limitesPokeball[1] + " C: " + limitesPokeball[2] + " B: " + limitesPokeball[3]);

                        //verifica se houve captura pela interseção de imagens
                        if (isCapturou(limitesPokeball, limitesPokemon, capturou)) {
                            Log.w("Mover Pokebola", "Capturou por tocar " + getTime());
                            capturou = true;
                            configuraEfeitoCaptura();
                        }
                        //---------------------------------------------

                        return true;

                    default:
                        Log.d("Mover Pokebola", "Evento não classificado na pokebola");
                }

                return false;
            }
        });
    }

    public String getTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
        String strDate = sdf.format(c.getTime());
        return strDate;
    }

    //verifica interseção
    public boolean isCapturou(float pkball[],float pkmn[], boolean cap){
        if(pkball[0] <= pkmn[1] &&
                pkmn[0] <= pkball[1] &&
                pkball[2] <= pkmn[3] &&
                pkmn[2] <= pkball[3] && !cap){
            return true;
        }else{
            return false;
        }
    }

   /*private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }*/

    class DeslocamentoPokebola extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pDialog = new ProgressDialog(Colaboracao.this);
            pDialog.setMessage("ENVIANDO...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();*/
        }

        protected String doInBackground(String... args) {
            /*
                        tempoFinal = System.currentTimeMillis();
                        xFimTouch = pokebola.getX();
                        yFimTouch = pokebola.getY();

                        diferencaX = Math.abs(xInicioTouch - xFimTouch);
                        diferencaY = Math.abs(yInicioTouch - yFimTouch);
                        duracaoTouch = tempoFinal - tempoInicial;

                        velocidadeX = diferencaX/duracaoTouch; //pixel por milisegundo
                        velocidadeY = diferencaY/duracaoTouch; //pixel por milisegundo
             */

            velocidadeY = velocidadeY*3;
            velocidadeX = velocidadeX*3;

            velocidadeXoriginal = velocidadeX;
            velocidadeYoriginal = velocidadeY;

            while(velocidadeX > 0 && velocidadeY > 0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int tempo = 100;
                        //pokebola arremessada para a direita
                        if(xFimTouch >= xInicioTouch) {
                            pokebola.setX(pokebola.getX() + (tempo * velocidadeX));
                        }else{
                            pokebola.setX(pokebola.getX() - (tempo * velocidadeX));
                        }

                        //pokebola arremessada para cima
                        if(yFimTouch <= yInicioTouch) {
                            pokebola.setY(pokebola.getY() - (tempo * velocidadeY));
                        }else{
                            pokebola.setY(pokebola.getY() + (tempo * velocidadeY));
                        }

                        try {
                            Thread.sleep(tempo);
                        }catch (Exception e){
                            Log.e("ERRO", "sleep asyncTask");
                        }

                        Log.d("Deslocamento", "Atualizei..VX: " + velocidadeX + " VY: " + velocidadeY);

                        velocidadeX = velocidadeX - (velocidadeXoriginal*(float)0.03);
                        velocidadeY = velocidadeY - (velocidadeYoriginal*(float)0.03);

                        if(velocidadeX < 0 || velocidadeY < 0)
                            return;

                    }
                });

            }



            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            /*pDialog.dismiss();
            Log.i("CANCELOU", "CANCELOU ASYNC TASK");*/
        }

        protected void onPostExecute(String file_url) {
            //pDialog.dismiss();
            Log.d("Deslocamento", "acabei..");
        }
    }

}


