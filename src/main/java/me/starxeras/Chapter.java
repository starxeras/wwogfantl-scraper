package me.starxeras;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Chapter {

    private final String url;
    private Document document;
    private String title;
    private final StringBuilder content = new StringBuilder();
    private StringBuilder formattedContent = new StringBuilder();
    /*
    { [1]:
        {footnote:"", charPosition:""}
     }
     */
    private LinkedHashMap<String, List<String>> footnotes = new LinkedHashMap<>();

    public Chapter(String url) {
        this.url = url;
        try {
            this.document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getInfo();


    }

    private void getInfo() {
        // content
        Elements p = this.document.select("p").not("p[class=alignright], p[class=alignleft], p > a, time");
        for (int i = 0; i < p.size(); i++) {
            if (i == 0 || i == 1) { continue; }
            this.content.append(p.get(i).toString().replace("<p>", "").replace("</p>", "")
                    .replace("<em>", "*").replace("</em>", "*").replace("<strong>", "**").replace("</strong>", "**")).append('\n');
        }
        // title
        Elements h1 = this.document.select("h1");
        for (int i = 0; i < h1.size(); i++) {
            if (i == 0) { continue; }
            this.title = h1.get(i).toString().replace("\uD83D\uDCD6", "")
                    .replace("<h1>", "").replace("</h1>", "").strip();
        }

        // footnotes
        LinkedHashMap<Integer, List<Integer>> positions = new LinkedHashMap<>();
        List<String> fn = new ArrayList<>();
        int footnoteNumber = 0;
        for (int i = 0; i < this.content.toString().length() - 1; i++) {
            if (this.content.charAt(i) == '[') {
                List<Integer> posList = new ArrayList<>();
                posList.add(i - 51);
                posList.add(i + 13);
                fn.add(String.valueOf(this.content.charAt(i + 1)));
                footnoteNumber++;
                positions.put(footnoteNumber, posList);
            }
        }
        this.formattedContent = new StringBuilder(this.content.toString());
        // remove footnote tags and replace it with more readable format
        // example: <sup class="footnote"><a href="#fn-2" id="fnref=2">[2]</a></sup> to [2]
        int offset = 0;
        for (int i = 1; i <= fn.size(); i++) {
            if (i > 1) offset += 61;
            this.formattedContent.delete(positions.get(i).getFirst() - offset, positions.get(i).getLast() - offset);
            this.formattedContent.insert(positions.get(i).getFirst() - offset, "[" + fn.get(i - 1) + "]");
        }
    }

    /**
     * Returns the content from the chapter, without special footnote formatting.
     * @see Chapter#getFormattedContent()
     * @return String
     */
    public String getContent() {
        return this.content.toString();
    }

    public String getTitle() {
        return this.title;
    }

    public String getUrl() {
        return this.url;
    }

    public String getDocument() {
        return this.document.toString();
    }

    /**
     * Returns the content from the special, <em>with</em> special foonote formatting.
     * Example: example: <code>sup class="footnote">a href="#fn-2" id="fnref=2"></a></sup></code> to [2]
     * @see Chapter#getContent()
     * @return String
     */
    public String getFormattedContent() {
        return this.formattedContent.toString();
    }

}
