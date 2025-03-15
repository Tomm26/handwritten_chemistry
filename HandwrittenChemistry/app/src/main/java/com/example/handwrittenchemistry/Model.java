package com.example.handwrittenchemistry;

import android.content.Context;

import com.example.handwrittenchemistry.ml.ConvertedModelV12;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Model {

    private List<Mat> images;
    private Context context;
    private ConvertedModelV12 model;
    private String []dic = {"(", ")", "0", "1", "2", "3", "4","5","6","7","8","9","A","b","C","d","e","f","G","H","i","j","k","l","M","N","o","p","plus","q","R","rightarrow","S","T","u","v","w","X","y","z"};

    public Model(List<Mat> images, Context context) throws IOException {
        this.images = images;
        this.model = ConvertedModelV12.newInstance(context);
        this.context = context;
        Arrays.sort(dic);
    }

    public List<String> getPredictions(Mat img, int maxs){

        Map<String, Float> r = new HashMap<>();
        List<String> res = new ArrayList<>();
        int c = 0;

        img.convertTo(img, CvType.CV_32S);
        int[] rgba = new int[(int)(img.total()*img.channels())];
        img.get(0,0,rgba);
        TensorBuffer input = TensorBuffer.createFixedSize(new int[]{1, 45, 45, 1}, DataType.FLOAT32);
        input.loadArray(rgba,new int[]{1, 45, 45, 1});

        ConvertedModelV12.Outputs outputs = model.process(input);
        TensorBuffer outputFeature = outputs.getOutputFeature0AsTensorBuffer();
        float[] results = outputFeature.getFloatArray();
        //model.close();

        for(int i=0;i<results.length;i++)
            r.put(dic[i], results[i]);

        r = sortByValue(r);

        for(Map.Entry<String, Float> e: r.entrySet())
            if(c<maxs){
                if(e.getKey().equals("plus"))
                    res.add("+");
                else if(e.getKey().equals("rightarrow"))
                    res.add("=");
                else
                    res.add(e.getKey());
                c++;
            }

        return res;
    }

    private List<String> readElems() throws IOException {
        //reads all the elems from file
        List<String> elems = new ArrayList<>();
        InputStream forms = context.getResources().openRawResource(R.raw.periodictable);
        BufferedReader br = new BufferedReader(new InputStreamReader(forms, "UTF-8"));

        for(String line = br.readLine(); line != null; line = br.readLine())
            elems.add(line.trim());

        return elems;
    }
    private String errCorrection(String temp, List<String> cl, boolean cbl, boolean cbn, String df){
        String res="";
        try{
            List<String> elems = readElems();
            List<String> firstChar = new ArrayList<>();
            boolean could_be_letter = cbl;
            boolean could_be_number = cbn;

            for(String e: elems)
                firstChar.add(e.charAt(0)+"");

            if(cl.isEmpty())
                return df;

            if(Objects.equals(cl.get(0), "0"))
                return "O";

            if(cl.size()== 1 && cl.get(0).matches("[a-zA-Z]")){
                //symbol is letter, if it's in the table (like O) return,
                //else check if with the previous is in table
                if(elems.contains(cl.get(0)) || (firstChar.contains(cl.get(0).toUpperCase())))
                    return cl.get(0);
                else if (!temp.isEmpty() &&
                        elems.contains(temp.charAt(temp.length()-1)+cl.get(0).toLowerCase()))
                    return cl.get(0);
                else
                    could_be_letter = false;
            }

            if(cl.size() == 1 && cl.get(0).matches("[0-9]+")) {
                //symbol is number, if it's at the beginning or is not after a ) or after an elem then it could not be num
                if (temp.isEmpty() ||
                        (!elems.contains(temp.charAt(temp.length()-1)+"") && temp.charAt(temp.length()-1) == ')'
                                && temp.length()>1 && !elems.contains(temp.substring(temp.length()-2, temp.length()-1)))
                )
                    could_be_number = false;
                else
                    return cl.get(0);
            }
            if (could_be_letter && could_be_number){
                //it's neither a letter nor a number
                return cl.get(0); //for now...
            }
            return errCorrection(temp, cl.subList(1, cl.size()), could_be_letter, could_be_number, df);

        }catch(Exception e){
            return df;
        }

    }

    //sort a map by value (descending
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public String getStringPredicted(){

        String temp = "";
        String res = "";
        for(int i=0;i<images.size();i++){

            List<String> pr = getPredictions(images.get(i), 3);
            //res+=pr.get(0)+" ";
            temp+=errCorrection(temp, pr, true, true, pr.get(0));
        }

        for(int i=0;i<temp.length();i++){
            if(temp.charAt(i)=='+' || temp.charAt(i)=='=')
                res+=" "+temp.charAt(i)+" ";
            else
                res+=temp.charAt(i);
        }
        return res;
    }
}
