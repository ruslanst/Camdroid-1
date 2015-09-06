package org.hschott.camdroid.processor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;

import org.hschott.camdroid.OnCameraPreviewListener.FrameDrawer;
import org.hschott.camdroid.R;
import org.hschott.camdroid.UIFragment;
import org.opencv.android.Utils;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class CascadeClassifierProcessor extends AbstractOpenCVFrameProcessor {

    public static class CascadeClassifierUIFragment extends
            ConfigurationFragment implements UIFragment {
        public static CascadeClassifierUIFragment newInstance() {
            CascadeClassifierUIFragment f = new CascadeClassifierUIFragment();
            return f;
        }

        private SeekBar objectMaxSizeSeekBar;

        private SeekBar objectMinSizeSeekBar;

        @Override
        public int getLayoutId() {
            return R.layout.cascadeclassifier_ui;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = super
                    .onCreateView(inflater, container, savedInstanceState);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this.getActivity(), android.R.layout.simple_list_item_1,
                    android.R.id.text1, new ArrayList<String>(
                    classifiersId.keySet()));
            Spinner s = (Spinner) v.findViewById(R.id.classifiers);
            s.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    classifierPath = classifiersPath.get((int) id);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            this.objectMinSizeSeekBar = (SeekBar) v
                    .findViewById(R.id.object_min_size);
            this.objectMinSizeSeekBar.setMax(100);
            this.objectMinSizeSeekBar.setProgress(object_min_size);

            this.objectMinSizeSeekBar
                    .setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar,
                                                      int progress, boolean fromUser) {
                            object_min_size = progress == 0 ? 1 : progress;
                            if (fromUser) {
                                if (object_min_size > object_max_size) {
                                    CascadeClassifierUIFragment.this.objectMaxSizeSeekBar
                                            .setProgress(progress);
                                }
                                CascadeClassifierUIFragment.this
                                        .showValue(object_min_size
                                                + "% of height");
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

            this.objectMaxSizeSeekBar = (SeekBar) v
                    .findViewById(R.id.object_max_size);
            this.objectMaxSizeSeekBar.setMax(100);
            this.objectMaxSizeSeekBar.setProgress(object_max_size);

            this.objectMaxSizeSeekBar
                    .setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar,
                                                      int progress, boolean fromUser) {
                            object_max_size = progress == 0 ? 1 : progress;
                            if (fromUser) {
                                if (object_max_size < object_min_size) {
                                    CascadeClassifierUIFragment.this.objectMinSizeSeekBar
                                            .setProgress(progress);
                                }
                                CascadeClassifierUIFragment.this
                                        .showValue(object_max_size
                                                + "% of height");
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

            SeekBar scaleFactorSeekBar = (SeekBar) v
                    .findViewById(R.id.scale_factor);
            scaleFactorSeekBar.setMax(100);
            scaleFactorSeekBar.setProgress(scale_factor);

            scaleFactorSeekBar
                    .setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar,
                                                      int progress, boolean fromUser) {
                            if (fromUser) {
                                scale_factor = progress == 0 ? 1 : progress;
                                CascadeClassifierUIFragment.this
                                        .showValue(1 + (double) scale_factor / 100);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

            SeekBar neighborsSeekBar = (SeekBar) v
                    .findViewById(R.id.min_neighbors);
            neighborsSeekBar.setMax(10);
            neighborsSeekBar.setProgress(min_neighbors);

            neighborsSeekBar
                    .setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar,
                                                      int progress, boolean fromUser) {
                            if (fromUser) {
                                min_neighbors = progress;
                                CascadeClassifierUIFragment.this
                                        .showValue(min_neighbors);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

            s.setAdapter(adapter);

            return v;
        }
    }

    private static LinkedHashMap<String, Integer> classifiersId = new LinkedHashMap<String, Integer>();
    private static List<String> classifiersPath = new ArrayList<String>();

    static {
        classifiersId
                .put("Frontal face", R.raw.haarcascade_frontalface_default);
        classifiersId.put("Profile face", R.raw.haarcascade_profileface);

        classifiersId.put("Eye", R.raw.haarcascade_eye);
        classifiersId.put("Eye w/ eyeglasses",
                R.raw.haarcascade_eye_tree_eyeglasses);
        classifiersId.put("Left eye", R.raw.haarcascade_lefteye_2splits);

        classifiersId.put("Mouth", R.raw.haarcascade_mcs_mouth);
        classifiersId.put("Nose", R.raw.haarcascade_mcs_nose);
        classifiersId.put("Left ear", R.raw.haarcascade_mcs_leftear);

        classifiersId.put("Full body", R.raw.haarcascade_fullbody);
        classifiersId.put("Upper body", R.raw.haarcascade_upperbody);
        classifiersId.put("Lower body", R.raw.haarcascade_lowerbody);
    }

    private static String classifierPath;

    private static int object_min_size = 10;
    private static int object_max_size = 25;
    private static int min_neighbors = 2;
    private static int scale_factor = 50;

    public CascadeClassifierProcessor(FrameDrawer drawer) {
        super(drawer);
        for (Entry<String, Integer> entry : classifiersId.entrySet()) {
            String path = Utils.exportResource(((View) drawer).getContext(),
                    entry.getValue());
            classifiersPath.add(path);
        }
        classifierPath = classifiersPath.get(0);
    }

    @Override
    public Fragment getConfigUiFragment() {
        return CascadeClassifierUIFragment.newInstance();
    }

    @Override
    public FrameWorker createFrameWorker() {
        return new CascadeClassifierFrameWorker(drawer);
    }

    public class CascadeClassifierFrameWorker extends AbstractOpenCVFrameWorker {
        private CascadeClassifier detector;
        private String lastUsedClassifierPath;

        public CascadeClassifierFrameWorker(FrameDrawer drawer) {
            super(drawer);
            this.detector = new CascadeClassifier();
        }

        protected void execute() {

            if (!classifierPath.equals(lastUsedClassifierPath)) {
                detector.load(classifierPath);
                lastUsedClassifierPath = classifierPath;
            }

            if (detector.empty()) {
                return;
            }

            out = gray();

            double min = (double) this.in.height() * object_min_size / 100;
            double max = (double) this.in.height() * object_max_size / 100;

            MatOfRect rects = new MatOfRect();
            detector.detectMultiScale(out, rects,
                    1 + (double) scale_factor / 100, min_neighbors, 0,
                    new Size(min, min), new Size(max, max));

            for (Rect rect : rects.toArray()) {
                Imgproc.rectangle(out, rect.tl(), rect.br(),
                        new Scalar(255, 0, 0), 1);
            }

        }

    }

}