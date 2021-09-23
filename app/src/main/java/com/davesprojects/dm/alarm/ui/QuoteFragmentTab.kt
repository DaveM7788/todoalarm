package com.davesprojects.dm.alarm.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.davesprojects.dm.alarm.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class QuoteFragmentTab : Fragment() {
    var con: Context? = null
    var myView: View? = null
    var numOfQuotes = 71
    var quoteTV: TextView? = null

    // add quotes text file to assets
    // load text file
    // choose random line from text file
    // display that line
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.quote_layout, container, false)
        con = myView?.context
        quoteTV = myView?.findViewById(R.id.textViewQuote)
        quoteTV?.text = motQuote()
        return myView
    }

    private fun motQuote(): String {
        var quote = ""
        val rn = Random()
        val lineNumberOfQ = rn.nextInt(numOfQuotes) + 1 // nextInt(max - min + 1) + min (where min = 1)
        try {
            val stream = con?.assets?.open("quotes.txt")
            val br = BufferedReader(InputStreamReader(stream))
            for (i in 0 until lineNumberOfQ - 1) {
                br.readLine()
            }
            quote = br.readLine()
        } catch (e: IOException) {
            Toast.makeText(con, "Input Output Exception", Toast.LENGTH_SHORT).show()
        }
        return quote
    }
}