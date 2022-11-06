package com.app.plantdisease.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.plantdisease.R;
import com.app.plantdisease.activities.ActivityPostDetail;
import com.app.plantdisease.activities.MainActivity;
import com.app.plantdisease.adapter.AdapterVideo;
import com.app.plantdisease.callbacks.CallbackRecent;
import com.app.plantdisease.config.AppConfig;
import com.app.plantdisease.models.News;
import com.app.plantdisease.rests.ApiInterface;
import com.app.plantdisease.rests.RestAdapter;
import com.app.plantdisease.utils.Constant;
import com.app.plantdisease.utils.NetworkCheck;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentLeafScan extends Fragment {

    private View root_view, parent_view;

    private static final int REQUEST_CODE = 1;
    private Bitmap bitmap;
    private ImageView imageView;

    Button button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_leaf_scan, null);
        parent_view = getActivity().findViewById(R.id.main_content);


        button = root_view.findViewById(R.id.btn_scan);
        imageView = root_view.findViewById(R.id.images);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });

        return root_view;
    }

    public void captureImage() {

        // Creating folders for Image
        String imageFolderPath = Environment.getExternalStorageDirectory().toString()
                + "/AutoFare";
        File imagesFolder = new File(imageFolderPath);
        imagesFolder.mkdirs();

        // Generating file name
        String imageName = new Date().toString() + ".png";

        // Creating image here
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION & Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());
        startActivityForResult(intent, REQUEST_CODE);
      /*  Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CODE);*/

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                if (data != null) {

                    try {


                            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                            imageView.setImageBitmap(bitmap);

                        //                    String path = saveImage(bitmap);
                        //                    Toast.makeText(getActivity(), "Image Saved!", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
                {
                    Toast.makeText(getActivity(), "Data not found", Toast.LENGTH_SHORT).show();
                }

            }
        }

    }
}

