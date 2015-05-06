package conferenceimporter.es2.estg.ipleiria.pt.conferencedataimporter;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.Firebase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Isto tem que ser feito uma vez no arranque da aplica��o
        Firebase.setAndroidContext(getApplicationContext());
    }

    // M�todo callback do bot�o "Send Data". Ver propriedade "onClick" desse bot�o.
    public void sendData(View v) {
        EditText url=(EditText)findViewById(R.id.editText);

        // URL base definido pelo utilizador na caixa de texto - URL da base de dados Firebase
        String firebaseUrl=url.getText().toString().trim();

        // Refer�ncia para a raiz do Firebase
        Firebase fb=new Firebase(firebaseUrl);
        String key;

        try {
            // Acesso ao ficheiro na pasta de assets (inclu�do no .apk da aplica��o)
            InputStream is=getAssets().open("DadosConferencia.csv"); // bin�rio
            InputStreamReader reader=new InputStreamReader(is); // convers�o para texto
            BufferedReader br=new BufferedReader(reader); // stream processada

            //m�todo auxiliar para leitura dos dados da confer�ncia
            key = importConferenceData(br, fb);
            br.close();

            // Acesso ao ficheiro na pasta de assets (inclu�do no .apk da aplica��o)
            is=getAssets().open("DadosSessoes.csv"); // bin�rio
            reader=new InputStreamReader(is); // convers�o para texto
            br=new BufferedReader(reader); // stream processada

            //m�todo auxiliar para leitura dos dados das sess�es da confer�ncia
            importSessionData(br, fb, key);
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importSessionData(BufferedReader br, Firebase fb, String key) throws IOException {
        // Defini��o de um novo n� para as sess�es
        HashMap<String, String> sessions = new HashMap<>();

        // separar a linha lida pelo delimitador '|' - nomes dos campos
        String []campos=br.readLine().split("\\|");

        String line;
        while ((line=br.readLine())!=null) {

            String[] parts = line.split("\\|",-1); // separar os diversos valores de uma sess�o
            for (int i=0; i<campos.length; i++) {
                sessions.put(campos[i], parts[i]); // definir o valor de cada campo
                System.out.println("Session " + campos[i] + " : " + parts[i]);
            }

            fb.child("Conferences").child(key).child("Sessions").push().setValue(sessions);
            sessions.clear();
        }
    }

    private String importConferenceData(BufferedReader br, Firebase fb) throws IOException {
        // Defini��o de um novo n� para os dados da confer�ncia

        HashMap<String, String> conference = new HashMap<>();

        String line;
        while ((line=br.readLine())!=null) {
            String[] parts = line.split("\\|"); // separar a linha lida pelo delimitador '|' - campo|valor da confer�ncia
            conference.put(parts[0], parts[1]); // definir o valor de cada campo
        }

        String k = fb.child("Conferences").push().getKey();
        fb.child("Conferences").child(k).setValue(conference);
        return k;
    }
}
