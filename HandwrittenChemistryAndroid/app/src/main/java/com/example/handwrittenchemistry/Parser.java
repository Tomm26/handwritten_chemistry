package com.example.handwrittenchemistry;

import android.content.Context;
import android.graphics.Region;

import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Optional;
import java.util.Scanner;

public class Parser {

    private final Context context;
    private Tokenizer tk;
    private final Equation eq;
    private final HashMap<String, String> formule;

    public Parser(Context ctx) throws IOException{
        this.context = ctx;
        formule = new HashMap<>();
        loadFormule();
        eq = new Equation();
    }
    private void loadFormule() throws IOException{

        InputStream forms = context.getResources().openRawResource(R.raw.formule);
        BufferedReader br = new BufferedReader(new InputStreamReader(forms, "UTF-8"));

        String line;
        while ((line = br.readLine()) != null) {
            String[] res = line.split(":");
            if (res.length ==2)
                formule.put(res[1].trim(), res[0]);
        }

        br.close();

    }

    public void setTk(String s){ tk = new Tokenizer(s);}
    public Optional<String> isFormula(String s){

        return Optional.ofNullable(s); //should be formule.get(s)
    }

    public Optional<Integer> isNumber(String s){
        return s.matches("-?\\d+(\\.\\d+)?") ? Optional.of(Integer.parseInt(s)) : Optional.empty();
    }

    public Optional<String> isSide(int side){

        String tres="";
        int r1;
        Optional<String> r2;

        if(isNumber(tk.peekNextElement(1).get()).isPresent() && isFormula(tk.peekNextElement(2).get()).isPresent()){
            r1 = isNumber(tk.getNextToken().get()).get();
            String tf = tk.getNextToken().get();
            r2 = isFormula(tf);

            if (r1 > 0 && r2.isPresent()){
                tres+= String.valueOf(r1)+ " "+r2.get();
                eq.add(side, tf);
            }else
                return Optional.of(tres);

            if(tk.peekNextElement(1).isPresent() &&
                    tk.peekNextElement(1).get().equals("+")){

                tk.getNextToken();
                tres+= ", ";
                tres+=isSide(side).get();
            }
        } else if (isFormula(tk.peekNextElement(1).get()).isPresent()) {
            String tf = tk.getNextToken().get();
            r2 = isFormula(tf);

            if(r2.isPresent()){
                tres = "1 "+ r2.get();
                eq.add(side, tf);
            }else
                return Optional.of(tres);

            if(tk.peekNextElement(1).isPresent() &&
                    tk.peekNextElement(1).get().equals("+")){

                tk.getNextToken();
                tres+= ", ";
                tres+=isSide(side).get();
            }
        }

        return Optional.of(tres);
    }

    public String isEquation(){

        Optional<String> r1 = isSide(0);
        String r2 = tk.getNextToken().get();
        Optional<String> r3 = isSide(1);

        if(r1.isPresent() && r2.equals("=") && r3.isPresent())
            return "by adding "+r1.get()+ " you get "+r3.get();
        else
            return "String is not correctly formed!";
    }
    public String getFormule(){
        return formule.toString();
    }

    public Equation getEquation(){
        return eq;
    }

}