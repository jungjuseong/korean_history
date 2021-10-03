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

    private static String getId(String line) {
        final Pattern idPattern = Pattern.compile("\\[([0-9]+)-([0-9]+)\\]");

        Matcher matcher = idPattern.matcher(line.substring(5));

        String round = "";
        String question_number = "";
        while(matcher.find()) {
            round = matcher.group(1);
            question_number = matcher.group(2);
        }

        return round + "-" + question_number;
    }

    private static int getIdLength(String line) {
        final Pattern idPattern = Pattern.compile("\\[([0-9]+)-([0-9]+)\\]");

        Matcher matcher = idPattern.matcher(line.substring(5));

        int len = 0;
        while(matcher.find()) {
            len = matcher.group().length();
        }

        return len;
    }

    private final String Course = "부동산학개론";

    public static void firstStage(ResourceLoader resourceLoader, String course, String doc) throws Exception
    {
        Resource resource = resourceLoader.getResource("classpath:" + doc);
        InputStream inputStream = resource.getInputStream();

        try
        {
            var reader = new BufferedReader(new InputStreamReader(inputStream));
            var sql = new StringBuilder("");
            var choices = new StringBuilder("");
            var hasExample = false;
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.startsWith("####")) {
                    hasExample = false;
                    choices = new StringBuilder("");
                    sql = new StringBuilder("");

                    //line.replaceAll("'","------------------------");
                    sql.append("INSERT INTO real_estate_brokerage_exam(course, question_id, question, example, choices)\n");
                    sql.append("VALUES(");
                    sql.append(String.format("'%s','%s',",course, getId(line)));
                    sql.append(String.format("'%s',",line.substring(5 + getIdLength(line) + 1)));

                } else if (line.startsWith("====")) {
                    var examples = "";
                    while (reader.ready()) {
                        String sample = reader.readLine();
                        if (sample.startsWith("====")) {
                            break;
                        }
                        else {
                            examples += sample + "\\n";
                        }
                    }
                    //samples.replaceAll("''","------------------------");
                    sql.append(String.format("'%s',", examples));
                    hasExample = true;
                }
                else if (line.startsWith("①") || line.startsWith("②") ||
                        line.startsWith("③") || line.startsWith("④") || line.startsWith("⑤")) {

                    //line.replaceAll("'","------------------------");
                    choices.append(line + "\\n");

                    if (line.startsWith("⑤")) {
                        if (hasExample) {
                            sql.append(String.format("'%s'", choices));
                        }
                        else {
                            sql.append(String.format("'','%s'", choices));
                        }
                        sql.append(");\n");
                        System.out.println(sql);
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
