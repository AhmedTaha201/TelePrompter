package com.example.teleprompter.utils;

import android.hardware.camera2.CameraCharacteristics;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;

/* Utils for camera rotation
 * The Whole camera2 implementation is based on this tutorial
 * https://www.youtube.com/watch?v=CuvVpsFc77w&list=PL9jCwTXYWjDIHNEGtsRdCTk79I9-95TbJ */
public class CameraUtils {

    //Orientation
    public static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);

    }

    //Transform the sensor rotation to device rotation  to decie whether to swap the width and height of the preview or not
    public static int calculateTotalRotation(CameraCharacteristics cc, int deviceOrientation) {
        int sensorOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);

        return (sensorOrientation + deviceOrientation + 360) % 360;
    }

    public static Size chooseOptimalSize(Size[] sizes, int width, int height) {
        List<Size> goodSizes = new ArrayList<>();
        for (Size s : sizes) {
            if (s.getHeight() == s.getWidth() * height / width
                    && s.getWidth() >= width
                    && s.getHeight() >= height) {
                goodSizes.add(s);
            }
        }
        if (goodSizes.size() > 0) {
            //Found a good size
            return Collections.min(goodSizes, new SizeComparator());
        } else {
            //Choosing the first available size
            Timber.d("Choosing the first available size");
            return sizes[1];
        }
    }


    private static class SizeComparator implements Comparator<Size> {

        @Override
        public int compare(Size l, Size r) {
            long leftArea = l.getHeight() * l.getWidth();
            long rightArea = r.getHeight() * r.getWidth();
            return (Long.signum(leftArea - rightArea));
        }
    }

}
