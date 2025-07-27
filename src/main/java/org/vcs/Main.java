package org.vcs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        Groot groot=new Groot(Path.of("."));
       groot.add(String.valueOf(Paths.get("sample.txt")));
       groot.add(String.valueOf(Paths.get("deepak.txt")));
       groot.commit("Third commit");
       groot.log();
        }
    }