package com.fyp.lun.rehabpartner;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/* Files class to handle reading and writing of data to files */
public class Files {

    Context context;

    public Files(Context context){
        this.context = context;
    }

    public void clear(final String path){
        File dir = context.getFilesDir();
        File files = new File(dir, path);
        boolean deleted = files.delete();
    }

    public void write(final String path, final String[] text){
        write(path, text[0]);
        for(int i=1; i<text.length; i++){
            append(path, text[i]);
        }
    }

    public void writeList(final String path, final ArrayList<String> text)
    {
        FileOutputStream outputStream = null;
        try
        {
            outputStream = context.openFileOutput(path, Context.MODE_PRIVATE);
            for(String s: text)
            {
                outputStream.write(s.getBytes());
                //outputStream.write(System.getProperty("line.separator").getBytes());
            }
            outputStream.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void append(final String path, final String[] text){
        for(String s : text){
            append(path, s);
        }
    }

    public String[] read(final String path){
        ArrayList<String> list = readArr(path);
        return list.toArray(new String[list.size()]);
    }

    public void append(final String path, final String text){
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(path, Context.MODE_APPEND);
            if(read(path)!= null)
            {
                outputStream.write(System.getProperty("line.separator").getBytes());
            }
            outputStream.write(text.getBytes());
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendList(final String path, final ArrayList<String> text)
    {
        FileOutputStream outputStream;
        try
        {
            outputStream = context.openFileOutput(path, Context.MODE_APPEND);
            if(!readArr(path).isEmpty())
            {
                outputStream.write(System.getProperty("line.separator").getBytes());
            }
            for(String s : text)
            {
                outputStream.write(s.getBytes());
            }
            outputStream.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void write(final String path, final String text){
        FileOutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput(path, Context.MODE_PRIVATE);

            outputStream.write(text.getBytes());
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> readArr(String path){
        ArrayList<String> text = new ArrayList<String>();

        FileInputStream inputStream;
        try {
            inputStream = context.openFileInput(path);

            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(isr);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                text.add(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return text;
    }

    public boolean isEmpty(final String path){
        return (readArr(path).size()==0);
    }

}
