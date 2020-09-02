package teste.lucasvegi.pokemongooffline.Controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import teste.lucasvegi.pokemongooffline.Model.ControladoraFachadaSingleton;
import teste.lucasvegi.pokemongooffline.R;

public class CadastrarActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar);
        //teste 2
    }

    public void clickVoltar(View v){
        Intent it = new Intent(this,LoginActivity.class);
        startActivity(it);
        finish();
    }

    public void clickCadastrarUser(View v){
        try {
            Log.i("CADASTRO", "Cadastrando o usuário no sistema...");

            boolean cadOk = true;

            EditText edtNome = (EditText) findViewById(R.id.edtNomeCadastro);

            //TODO: verificar no servidor se usuário já existe.
            EditText edtUsuario = (EditText) findViewById(R.id.edtUsuarioCadastro);

            EditText edtSenha = (EditText) findViewById(R.id.edtSenhaCadastro);
            EditText edtConfirmaSenha = (EditText) findViewById(R.id.edtConfirmacaoSenhaCadastro);
            RadioGroup sexo = (RadioGroup) findViewById(R.id.grupoSexo);

            //obtem dados informados pelo usuário
            String nome = edtNome.getText().toString();
            String user = edtUsuario.getText().toString();
            String senha = edtSenha.getText().toString();
            String confSenha = edtConfirmaSenha.getText().toString();
            String nomeSexo = "";

            //obtem informação do radio group
            if (sexo.getCheckedRadioButtonId() == R.id.sexoMasculino)
                nomeSexo = "M";
            else
                nomeSexo = "F";

            //Verifica preenchimento de campos obrigatórios e valida dados
            if (nome.length() == 0 || nome.length() > 50) {
                Toast.makeText(this, "Informe um nome com até 50 caracteres!", Toast.LENGTH_SHORT).show();
                cadOk = false;
            } else if (user.length() == 0 || user.length() > 45) {
                Toast.makeText(this, "Informe um usuário com até 45 caracteres!", Toast.LENGTH_SHORT).show();
                cadOk = false;
            } else if (senha.length() == 0 || senha.length() > 45) {
                Toast.makeText(this, "Informe uma senha com até 45 caracteres!", Toast.LENGTH_SHORT).show();
                cadOk = false;
            } else if (confSenha.length() == 0) {
                Toast.makeText(this, "Informe a confirmação da senha!", Toast.LENGTH_SHORT).show();
                cadOk = false;
            } else if (!senha.equals(confSenha)) {
                Toast.makeText(this, "Confirmação de senha inválida!\nDigite-a novamente.", Toast.LENGTH_SHORT).show();
                cadOk = false;
            }

            //Cadastra usuário se dados forem válidos
            if (cadOk) {
                if (ControladoraFachadaSingleton.getInstance().cadastrarUser(user, senha, nome, nomeSexo, "")) {
                    Toast.makeText(this, "Usuário cadastrado!", Toast.LENGTH_SHORT).show();

                    Intent it = new Intent(this, MapActivity.class);
                    startActivity(it);
                    finish();
                } else {
                    Toast.makeText(this, "Problemas ao tentar cadastrar o Usuário.\nTente novamente!", Toast.LENGTH_SHORT).show();
                }
            }

        }catch (Exception e){
            Log.e("CADASTRO", "ERRO: " + e.getMessage());
        }

    }
}
