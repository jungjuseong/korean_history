package com.exam.realestate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

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
            while (reader.ready()) {
                String line = reader.readLine();
                String questions = "";
                if (line.startsWith("####")) {
                    cnt++;
                    System.out.printf("%d:%s\n", cnt, line.substring(5));

                    Matcher matcher = idPattern.matcher(line.substring(5));

                    boolean found = false;
                    StringBuilder message = new StringBuilder();
                    String id = "";
                    String round = "";
                    String number = "";
                    while(matcher.find()) {
                        round = matcher.group(1);
                        number = matcher.group(2);

                        message.append("텍스트 \"")
                                .append(matcher.group(1))      // 찾은 문자열 그룹 입니다.
                                .append("\"를 찾았습니다.\n")
                                .append("인덱스 ")
                                .append(matcher.start())      // 찾은 문자열의 시작 위치 입니다.
                                .append("에서 시작하고, ")
                                .append(matcher.end())        // 찾은 문자열의 끝 위치 입니다.
                                .append("에서 끝납니다.\n");
                        found = true;
                        System.out.printf("%s:%s\n", round, number);
                    }
                    questions = "";

                } else if (line.startsWith("====")) {
                    var samples = "";
                    while (reader.ready()) {
                        String sample = reader.readLine();
                        if (sample.startsWith("====")) {
                            break;
                        }
                        else {
                            samples += sample + "\n";
                        }
                    }
                    System.out.printf("예시%d:%s\n", cnt, samples);
                }
                else if (line.startsWith("①") || line.startsWith("②") ||
                        line.startsWith("③") || line.startsWith("④") || line.startsWith("⑤")) {
                    questions += line + "\n";
                    System.out.printf("%d:문항 %s\n", cnt, line);
                }
            }
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }
}
