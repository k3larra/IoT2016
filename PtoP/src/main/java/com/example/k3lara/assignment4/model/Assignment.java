package com.example.k3lara.assignment4.model;

/**
 * Created by K3LARA on 2015-09-06.
 */
public class Assignment {
    public enum STATE {
        NOT_STARTED, STARTED, FAILED, SUCCESFULL
    }

    private String question;
    private String[] answers = new String[3];
    private STATE state;
    private int correctanswer_nbr;

    public Assignment(String question, String answer_nbr_0, String answer_nbr_1, String answer_nbr_2, int correctanswer_nbr) {
        this.question = question;
        this.answers[0] = answer_nbr_0;
        this.answers[1] = answer_nbr_1;
        this.answers[2] = answer_nbr_2;
        if (correctanswer_nbr>-1&&correctanswer_nbr<3){
            this.correctanswer_nbr = correctanswer_nbr;
        }else{
            this.correctanswer_nbr = 1;
        }
        state = STATE.NOT_STARTED;
    }

    public Assignment() {
        this("question", "answer1", "answer2", "answer3", 0);
    }

    public String getQuestion() {
        return question;
    }

    public String getCorrectanswer() {
        return answers[correctanswer_nbr];
    }

    public String getAnswer ( int i){
        return this.answers[i];
    }

    /**0 is first question*/
    public void setAnswer(int alternative){
        if (alternative==correctanswer_nbr){
            state = STATE.SUCCESFULL;
        }else{
            state = STATE.FAILED;
        }
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state){
        this.state = state;
    }
}
