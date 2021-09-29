package com.exam.realestate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.print.DocFlavor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocParser {

    final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public static void firstStage(ResourceLoader resourceLoader) throws Exception
    {
        Resource resource = resourceLoader.getResource("classpath:real_estate_exam.adoc");
        InputStream inputStream = resource.getInputStream();

        final Pattern idPattern = Pattern.compile("\\[([0-9]+)-([0-9]+)\\]");
        try
        {
            var reader = new BufferedReader(new InputStreamReader(inputStream));
            int cnt = 0;

            var sql = new StringBuilder("");
            var questions = new StringBuilder("");

            while (reader.ready()) {
                String line = reader.readLine();

                if (line.startsWith("####")) {

                    line.replaceAll("'","------------------------");
                    cnt++;
                    sql.append("INSERT INTO real_estate_exam(round, question_number, sample, choices)\n");
                    sql.append("VALUES(");

                    Matcher matcher = idPattern.matcher(line.substring(5));

                    String round = "";
                    String question_number = "";
                    while(matcher.find()) {
                        round = matcher.group(1);
                        question_number = matcher.group(2);
                        sql.append(String.format("'%s',",round))
                                .append(String.format("'%s',",question_number));
                    }

                } else if (line.startsWith("====")) {
                    var samples = "";
                    while (reader.ready()) {
                        String sample = reader.readLine();
                        if (sample.startsWith("====")) {
                            break;
                        }
                        else {
                            samples += sample + "\\n";
                        }
                    }
                    samples.replaceAll("''","------------------------");
                    sql.append(String.format("'%s',", samples));
                }
                else if (line.startsWith("①") || line.startsWith("②") ||
                        line.startsWith("③") || line.startsWith("④") || line.startsWith("⑤")) {

                    line.replaceAll("'","------------------------");
                    questions.append(line + "\\n");

                    if (line.startsWith("⑤")) {
                        sql.append(String.format("'%s'", questions));
                        sql.append(");\n");
                        System.out.println(sql);

                        questions = new StringBuilder("");
                        sql = new StringBuilder("");
                    }
                }
            }
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }

    public static void  removeQuotation(String a) {
        final Pattern quotationPattern = Pattern.compile("(['])");

        Matcher matcher = quotationPattern.matcher(a);

        String q = "";
        while(matcher.find()) {
            q = matcher.group();

            System.out.printf("---> %s\n", a);
        }
    }
}
