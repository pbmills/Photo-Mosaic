package com.pre.canva;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jakewharton.rxbinding.view.RxView;
import com.pre.canva.application.CanvaApplication;
import com.pre.canva.application.PrefKeys;
import com.pre.canva.network.WebAgent;
import com.pre.canva.usecase.DecodeBitmapUseCase;
import com.pre.canva.usecase.MosaicUseCase;
import com.pre.canva.usecase.SaveFileUseCase;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.ViewById;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.io.File;

import javax.inject.Inject;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.tooltip.Tooltip;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Pre on 14/09/2017.
 */
@EActivity
public class MainActivity extends AppCompatActivity {
    @ViewById(R.id.root_view)
    CoordinatorLayout rootView;

    @ViewById(R.id.image_view)
    ImageViewTouch imageView;

    @ViewById(R.id.progress_bar)
    ProgressBar progressBar;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.fab)
    FloatingActionButton fab;

    @ViewById(R.id.action_host_ip)
    ImageView hostIPButton;

    @ViewById(R.id.action_save_photo)
    ImageView savePhotoButton;

    //Todo: handle configuration change when async tasks are in progress
    @NonConfigurationInstance
    Bitmap bitmap;

    @Inject
    DecodeBitmapUseCase.Builder decodeBitmapBuilder;

    @Inject
    MosaicUseCase.Builder mosaicBuilder;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    SharedPreferences.Editor sharedPreferencesEditor;

    @Inject
    InetAddressValidator ipValidator;

    @Inject
    WebAgent webAgent;

    protected File savedFile;

    protected static final int TILE_WIDTH = 32;
    protected static final int TILE_HEIGHT = 32;

    private CompositeSubscription subscriptions = new CompositeSubscription();

    protected void addSubscription(Subscription s) {
        subscriptions.add(s);
    }

    protected void unsubscribeAll() {
        subscriptions.unsubscribe();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((CanvaApplication)getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @AfterViews
    void afterViews() {
        setSupportActionBar(toolbar);
        hideProgressBar();

        Subscription fabSubscription =
                RxView
                        .clicks(fab)
                        .compose(RxPermissions.getInstance(this).ensure(Manifest.permission.READ_EXTERNAL_STORAGE))
                        .subscribe(granted -> {
                            if (granted) {
                                pickPhoto();
                            } else {
                                makeSnackBar(R.string.storage_access).show();
                            }
                        });

        addSubscription(fabSubscription);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.mipmap.canvalogo);
        }

        RxView
                .clicks(hostIPButton)
                .subscribe(v -> showHostDialog());

        RxView
                .clicks(savePhotoButton)
                .subscribe(v -> savePhoto());

        showTooltip();
    }

    //Todo: add saved photo file into system with mediaScanner to make it show in gallery app immediately
    protected void savePhoto() {
        String fullPath = Environment.getExternalStorageDirectory() + "//CanvaApp//" + System.currentTimeMillis() + ".jpg";

        Observable<File> task = new SaveFileUseCase.Builder()
                .setBitmap(bitmap)
                .setFullFilePath(fullPath)
                .createUseCase();

        showProgressBar();

        Subscription subscription = task
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(() -> hideProgressBar())
                .subscribe(
                        file -> savedFile = file,
                        error -> makeSnackBar(R.string.could_not_save_photo).show(),
                        () -> makeSnackBar(R.string.photo_saved)
                                .setAction(
                                        R.string.share,
                                        view -> sharePhoto(savedFile))
                                .show());

        subscriptions.add(subscription);
    }

    //Todo: check availability of sharing photo intent
    protected void sharePhoto(File file) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    protected void showTooltip() {
        int showTimes = sharedPreferences.getInt(PrefKeys.SHOW_TOOLTIPS_TIMES, 0);

        //only show tooltips 3 times
        if (showTimes > 2) {
            return;
        }

        Tooltip.make(this,
                new Tooltip
                        .Builder(101)
                        .anchor(hostIPButton, Tooltip.Gravity.BOTTOM)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(true, false), 3000)
                        .activateDelay(0)
                        .showDelay(100)
                        .text(getString(R.string.change_server))
                        .maxWidth(1000)
                        .withArrow(true)
                        .withOverlay(true)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .build()
        ).show();

        Tooltip.make(this,
                new Tooltip
                        .Builder(101)
                        .anchor(imageView, Tooltip.Gravity.CENTER)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(true, false), 7000)
                        .activateDelay(0)
                        .showDelay(3000)
                        .text(getString(R.string.gesture_tip))
                        .maxWidth(1000)
                        .withArrow(true)
                        .withOverlay(true)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .build()
        ).show();

        ++showTimes;

        sharedPreferencesEditor.putInt(PrefKeys.SHOW_TOOLTIPS_TIMES, showTimes).commit();
    }

    protected Snackbar makeSnackBar(int stringID) {
        return Snackbar.make(rootView, stringID, Snackbar.LENGTH_LONG);
    }

    @Override
    protected void onDestroy() {
        unsubscribeAll();

        super.onDestroy();
    }

    static class IntentRequest {
        public static final int PICK_PHOTO = 1;
    }

    protected void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        startActivityForResult(intent, IntentRequest.PICK_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != RESULT_OK) {
            return;
        }

        Uri uri;

        switch (requestCode) {
            case IntentRequest.PICK_PHOTO:
                uri = intent.getData();
                decodePhoto(uri);
                break;
            default:
                break;
        }
    }

    protected void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    protected void setBitmap(Bitmap bitmap) {
        if(this.bitmap != null && this.bitmap != bitmap){
            this.bitmap.recycle();
        }

        this.bitmap = bitmap;
        imageView.setImageBitmap(bitmap);
    }

    protected void decodePhoto(final Uri uri) {
        Observable<Bitmap> decodeTask = decodeBitmapBuilder
                .setUri(uri)
                .createUseCase();

        showProgressBar();

        Subscription s =
                decodeTask
                        .subscribe(
                                bitmap -> {
                                    setBitmap(bitmap);
                                    generateMosaic(bitmap);
                                },
                                error -> {
                                    makeSnackBar(R.string.could_not_load_photo).show();
                                    hideProgressBar();
                                }
                        );

        addSubscription(s);
    }

    protected void generateMosaic(Bitmap bitmap) {
        Bitmap mosaicBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Observable<Bitmap> mosaicTask = mosaicBuilder
                .setBitmap(bitmap)
                .setResultContainer(mosaicBitmap)
                .setTileWidth(TILE_WIDTH)
                .setTileHeight(TILE_HEIGHT)
                .createUseCase();

        showProgressBar();

        Subscription s =
                mosaicTask
                .subscribe(
                        bmp -> setBitmap(bmp),
                        error -> {
                            makeSnackBar(R.string.could_not_generate_mosaic).show();
                            hideProgressBar();
                        },
                        () -> hideProgressBar()
                );

        addSubscription(s);
    }

    public void showHostDialog() {
        String currentHost = sharedPreferences.getString(PrefKeys.HOST_IP, PrefKeys.DEFAULT_HOST_IP);

        new MaterialDialog.Builder(this)
                .title(R.string.host_ip_address)
                .content(R.string.default_port)
                .inputType(InputType.TYPE_CLASS_PHONE)
                .input(getString(R.string.host_ip_address), currentHost, (dialog, input) -> {})
                .onPositive((dialog, which) -> {
                    String ip = dialog.getInputEditText().getText().toString().trim();
                    if (ipValidator.isValid(ip)) {
                        sharedPreferencesEditor.putString(PrefKeys.HOST_IP, ip).commit();
                        webAgent.setWebServer("http://" + ip + ":8765/");
                    } else {
                        makeSnackBar(R.string.not_valid_ip_address).show();
                    }
                }).show();
    }

}
