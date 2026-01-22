package com.example.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;

public class AnalyzerDemo {

    public static void main(String[] args) throws IOException {

        // 1️⃣ Input text
        String text = "Lucene is a Powerful Search-Engine library, developed by Apache!";

        // 2️⃣ Create Analyzer
        Analyzer analyzer = new StandardAnalyzer();

        // 3️⃣ Create TokenStream
        TokenStream tokenStream = analyzer.tokenStream("field", new StringReader(text));

        // 4️⃣ Attribute to read tokens
        CharTermAttribute charTermAttribute =
                tokenStream.addAttribute(CharTermAttribute.class);

        // 5️⃣ Start tokenization
        tokenStream.reset();

        System.out.println("Tokens produced by Analyzer:\n");


        while (tokenStream.incrementToken()) {
            System.out.println(charTermAttribute.toString());
        }

        // 6️⃣ Close resources
        tokenStream.end();
        tokenStream.close();
        analyzer.close();
    }
}
