package es.uniovi.uo252406.peniapp.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import es.uniovi.uo252406.peniapp.Logical.Player;
import es.uniovi.uo252406.peniapp.Logical.Util.Parpadeo;
import es.uniovi.uo252406.peniapp.Logical.Persistence.FavouritesDataSource;
import es.uniovi.uo252406.simplefer.R;


public class AudiosFragment extends android.support.v4.app.Fragment {

    View view;
    ImageView swipe;

    ArrayList<String> audios;
    String person;
    boolean isFavouriteFragment;

    String exStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    String path=(exStoragePath +"/media/ringtones/");

    String filename;
    String pressed = "";

    FavouritesDataSource bd;

    Parpadeo parpadeo;

    private final int REQUEST_ACCESS_FINE =0;


    public AudiosFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_audios, container, false);


        //Obtenemos los objetos enviados desde el MainActivity
        Bundle b = getActivity().getIntent().getExtras();
        person = (String) b.getString("person");
        isFavouriteFragment = (boolean) getArguments().getBoolean("favourite");

        swipe =  view.findViewById(R.id.audiosSwipe);

        new Progress().execute();

        return view;
    }



    private  void checkFirstRun(){
        boolean isFirtsRun = getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getBoolean("isFirstRun",true);
        if(isFirtsRun){

            swipe.setVisibility(View.INVISIBLE);

            final Dialog info = new Dialog(getActivity());
            info.setContentView(R.layout.info_dialog);
            info.show();


            final CheckBox checkBox = info.findViewById(R.id.checkNoMore);
            Button button = info.findViewById(R.id.btnAceptInfoDialog);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (checkBox.isChecked())
                        getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();

                    info.dismiss();

                    if (audios.size() > 11) {
                        swipe.setVisibility(View.VISIBLE);
                        parpadeo = new Parpadeo(view.getContext(), swipe);
                    }

                }
            });


        }else{
            if(audios.size()>10)
                parpadeo = new Parpadeo(view.getContext(),swipe);
            else
                swipe.setVisibility(View.INVISIBLE);
        }

    }




    private class Progress extends AsyncTask<Void, Void, Void> {


        protected void onPreExecute() {

            ProgressBar progressBar = view.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            swipe.setVisibility(View.INVISIBLE);

        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }



            if (isFavouriteFragment) {

                openDB();
                audios = bd.getAllFavorites(person);
                closeDB();

            } else
                audios = Player.getInstance().getAudios(person);

            Collections.sort(audios);
            return null;
        }

        protected void onPostExecute (Void result){

            checkFirstRun();

            ProgressBar progressBar = view.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);

            //Obtenemos el linear layout del scroll
            LinearLayout lScroll = (LinearLayout) view.findViewById(R.id.lScroll);


            //Propiedades para los botones
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);


            ArrayList<String> elementos = new ArrayList<>();

            elementos.add("Guadar como favorito");
            elementos.add("Compartir en whatsapp");
            elementos.add("Establecer como tono de llamada");


            ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, elementos);

            //Creaación de los botones

            for (int i = 0; i < audios.size(); i++) {

                Button button = new Button(view.getContext());
                button.setId(i);
                //Asignamos propiedades de layout al boton
                button.setLayoutParams(lp);
                //Nos quedamos solo con el texto que nos interesa
                String buttonText = String.valueOf(audios.get(i)).replace("_", " ").replace(person, "").replace("0", "ñ"); //Los audios no pueden llevar "ñ" así que ponemos un 0 y luego sustituimos
                //Asignamos Texto al botón
                button.setText(buttonText);
                //Asignamos la fuente
                Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.indieflower);
                button.setTypeface(typeface);
                //Aumentamos el tamaño de la letra
                button.setTextSize(20);
                //Cambiamos colores
                button.setBackgroundColor(getResources().getColor(R.color.black));
                button.setTextColor(getResources().getColor(R.color.white));
                //Asignamos los listener
                button.setOnClickListener(new AudiosFragment.ButtonsOnClickListener(audios.get(i)));
                button.setOnLongClickListener(new AudiosFragment.ButtonsOnLongClickListener(audios.get(i)));


                //Añadimos los botones a la botonera
                lScroll.addView(button);


            }




        }
    }



    class ButtonsOnClickListener  implements View.OnClickListener {

        private String name;

        public ButtonsOnClickListener(String name) {
            this.name = name;

        }

        @Override
        public void onClick(View v) {
            if (Player.getInstance() != null)
                Player.getInstance().mpNull();
            Player.getInstance().selectAudio(getActivity().getBaseContext(), name);
            try {
                Player.getInstance().start();
            } catch (IllegalStateException e) {
                Log.e("IllegalStateException", "Illegal State Exception: " + e.getMessage());
            }


        }
    }



    class ButtonsOnLongClickListener implements View.OnLongClickListener {

        private String name;

        public ButtonsOnLongClickListener(String name) {
            this.name = name;
        }

        @Override
        public boolean onLongClick(View v) {

            Player.getInstance().pause();
            pressed = name;

            final Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.custom_dialog_options);
            dialog.show();


            Button btnFav = dialog.findViewById(R.id.btnFav);
            Button btnRingtone = dialog.findViewById(R.id.btnLlamada);
            Button btnShare = dialog.findViewById(R.id.btnCompartir);
            Button btnCancel = dialog.findViewById(R.id.btnCancelar);
            Button btnAlarm = dialog.findViewById(R.id.btnAlarm);
            Button btnNotification = dialog.findViewById(R.id.btnNotification);


            openDB();
            if(bd.isFavourite(pressed,person))
                btnFav.setText("Quitar de favoritos");

            closeDB();

            btnFav.setTextSize(18);
            btnRingtone.setTextSize(18);
            btnShare.setTextSize(18);
            btnCancel.setTextSize(18);
            btnAlarm.setTextSize(18);
            btnNotification.setTextSize(18);


            btnFav.setTextColor(getResources().getColor(R.color.black));
            btnRingtone.setTextColor(getResources().getColor(R.color.black));
            btnShare.setTextColor(getResources().getColor(R.color.black));
            btnCancel.setTextColor(getResources().getColor(R.color.black));
            btnAlarm.setTextColor(getResources().getColor(R.color.black));
            btnNotification.setTextColor(getResources().getColor(R.color.black));



            Typeface typeface = ResourcesCompat.getFont(getContext(),R.font.indieflower);

            btnFav.setTypeface(typeface);
            btnRingtone.setTypeface(typeface);
            btnShare.setTypeface(typeface);
            btnCancel.setTypeface(typeface);
            btnAlarm.setTypeface(typeface);
            btnNotification.setTypeface(typeface);


            btnFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast toast;
                    openDB();
                    if(!bd.isFavourite(pressed,person)) {
                        bd.addFavorite(pressed, person);
                        toast = Toast.makeText(getContext(), "Se ha añadido a favoritos", Toast.LENGTH_SHORT);
                    }else {
                        bd.removeFavorite(pressed, person);
                        toast = Toast.makeText(getContext(), "Se ha eliminado de favoritos", Toast.LENGTH_SHORT);

                        if(isFavouriteFragment) {

                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            AudiosFragment af = new AudiosFragment();
                            Bundle args = new Bundle();

                            if(bd.getAllFavorites(person).size()>0) {
                                args.putBoolean("favourite", true);
                            }else{
                                args.putBoolean("favourite", false);
                            }

                            af.setArguments(args);
                            fm.beginTransaction().replace(R.id.escenario, af).commit();

                        }
                    }
                    closeDB();


                    toast.show();

                    dialog.dismiss();


                }
            });

            btnRingtone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    if(!havePermissons()) {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ACCESS_FINE);

                    }else{
                        copyFile();


                        RingtoneManager.setActualDefaultRingtoneUri(
                                getActivity(),
                                RingtoneManager.TYPE_RINGTONE,
                                writeDB()
                        );

                        Toast toast = Toast.makeText(getContext(),"Se ha establecido un nuevo tono de llamada",Toast.LENGTH_SHORT);
                        toast.show();

                        dialog.dismiss();
                    }
                }
            });


            btnAlarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(!havePermissons()) {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ACCESS_FINE);

                    }else{
                        copyFile();


                        RingtoneManager.setActualDefaultRingtoneUri(
                                getActivity(),
                                RingtoneManager.TYPE_ALARM,
                                writeDB()
                        );

                        Toast toast = Toast.makeText(getContext(),"Se ha establecido un nuevo tono de alarma",Toast.LENGTH_SHORT);
                        toast.show();

                        dialog.dismiss();
                    }

                }
            });

            btnNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(!havePermissons()) {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ACCESS_FINE);

                    }else{
                        copyFile();


                        RingtoneManager.setActualDefaultRingtoneUri(
                                getActivity(),
                                RingtoneManager.TYPE_NOTIFICATION,
                                writeDB()
                        );

                        Toast toast = Toast.makeText(getContext(),"Se ha establecido un nuevo tono de notificación",Toast.LENGTH_SHORT);
                        toast.show();

                        dialog.dismiss();
                    }

                }
            });

            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(!havePermissons()) {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ACCESS_FINE);

                    }else {
                        copyFile();
                        writeDB();

                        String sharePath = path + filename;
                        Uri uri = Uri.parse(sharePath);
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("audio/mp3");
                        share.putExtra(Intent.EXTRA_STREAM, uri);
                        startActivity(Intent.createChooser(share, "Share Sound File"));

                        dialog.dismiss();
                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });




            return true;
        }

    }

    private void copyFile(){


        byte[] buffer = null;
         int rawID = getContext().getResources().getIdentifier(pressed, "raw", getContext().getPackageName());

        InputStream fIn = getActivity().getBaseContext().getResources().openRawResource(
                rawID);
        int size = 0;

        try {
            size = fIn.available();
            buffer = new byte[size];
            fIn.read(buffer);
            fIn.close();
        } catch (IOException e) {
            return ;
        }

        filename = pressed+Math.random()+".mp3";

        boolean exists = (new File(path)).exists();
        if (!exists) {
            new File(path).mkdirs();
        }

        FileOutputStream save;
        try {
            save = new FileOutputStream(path + filename);
            save.write(buffer);
            save.flush();
            save.close();
        } catch (FileNotFoundException e) {
            return ;
        } catch (IOException e) {
            return ;
        }
    }


    private Uri writeDB(){

        File k = new File(path,filename);


        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, 215454);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.DURATION, 230);
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        //Insert it into the database
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(k.getAbsolutePath());
        return getActivity().getContentResolver().insert(uri, values);

    }

    private boolean havePermissons(){
        if(!Settings.System.canWrite(getContext())){
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            startActivity(intent);

            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ) {
                return false;
            }

        }
        return true;
    }



    /**
     * Método que se conecta a la base de datos
     */
    public void openDB(){
        bd = new FavouritesDataSource(getActivity().getApplicationContext());
        bd.open();
    }

    /**
     * Método que cierra la base de datos
     */
    public void closeDB(){
        bd.close();
    }


}