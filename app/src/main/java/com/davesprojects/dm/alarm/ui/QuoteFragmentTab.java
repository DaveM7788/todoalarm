package com.davesprojects.dm.alarm.ui;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.davesprojects.dm.alarm.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import androidx.fragment.app.Fragment;

public class QuoteFragmentTab extends Fragment {
    Context con;
    View myView;
    int numOfQuotes = 71;
    TextView quoteTV;

    // add quotes text file to assets
    // load text file
    // choose random line from text file
    // display that line

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.quote_layout, container, false);
        con = myView.getContext();

        quoteTV = myView.findViewById(R.id.textViewQuote);
        quoteTV.setText(motQuote());

        return myView;
    }

    public String motQuote() {
        String quote = "";
        Random rn = new Random();
        int lineNumberOfQ = rn.nextInt(numOfQuotes) + 1; // nextInt(max - min + 1) + min (where min = 1)

        try {
            InputStream stream = con.getAssets().open("quotes.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));

            for (int i = 0; i < lineNumberOfQ - 1; i++) {
                br.readLine();
            }
            quote = br.readLine();
        } catch (IOException e) {
            Toast.makeText(con, "Input Output Exception", Toast.LENGTH_SHORT).show();
        }

        return quote;
    }
}
