import java.io.*;
import java.util.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.text.*;
import com.google.gson.*;

public class pdf_outline {

    static class LineInfo {
        String text;
        float fontSize;
        boolean isBold;
        int page;
        float y;

        LineInfo(String text, float fontSize, boolean isBold, int page, float y) {
            this.text = text;
            this.fontSize = fontSize;
            this.isBold = isBold;
            this.page = page;
            this.y = y;
        }
    }

    static class OutlineEntry {
        String level;
        String text;
        int page;

        OutlineEntry(String level, String text, int page) {
            this.level = level;
            this.text = text;
            this.page = page;
        }
    }

    static class OutputJson {
        String title = null;
        List<OutlineEntry> outline = new ArrayList<>();
    }

    public static void main(String[] args) throws IOException {
        File inputFolder = new File("./input");
        if (!inputFolder.exists()) {
            System.out.println("Input folder not found.");
            return;
        }

        for (File file : inputFolder.listFiles()) {
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                System.out.println("Skipping non-PDF file: " + file.getName());
                continue;
            }

            System.out.println("\nProcessing: " + file.getName());
            try {
                extractFontInfo(file);
            } catch (Exception e) {
                System.out.println("Error processing file " + file.getName() + ": " + e.getMessage());
            }
            System.out.println("--------------------------------------------------");
        }
    }

    public static void extractFontInfo(File file) throws IOException {
        PDDocument doc = PDDocument.load(file);
        List<LineInfo> allLines = new ArrayList<>();
        Map<String, Integer> lineFreq = new HashMap<>();
        Set<Float> paragraphFontSizes = new HashSet<>();

        PDFTextStripper stripper = new PDFTextStripper() {
            int currentPage = 1;

            @Override
            protected void startPage(PDPage page) throws IOException {
                super.startPage(page);
                currentPage = getCurrentPageNo();
            }

            @Override
            protected void writeString(String text, List<TextPosition> positions) throws IOException {
                if (text.trim().isEmpty()) return;

                float y = positions.get(0).getY();
                if (y < 50 || y > 750) return; // Skip headers/footers

                float maxFontSize = 0;
                boolean isBold = false;
                for (TextPosition pos : positions) {
                    maxFontSize = Math.max(maxFontSize, pos.getFontSizeInPt());
                    if (pos.getFont().getName().toLowerCase().contains("bold")) {
                        isBold = true;
                    }
                }

                String line = text.trim();
                allLines.add(new LineInfo(line, maxFontSize, isBold, currentPage, y));

                if (line.length() > 100) {
                    paragraphFontSizes.add(maxFontSize);
                }

                if (currentPage <= 4) {
                    lineFreq.put(line, lineFreq.getOrDefault(line, 0) + 1);
                }
            }
        };

        stripper.setSortByPosition(true);
        stripper.setStartPage(1);
        stripper.setEndPage(doc.getNumberOfPages());
        stripper.getText(doc);

        Set<String> headersFooters = new HashSet<>();
        lineFreq.forEach((line, count) -> {
            if (count >= 3) headersFooters.add(line);
        });

        List<LineInfo> lines = new ArrayList<>();
        for (LineInfo li : allLines) {
            if (!headersFooters.contains(li.text)) lines.add(li);
        }

        Set<Float> nonParaSizes = new HashSet<>();
        for (LineInfo li : lines) {
            if (paragraphFontSizes.stream().noneMatch(p -> li.fontSize <= p)) {
                nonParaSizes.add(li.fontSize);
            }
        }

        List<Float> sortedFontSizes = new ArrayList<>(nonParaSizes);
        sortedFontSizes.sort(Comparator.reverseOrder());

        String[] levels = {"Title", "H1", "H2", "H3", "H4", "H5"};
        Map<Float, String> fontLevelMap = new HashMap<>();
        for (int i = 0; i < sortedFontSizes.size(); i++) {
            fontLevelMap.put(sortedFontSizes.get(i), i < levels.length ? levels[i] : "H5");
        }

        OutputJson output = new OutputJson();
        float titleFontSize = -1;
        int titlePage = 0;
        boolean titleFound = false;

        List<LineInfo> merged = new ArrayList<>();
        for (int i = 0; i < lines.size(); ) {
            LineInfo base = lines.get(i);
            StringBuilder txt = new StringBuilder(base.text);
            float maxSize = base.fontSize;
            float y = base.y;
            int pg = base.page;
            boolean isBold = base.isBold;

            int j = i + 1;
            while (j < lines.size()) {
                LineInfo nxt = lines.get(j);
                if (Math.abs(nxt.fontSize - base.fontSize) < 1.0 && nxt.page == pg && Math.abs(nxt.y - y) < 40) {
                    txt.append(" ").append(nxt.text);
                    maxSize = Math.max(maxSize, nxt.fontSize);
                    y = nxt.y;
                    j++;
                } else break;
            }

            merged.add(new LineInfo(txt.toString().trim(), maxSize, isBold, pg, base.y));
            i = j;
        }

        for (int i = 0; i < merged.size(); i++) {
            LineInfo li = merged.get(i);
            String originalText = li.text;
            String text = originalText;

            // Exclude if bold followed by paragraph-looking text
            if (li.isBold && text.length() > 100) continue;

            // Cut off at ':' or ':-'
            // int colon = text.indexOf(":");
            // int dashColon = text.indexOf(":-");
            // int cut = colon;
            // if (dashColon != -1 && (colon == -1 || dashColon < colon)) cut = dashColon;
            // if (cut != -1) text = text.substring(0, cut).trim();

            String level = fontLevelMap.getOrDefault(li.fontSize, "H5");

            if (level.equals("Title") && li.page <= 4) {
                if (!titleFound) {
                    output.title = text;
                    titleFontSize = li.fontSize;
                    titlePage = li.page;
                    titleFound = true;
                } else if (Math.abs(li.fontSize - titleFontSize) < 1.0 && li.page == titlePage) {
                    output.title += " " + text;
                }
                continue;
            }

            if (!titleFound || li.page < titlePage) continue;
            if (paragraphFontSizes.contains(li.fontSize)) continue;

            boolean isValid = false;
            for (int k = i + 1; k < merged.size(); k++) {
                LineInfo next = merged.get(k);
                if (next.page != li.page) break;

                if (paragraphFontSizes.contains(next.fontSize)) {
                    isValid = true;
                    break;
                }

                String nextLevel = fontLevelMap.getOrDefault(next.fontSize, "H5");
                int curIdx = Arrays.asList(levels).indexOf(level);
                int nextIdx = Arrays.asList(levels).indexOf(nextLevel);
                if (nextIdx > curIdx) {
                    isValid = true;
                    break;
                }
            }

            if (isValid && !text.isEmpty()) {
                output.outline.add(new OutlineEntry(level, text, li.page));
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(output);
        String pdfName = file.getName().replaceAll("(?i)\\.pdf$", "");
        try (FileWriter writer = new FileWriter("./output/" + pdfName + ".json")) {
            writer.write(jsonOutput);
            System.out.println("Saved JSON to ./output/" + pdfName + ".json");
        } catch (IOException e) {
            System.err.println("Failed to write JSON file: " + e.getMessage());
        }

        doc.close();
    }
}
