// package com.lcmonitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class DocWriter {
    public static void main(String[] args) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            BufferedWriter byw = new BufferedWriter(new OutputStreamWriter(bytes));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("numbers.txt")));
            String line = null;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                byw.write(line);
                bw.newLine();
            }
            System.out.println("The document has been written successfully.");
            byw.flush();
            bw.write("This document contains " + bytes.size() + " bytes in total.");
            bw.flush();
            bw.close();
            byw.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}