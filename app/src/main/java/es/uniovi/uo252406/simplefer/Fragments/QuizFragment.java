package es.uniovi.uo252406.simplefer.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import es.uniovi.uo252406.simplefer.Entities.Question;
import es.uniovi.uo252406.simplefer.Parser;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_quiz, container, false);


        //Recogemos los datos
        Bundle b = getActivity().getIntent().getExtras();
        person = (String) b.getString("person");


        try {
            questions = Parser.getInstance().parse(getJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        prepareComponents();
        startQuiz();

        return view;
    }

    /**
     * Localiza los componentes del QuizFragment
     */
    private void prepareComponents(){

        question = view.findViewById(R.id.textQuestion);
        option1 = view.findViewById(R.id.btnO1);
        option2 = view.findViewById(R.id.btnO2);
        option3 = view.findViewById(R.id.btnO3);

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


        option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(1);
            }
        });

        option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(2);
            }
        });

        option3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(3);
            }
        });


    }

    /**
     * Escribe la primera pregunta del quiz y sus respuestas
     */
    private void startQuiz() {

        question.setText(questions.get(actualQuestion).getQuestion());
        option1.setText("a) "+questions.get(actualQuestion).getOption1());
        option2.setText("b) "+questions.get(actualQuestion).getOption2());
        option3.setText("c) "+questions.get(actualQuestion).getOption3());

    }


    public StringBuilder getJson() {

        int rawID = getContext().getResources().getIdentifier(person+"quiz","raw",getContext().getPackageName());


        BufferedReader bR = new BufferedReader(new InputStreamReader(getResources().openRawResource(rawID)));
        StringBuilder sB = new StringBuilder();
        String linea = null;

        try {
            while ((linea = bR.readLine()) != null) {
                sB.append(linea).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sB;
    }



    private void checkAnswer(int selected){

        if(isCorrect(selected)){
            markCorrect();
        }else{

            switch (selected){

                case 1:
                    option1.setTextColor(getResources().getColor(R.color.red));
                    markCorrect();
                    break;

                case 2:
                    option2.setTextColor(getResources().getColor(R.color.red));
                    markCorrect();
                    break;

                case 3:
                    option3.setTextColor(getResources().getColor(R.color.red));
                    markCorrect();
                    break;

            }


        }


    }

    private void markCorrect() {

        switch (questions.get(actualQuestion).getAnswer()){

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
        return  questions.get(actualQuestion).getAnswer() == selected;
    }


}