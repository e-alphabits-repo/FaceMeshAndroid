package com.example.facemeshrecognition;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.media.ImageWriter;
import android.util.Log;

import java.nio.ByteBuffer;

public class Util {

//    public static Bitmap bitmapFromRgba(int width, int height, byte[] bytes) {
//        int[] pixels = new int[bytes.length / 4];
//        int j = 0;
//
//        // It turns out Bitmap.Config.ARGB_8888 is in reality RGBA_8888!
//        // Source: https://stackoverflow.com/a/47982505/1160360
//        // Now, according to my own experiments, it seems it is ABGR... this sucks.
//        // So we have to change the order of the components
//
//        for (int i = 0; i < pixels.length; i++) {
//            byte R = bytes[j++];
//            byte G = bytes[j++];
//            byte B = bytes[j++];
//            byte A = bytes[j++];
//
//            int pixel = (A << 24) | (B << 16) | (G << 8) | R;
//            pixels[i] = pixel;
//        }
//
//        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(pixels));
//        return bitmap;
//    }

//    public static int[] bitmapFromRgba(int width, int height, byte[] bytes) {
//        int[] pixels = new int[bytes.length / 4];
//        int j = 0;
//
//        for (int i = 0; i < pixels.length; i++) {
//            int R = bytes[j++] & 0xff;
//            int G = bytes[j++] & 0xff;
//            int B = bytes[j++] & 0xff;
//            int A = bytes[j++] & 0xff;
//
//            int pixel = (A << 24) | (R << 16) | (G << 8) | B;
//            pixels[i] = pixel;
//        }
//
//
////        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
////        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
////        return bitmap;
//        return pixels;
//    }

    public static byte[] YUV420toNV21(Image image) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
//        Log.d("abhish", " Width is" + width + "and height is " + height);
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];

        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
            }

            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();

            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }

}
