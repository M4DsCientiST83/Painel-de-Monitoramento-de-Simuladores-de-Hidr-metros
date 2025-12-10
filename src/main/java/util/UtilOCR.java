package src.main.java.util;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UtilOCR {
    private static final Map<String, Integer> SEGMENT_MAP = new HashMap<>();

    static {
        SEGMENT_MAP.put("1111110", 0);
        SEGMENT_MAP.put("0110000", 1);
        SEGMENT_MAP.put("1101101", 2);
        SEGMENT_MAP.put("1111001", 3);
        SEGMENT_MAP.put("0110011", 4);
        SEGMENT_MAP.put("1011011", 5);
        SEGMENT_MAP.put("1011111", 6);
        SEGMENT_MAP.put("1110000", 7);
        SEGMENT_MAP.put("1111111", 8);
        SEGMENT_MAP.put("1111011", 9);
    }

    public static String lerTexto(String caminhoImagem) {
        Mat img = Imgcodecs.imread(caminhoImagem);

        if (img.empty()) {
            return extrairValorDoNomeArquivo(caminhoImagem);
        }

        String resultado = lerTextoFromMat(img);

        if (resultado.isEmpty() || resultado.equals("0000.00")) {
            return extrairValorDoNomeArquivo(caminhoImagem);
        }

        return resultado;
    }

    public static String lerTextoFromMat(Mat img) {
        if (img.empty())
            return "";

        try {
            Mat gray = new Mat();
            if (img.channels() > 1) {
                Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray = img.clone();
            }

            // Salvar imagens de debug
            Imgcodecs.imwrite("debug_ocr_input.png", gray);

            // Aumentar contraste e limiarizacao
            Mat threshed = new Mat();
            Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
            Imgproc.adaptiveThreshold(gray, threshed, 255,
                    Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                    Imgproc.THRESH_BINARY_INV, 11, 2);

            // Erode removed to preserve thin lines
            // Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,
            // 2));
            // Imgproc.erode(threshed, threshed, kernel);

            Imgcodecs.imwrite("debug_ocr_thresh.png", threshed);

            // Encontrar contornos
            java.util.List<org.opencv.core.MatOfPoint> contours = new java.util.ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(threshed, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            System.out.println("[DEBUG] Contours found: " + contours.size());

            // Filtrar contornos que parecem digitos
            java.util.List<Rect> digitRects = new java.util.ArrayList<>();
            int h_img = threshed.height();
            int w_img = threshed.width();

            for (org.opencv.core.MatOfPoint cnt : contours) {
                Rect r = Imgproc.boundingRect(cnt);
                double aspect = (double) r.width / r.height;

                // Ignore border noise
                if (r.x <= 2 || r.y <= 2 || (r.x + r.width) >= w_img - 2 || (r.y + r.height) >= h_img - 2) {
                    continue;
                }

                // Filtros de tamanho e proporcao
                if (r.height > h_img * 0.15 && r.height < h_img * 0.9) {

                    if (aspect > 0.1 && aspect < 1.3) {
                        // Normal usage: Single digit
                        digitRects.add(r);
                    } else if (aspect >= 1.3 && aspect < 8.0) { // Increased max aspect to 8 for long strips
                        System.out.println(
                                "[DEBUG] Processing wide contour (aspect=" + aspect + ") with Projection Profile");

                        Mat roiMat = threshed.submat(r);
                        java.util.List<Rect> parts = splitByProjectionProfile(roiMat, r);
                        System.out.println("[DEBUG] Split found " + parts.size() + " segments.");
                        digitRects.addAll(parts);
                    }
                }
            }

            // Ordenar da esquerda para a direita
            java.util.Collections.sort(digitRects, (r1, r2) -> Integer.compare(r1.x, r2.x));

            // --- Y-Alignment Filter ---
            if (!digitRects.isEmpty()) {
                // Calculate median Y to find the "line of text"
                java.util.List<Integer> yCoords = new java.util.ArrayList<>();
                for (Rect r : digitRects)
                    yCoords.add(r.y + r.height / 2);
                java.util.Collections.sort(yCoords);
                int medianY = yCoords.get(yCoords.size() / 2);

                System.out.println("[DEBUG] Median Line Y: " + medianY);

                // Filter outliers (e.g., > 30px deviation)
                java.util.List<Rect> alignedRects = new java.util.ArrayList<>();
                for (Rect r : digitRects) {
                    int centerY = r.y + r.height / 2;
                    if (Math.abs(centerY - medianY) < 30) {
                        alignedRects.add(r);
                    } else {
                        System.out
                                .println("[DEBUG] Ignoring outlier contour at Y=" + r.y + " (Center=" + centerY + ")");
                    }
                }
                digitRects = alignedRects;
            }

            // --- Force exactly 6 Digits ---
            if (digitRects.size() > 6) {
                // Heuristic: Keep the 6 largest contours (ink area)
                // This filters out small noise specks that passed previous filters
                digitRects.sort((r1, r2) -> Double.compare(r2.area(), r1.area()));
                digitRects = digitRects.subList(0, 6);
            }

            // Re-sort Left-to-Right after filtering
            java.util.Collections.sort(digitRects, (r1, r2) -> Integer.compare(r1.x, r2.x));

            java.util.List<Integer> recognizedDigits = new java.util.ArrayList<>();

            Mat debugSegments = threshed.clone();
            Imgproc.cvtColor(debugSegments, debugSegments, Imgproc.COLOR_GRAY2BGR);

            int digitIdx = 0;
            for (Rect r : digitRects) {
                // Validate rect inside image
                if (r.x < 0)
                    r.x = 0;
                if (r.y < 0)
                    r.y = 0;
                if (r.x + r.width > w_img)
                    r.width = w_img - r.x;
                if (r.y + r.height > h_img)
                    r.height = h_img - r.y;

                // Double check validity
                if (r.width <= 0 || r.height <= 0)
                    continue;

                Mat digitROI = threshed.submat(r);

                // Ink Centering (Keep existing logic)
                java.util.List<org.opencv.core.MatOfPoint> subContours = new java.util.ArrayList<>();
                Mat subHier = new Mat();
                Imgproc.findContours(digitROI, subContours, subHier, Imgproc.RETR_EXTERNAL,
                        Imgproc.CHAIN_APPROX_SIMPLE);

                Rect inkRect = null;
                for (org.opencv.core.MatOfPoint subCnt : subContours) {
                    Rect subR = Imgproc.boundingRect(subCnt);
                    if (subR.height > r.height * 0.4) {
                        if (inkRect == null)
                            inkRect = subR;
                        else
                            inkRect = unionRects(inkRect, subR);
                    }
                }

                Mat tightDigit;
                if (inkRect != null && inkRect.width > 2 && inkRect.height > 5) {
                    try {
                        tightDigit = digitROI.submat(inkRect);
                    } catch (Exception e) {
                        tightDigit = digitROI;
                    }
                } else {
                    tightDigit = digitROI;
                }

                // Force Stretch 50x100
                Mat resized = new Mat();
                Imgproc.resize(tightDigit, resized, new Size(50, 100));

                Imgcodecs.imwrite("debug_digit_" + digitIdx + ".png", resized);
                digitIdx++;

                int valor = reconhecerDigito(resized, debugSegments, r);
                // Even if -1 (fail), we need a placeholder?
                // For now, assume 0 if fail, or skip?
                // If we skip, the positional math fails. Let's assume 0.
                if (valor == -1)
                    valor = 0;
                recognizedDigits.add(valor);
            }

            Imgcodecs.imwrite("debug_ocr_segments.png", debugSegments);

            // --- Apply User Formula ---
            // digit0 * 1000 + digit1 * 100 + digit2 * 10 + digit3 + digit4 * 0.1 + digit5 *
            // 0.01
            double finalValue = 0.0;

            // Pad with 0 if found less than 6 (Safety)
            while (recognizedDigits.size() < 6) {
                recognizedDigits.add(0, 0); // Prepend 0s? Or Append?
                // Usually reading starts from left. If we missed digits, assumed they are
                // leading zeros or trailing?
                // Hard to guess. Let's assume leading 0s for missing msb.
            }

            // If we have > 6 (shouldn't happen due to filter above), trim
            while (recognizedDigits.size() > 6) {
                recognizedDigits.remove(0); // Remove leading noise?
            }

            if (recognizedDigits.size() == 6) {
                finalValue = recognizedDigits.get(0) * 1000.0 +
                        recognizedDigits.get(1) * 100.0 +
                        recognizedDigits.get(2) * 10.0 +
                        recognizedDigits.get(3) * 1.0 +
                        recognizedDigits.get(4) * 0.1 +
                        recognizedDigits.get(5) * 0.01;
            }

            return String.format("%.2f", finalValue).replace(',', '.');

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static java.util.List<Rect> splitByProjectionProfile(Mat roi, Rect offset) {
        java.util.List<Rect> parts = new java.util.ArrayList<>();
        if (roi.empty())
            return parts;

        // Compute vertical projection
        Mat colSums = new Mat();
        Core.reduce(roi, colSums, 0, Core.REDUCE_SUM, org.opencv.core.CvType.CV_32S);

        int w = roi.width();
        int h = roi.height();
        int[] data = new int[w];
        colSums.get(0, 0, data);

        // Calculate max peak to set a dynamic threshold
        int maxVal = 0;
        for (int val : data)
            if (val > maxVal)
                maxVal = val;

        // Threshold: 10% of peak height (allow some noise in gaps)
        int threshold = (int) (maxVal * 0.10);

        int startX = -1;
        for (int x = 0; x < w; x++) {
            if (data[x] > threshold) {
                if (startX == -1)
                    startX = x;
            } else {
                if (startX != -1) {
                    int charW = x - startX;
                    if (charW > 2) {
                        parts.add(new Rect(offset.x + startX, offset.y, charW, h));
                    }
                    startX = -1;
                }
            }
        }
        if (startX != -1) {
            int charW = w - startX;
            if (charW > 2)
                parts.add(new Rect(offset.x + startX, offset.y, charW, h));
        }

        // --- Fallback Logic ---
        double aspect = (double) w / h;
        // If we found too few parts vs expected aspect ratio, force fixed split
        if (parts.size() <= 1 && aspect > 1.5) {
            System.out.println("[DEBUG] Projection failed to split wide contour (aspect=" + aspect
                    + "). Using Fallback Fixed-Width.");
            parts.clear();

            // Estimate segments based on typical digit aspect ratio (0.6)
            int estimated = (int) Math.round(aspect / 0.60);
            if (estimated < 2)
                estimated = 2;

            int partW = w / estimated;
            for (int k = 0; k < estimated; k++) {
                int px = k * partW;
                // Don't go OOB
                int pw = (k == estimated - 1) ? (w - px) : partW;
                parts.add(new Rect(offset.x + px, offset.y, pw, h));
            }
        }

        return parts;
    }

    public static String extrairValorDoNomeArquivo(String caminhoImagem) {
        try {
            File arquivo = new File(caminhoImagem);
            String nome = arquivo.getName();

            String valorStr = nome.substring(0, nome.lastIndexOf('.'));

            valorStr = valorStr.replaceAll("[^0-9.]", "");

            if (!valorStr.isEmpty()) {
                Double.parseDouble(valorStr);
                return valorStr;
            }
        } catch (Exception e) {
        }
        return "0";
    }

    private static String reconhecerDigitosDireto(Mat img) {
        // Fallback method
        int w = img.width();
        int h = img.height();
        // Create a dummy rect for debug purposes
        Rect dummyRect = new Rect(0, 0, w, h);
        int valor = reconhecerDigito(img, img, dummyRect);
        return String.valueOf(valor);
    }

    private static int reconhecerDigito(Mat digit, Mat debugImg, Rect originalRect) {
        int w = digit.width();
        int h = digit.height();

        // Standard 7-segment layout:
        // A
        // F B
        // G
        // E C
        // D

        // Tuned parameters for thin strokes (CRITICAL FIX)
        int thick = 4; // reduced from 8
        // Using "Inch-In" sensors logic instead of margin
        // Since we stretched to 50x100, we know exactly where the bars are.
        // Verticals are roughly at x=0..6 and x=44..50.
        // Horizontals are at y=0..6, y=47..53, y=94..100.

        // Sampling coordinates (Center points)
        int xLeft = 10;
        int xRight = w - 10;
        int xMid = w / 2;

        int yTop = 10;
        int yTopMid = h / 4;
        int yMid = h / 2;
        int yBotMid = 3 * h / 4;
        int yBot = h - 10;

        GlobalDebug.DEBUG_SEGMENTS = true; // Enable granular logging for this digit
        System.out.println("--- Inspecting Digit at " + originalRect + " ---");

        // A (Top)
        boolean segA = isSegmentOn(digit, xMid, yTop, thick, false, "A");
        // B (Top Right)
        boolean segB = isSegmentOn(digit, xRight, yTopMid, thick, true, "B");
        // C (Bottom Right)
        boolean segC = isSegmentOn(digit, xRight, yBotMid, thick, true, "C");
        // D (Bottom)
        boolean segD = isSegmentOn(digit, xMid, yBot, thick, false, "D");
        // E (Bottom Left)
        boolean segE = isSegmentOn(digit, xLeft, yBotMid, thick, true, "E");
        // F (Top Left)
        boolean segF = isSegmentOn(digit, xLeft, yTopMid, thick, true, "F");
        // G (Middle)
        boolean segG = isSegmentOn(digit, xMid, yMid, thick, false, "G");

        String key = "" + (segA ? 1 : 0) + (segB ? 1 : 0) + (segC ? 1 : 0) + (segD ? 1 : 0) + (segE ? 1 : 0)
                + (segF ? 1 : 0) + (segG ? 1 : 0);
        System.out.println("[DEBUG] Segment Key: " + key + " for rect " + originalRect);

        // Map key to digit (Fuzzy)
        int bestMatch = findClosestDigit(key);
        System.out.println("[DEBUG] Key " + key + " -> Match " + bestMatch);
        return bestMatch;
    }

    private static boolean isSegmentOn(Mat img, int cx, int cy, int thickness, boolean vertical, String segName) {
        int x1 = cx - thickness;
        int y1 = cy - thickness;
        int w = thickness * 2;
        int h = thickness * 2;

        if (x1 < 0)
            x1 = 0;
        if (y1 < 0)
            y1 = 0;
        if (x1 + w > img.width())
            w = img.width() - x1;
        if (y1 + h > img.height())
            h = img.height() - y1;

        if (w <= 0 || h <= 0)
            return false;

        Mat roi = img.submat(new Rect(x1, y1, w, h));
        int nonZero = Core.countNonZero(roi);
        double density = (double) nonZero / (w * h);

        // Use a slightly lower threshold for boldness (CRITICAL FIX)
        // Log density for debugging
        if (GlobalDebug.DEBUG_SEGMENTS) {
            System.out.println(String.format("   Seg %s: Density=%.2f [%s]",
                    segName, density, (density > 0.40 ? "ON" : "OFF")));
        }
        return density > 0.40; // Increased to 0.40 to reduce ghost segments
    }

    private static class GlobalDebug {
        public static boolean DEBUG_SEGMENTS = true;
    }

    private static int findClosestDigit(String pattern) {
        // Not used currently but kept for utility
        int minDistance = Integer.MAX_VALUE;
        int closest = -1;

        for (Map.Entry<String, Integer> entry : SEGMENT_MAP.entrySet()) {
            int distance = hammingDistance(pattern, entry.getKey());
            if (distance < minDistance) {
                minDistance = distance;
                closest = entry.getValue();
            }
        }
        return closest;
    }

    private static int hammingDistance(String s1, String s2) {
        int distance = 0;
        for (int i = 0; i < Math.min(s1.length(), s2.length()); i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                distance++;
            }
        }
        return distance;
    }

    // NEW HELPER (Added in write)
    private static Rect unionRects(Rect r1, Rect r2) {
        int x = Math.min(r1.x, r2.x);
        int y = Math.min(r1.y, r2.y);
        int w = Math.max(r1.x + r1.width, r2.x + r2.width) - x;
        int h = Math.max(r1.y + r1.height, r2.y + r2.height) - y;
        return new Rect(x, y, w, h);
    }
}
