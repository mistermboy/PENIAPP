package es.uniovi.uo252406.talkingfer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

public class GridTest extends AppCompatActivity {

    private Intent intent;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_grid);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                startMain("fer");
                //startMain("berto");
            }
        });
    }


    /**
     * Pasa al Main
     */
    private void startMain(String person) {
        intent = new Intent(GridTest.this, MainActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("person", person);

        intent.putExtras(bundle);
        startActivityForResult(intent,MainActivity.FREE_ACTIVITY);

    }

}
