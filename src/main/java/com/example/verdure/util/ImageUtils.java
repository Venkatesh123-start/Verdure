package com.example.verdure.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;

public class ImageUtils {

    // Save bitmap to internal storage and return absolute path
    public static String saveBitmapToInternal(Context ctx, Bitmap bmp, String filename) {
        try {
            FileOutputStream fos = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            return ctx.getFileStreamPath(filename).getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Load bitmap with sampling to avoid OOM
    public static Bitmap loadBitmap(String path, int maxDim) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        int w = opts.outWidth, h = opts.outHeight;
        int scale = 1;
        int largest = Math.max(w, h);
        while (largest / (scale * 2) >= maxDim) scale *= 2;
        opts.inSampleSize = scale;
        opts.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, opts);
    }

    // Compute simple RGB histogram with 4 bins per channel (4*4*4 = 64 dims)
    public static float[] computeHistogram(Bitmap bmp) {
        int w = bmp.getWidth(), h = bmp.getHeight();
        float[] hist = new float[64];
        int bins = 4;
        for (int y=0;y<h;y++){
            for (int x=0;x<w;x++){
                int px = bmp.getPixel(x,y);
                int r = (px>>16)&0xff;
                int g = (px>>8)&0xff;
                int b = px & 0xff;
                int ri = r * bins / 256;
                int gi = g * bins / 256;
                int bi = b * bins / 256;
                if (ri==bins) ri = bins-1;
                if (gi==bins) gi = bins-1;
                if (bi==bins) bi = bins-1;
                int idx = ri * (bins*bins) + gi * bins + bi;
                hist[idx] += 1;
            }
        }
        float total = w*h;
        for (int i=0;i<hist.length;i++) hist[i] = hist[i]/total; // normalize
        return hist;
    }

    // Serialize histogram to CSV
    public static String histToString(float[] hist) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<hist.length;i++) {
            if (i>0) sb.append(",");
            sb.append(String.format(Locale.US, "%.6f", hist[i]));
        }
        return sb.toString();
    }

    // Parse CSV back to float[]
    public static float[] stringToHist(String s) {
        if (s==null || s.isEmpty()) return null;
        String[] parts = s.split(",");
        float[] h = new float[parts.length];
        for (int i=0;i<parts.length;i++){
            try { h[i] = Float.parseFloat(parts[i]); } catch(Exception e){ h[i]=0f; }
        }
        return h;
    }

    // Cosine similarity between histograms
    public static float cosineSimilarity(float[] a, float[] b) {
        if (a==null||b==null||a.length!=b.length) return 0f;
        double dot=0, na=0, nb=0;
        for (int i=0;i<a.length;i++){
            dot += a[i]*b[i];
            na += a[i]*a[i];
            nb += b[i]*b[i];
        }
        if (na==0 || nb==0) return 0f;
        return (float)(dot / (Math.sqrt(na)*Math.sqrt(nb)));
    }

    // Very simple brown-ish pixel detector (returns fraction of brown pixels)
    public static float brownFraction(Bitmap bmp) {
        int w=bmp.getWidth(), h=bmp.getHeight();
        int count=0, total=0;
        for (int y=0;y<h;y+=4){ // sample every 4th row/col to speed up
            for (int x=0;x<w;x+=4){
                int px = bmp.getPixel(x,y);
                int r=(px>>16)&0xff;
                int g=(px>>8)&0xff;
                int b=px&0xff;
                // brown-ish heuristic
                if (r>100 && g>40 && g<120 && b<100 && (r>g && g>b)) count++;
                total++;
            }
        }
        if (total==0) return 0f;
        return (float)count/total;
    }
}
