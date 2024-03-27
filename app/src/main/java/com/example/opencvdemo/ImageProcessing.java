package com.example.opencvdemo;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ImageProcessing {
    private final Mat edgesMat;

    public ImageProcessing(Bitmap bmp) {
        // Convert bitmap to Mat
        Mat rgbMat = new Mat();
        Utils.bitmapToMat(bmp, rgbMat);

        // Apply Canny edge detection
        edgesMat = cannyEdgeDetector(rgbMat);
    }

    public Bitmap resizeBorderOfEdges(int borderWidth) {
        // Resize borders around edges
        Mat borderedEdgesMat = getBordered(edgesMat, borderWidth);

        // Convert Mat back to Bitmap
        Bitmap resultBitmap = Bitmap.createBitmap(borderedEdgesMat.cols(), borderedEdgesMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(borderedEdgesMat, resultBitmap);

        // Rotate the bitmap if needed
        Matrix matrix = new Matrix();
        resultBitmap = Bitmap.createBitmap(resultBitmap, 0, 0, resultBitmap.getWidth(), resultBitmap.getHeight(), matrix, true);

        return resultBitmap;
    }

    private Mat cannyEdgeDetector(Mat rgbMat) {
        Mat grayMat = new Mat();
        Mat blurredMat = new Mat();
        Mat bwMat = new Mat();
        Mat closedMat = new Mat();

        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(grayMat, blurredMat, new Size(5, 5), 0); // Apply Gaussian blur
        Imgproc.Canny(blurredMat, bwMat, 0, 200, 3, false); // Apply Canny edge detection

        // Apply morphological closing to close small gaps between edges
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(bwMat, closedMat, Imgproc.MORPH_CLOSE, kernel);

        return closedMat;
    }

    public static Mat getBordered(Mat image, int width) {
        Mat bg = new Mat(image.size(), image.type(), new Scalar(255, 255, 255)); // Initialize background as black
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(image.clone(), contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(bg, contours, i, new Scalar(0, 0, 0), width);
        }

        return bg;
    }
}

