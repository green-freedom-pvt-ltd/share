package com.sharesmile.share.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.utils.ImageCompression;
import com.sharesmile.share.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

public class CropImageFragment extends BaseFragment {

    public final String TAG = "CropImageFragment";
    @BindView(R.id.crop_image_view)
    CropImageView cropImageView;
    @BindView(R.id.cancel)
    TextView cancel;
    @BindView(R.id.rotate)
    ImageView rotate;
    @BindView(R.id.done)
    TextView done;

    String imagePath = "";
    boolean cropStarted = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crop, null);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imagePath = getArguments().getString("image_path");
        cropImageView.setAspectRatio(1, 1);
        cropImageView.setFixedAspectRatio(true);
        cropImageView.setImageUriAsync(Uri.fromFile(new File(imagePath)));
        cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(CropImageView cropImageView, CropImageView.CropResult cropResult) {

                Bitmap croppedImage = cropResult.getBitmap();
                ShareImageLoader.getInstance().setUseMemoryCache(false);
                boolean imageSaved = Utils.storeImage(croppedImage, imagePath);
                ImageCompression imageCompression = new ImageCompression(getContext())
                {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        super.onPostExecute(imagePath);
                        CropImageFragment.this.imagePath = imagePath;
                        goBackWithImageData();
                    }
                };
                imageCompression.execute(imagePath);

            }
        });
        setupToolbar();

    }

/*
    private Bitmap getImage(String photoPath) {
        ExifInterface ei = null;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        try {
            ei = new ExifInterface(photoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
*/

    private void setupToolbar() {
        getFragmentController().hideToolbar();
    }

    @OnClick({R.id.cancel, R.id.rotate, R.id.done})
    public void onClick(View view) {
        if(!cropStarted) {
            switch (view.getId()) {
                case R.id.rotate:
                    cropImageView.rotateImage(90);
                    break;

                case R.id.done:
                    cropStarted = true;
                    cropImageView.getCroppedImageAsync();
                    break;

                case R.id.cancel:

//                    File file = new File(imagePath);
//                    file.delete();
                    imagePath = null;
                    goBackWithImageData();
                    break;
            }
        }else
        {
            MainApplication.showToast("Cropping in Progress");
        }
    }

    public void goBackWithImageData()
    {
        Intent intent = new Intent(getContext(), CropImageFragment.class);
        intent.putExtra("imagePath", imagePath);
        getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, intent);
        goBack();
    }
}
