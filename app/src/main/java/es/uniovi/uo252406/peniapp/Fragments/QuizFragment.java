package es.uniovi.uo252406.peniapp.Fragments;

import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONException;

import java.util.ArrayList;

import es.uniovi.uo252406.peniapp.Logical.Parser;
import es.uniovi.uo252406.peniapp.Logical.Player;
import es.uniovi.uo252406.peniapp.Logical.Entities.Question;
import es.uniovi.uo252406.simplefer.R;


public class QuizFragment extends android.support.v4.app.Fragment {

    private View view;

    private ArrayList<Question> questions;
    private String person;

    TextView question;
    Button option1;
    Button option2;
    Button option3;

    private int actualQuestion = 0;
    private int correctAnswers = 0;
    private View vView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_quiz, container, false);


        //Recogemos los datos
        Bundle b = getActivity().getIntent().getExtras();
        person = (String) b.getString("person");


        try {
            questions = Parser.getInstance().getQuiz(view,person);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        prepareComponents();
        writeQuiz();


        return view;
    }

    /**
     * Localiza los componentes del QuizFragment
     */
    private void prepareComponents(){


        vView = view.findViewById(R.id.vViewQuiz);

        vView.setVisibility(View.INVISIBLE);

        question = view.findViewById(R.id.textQuestion);
        option1 = view.findViewById(R.id.option1);
        option2 = view.findViewById(R.id.option2);
        option3 = view.findViewById(R.id.option3);

        question.setTextColor(getResources().getColor(R.color.white));
        option1.setTextColor(getResources().getColor(R.color.white));
        option2.setTextColor(getResources().getColor(R.color.white));
        option3.setTextColor(getResources().getColor(R.color.white));



        question.setTextSize(36);
        option1.setTextSize(24);
        option2.setTextSize(24);
        option3.setTextSize(24);

        question.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        option1.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        option2.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        option3.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

        Typeface typeface = ResourcesCompat.getFont(getContext(),R.font.indieflower);

        question.setTypeface(typeface);
        option1.setTypeface(typeface);
        option2.setTypeface(typeface);
        option3.setTypeface(typeface);

        option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Reviser().execute(1);
            }
        });

        option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Reviser().execute(2);
            }
        });

        option3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Reviser().execute(3);
            }
        });


    }

    /**
     * Escribe el Quiz con la pregunta actual
     */
    private void writeQuiz() {

        getActivity().runOnUiThread(new Runnable() {

            public void run() {
                option1.setEnabled(true);
                option2.setEnabled(true);
                option3.setEnabled(true);
            }
        });


        question.setText(questions.get(actualQuestion).getQuestion());
        option1.setText("a) "+questions.get(actualQuestion).getOption1());
        option2.setText("b) "+questions.get(actualQuestion).getOption2());
        option3.setText("c) "+questions.get(actualQuestion).getOption3());

    }


    /**
     *  Esta clase comprueba la opción seleccionada y actúa en consecuencia
     */
    private class Reviser extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            option1.setEnabled(false);
            option2.setEnabled(false);
            option3.setEnabled(false);
        }



        @Override
        protected Integer doInBackground(Integer... integers) {
            publishProgress(integers[0]);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... integer) {

            int selected = integer[0];

            if (isCorrect(selected)) {
                Player.getInstance().selectAudio(getContext(),"correct");
                correctAnswers++;
                markCorrect();
            } else {

                Player.getInstance().selectAudio(getContext(),"incorrect");

                if (selected == 1) {

                    option1.setTextColor(getResources().getColor(R.color.red));
                    markCorrect();

                } else if (selected == 2) {

                    option2.setTextColor(getResources().getColor(R.color.red));
                    markCorrect();

                } else if (selected == 3) {

                    option3.setTextColor(getResources().getColor(R.color.red));
                    markCorrect();

                }

            }

            Player.getInstance().start();
        }

            @Override
            protected void onPostExecute(Integer integer){
                if (actualQuestion < questions.size() - 1)
                    nextQuestion();
                else
                    finishQuiz();

            }

        }

        /**
         * Espera un tiempo corto y pasa a la siguiente pregunta
         */

        private void nextQuestion() {
            actualQuestion++;
            prepareComponents();
            writeQuiz();
        }


        private void markCorrect() {

            switch (questions.get(actualQuestion).getAnswer()) {

                case 1:
                    option1.setTextColor(getResources().getColor(R.color.green));
                    break;

                case 2:
                    option2.setTextColor(getResources().getColor(R.color.green));
                    break;

                case 3:
                    option3.setTextColor(getResources().getColor(R.color.green));
                    break;

            }

        }

        private boolean isCorrect(int selected) {
            return questions.get(actualQuestion).getAnswer() == selected;
        }


        private void finishQuiz() {
            actualQuestion = 0;

            if(correctAnswers == 1)
                question.setText("Has respondido " + correctAnswers + " pregunta bien de " + questions.size());
            else
                question.setText("Has respondido " + correctAnswers + " preguntas bien de " + questions.size());
            option1.setText("");
            option2.setText("");
            option3.setText("");

            if (correctAnswers == questions.size()) {
                Player.getInstance().selectAudio(getContext(), "quiz_perfect_"+person);
            } else if (correctAnswers >= 3 && correctAnswers < questions.size()) {
                Player.getInstance().selectAudio(getContext(), "quiz_good_" + person);
            } else {
                Player.getInstance().selectAudio(getContext(), "quiz_bad_" + person);
            }


            getActivity().runOnUiThread(new Runnable() {
                public void run() {


                    question.setEnabled(false);
                    option1.setEnabled(false);
                    option2.setEnabled(false);
                    option3.setEnabled(false);

                    VideoView vView = view.findViewById(R.id.vViewQuiz);
                    vView.setVisibility(View.VISIBLE);

                    int rawID = getContext().getResources().getIdentifier(person + "video", "raw", getContext().getPackageName());

                    Uri uri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + rawID);

                    vView.setVideoURI(uri);
                    vView.start();

                    Player.getInstance().start();

                    new Thread(new Runnable() {
                        public void run() {
                            VideoView vView = view.findViewById(R.id.vViewQuiz);
                            while (Player.getInstance().isPlaying()) {

                                if (!vView.isPlaying()) {
                                    vView.start();
                                }
                            }
                            vView.pause();
                            vView.seekTo(vView.getDuration());
                        }
                    }).start();


                }
            });
            correctAnswers = 0;
        }



}
