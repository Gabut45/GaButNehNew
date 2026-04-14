package com.ujicoba.gabut.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.WindowManager;

import com.ujicoba.gabut.R;
import com.ujicoba.gabut.databinding.LayoutDialogBinding;

public class SantaCustomDialog extends Dialog {
    private OnDialogButtonClickListener onDialogButtonClickListener;
    private Runnable onRewardedButtonClick;
    private final LayoutDialogBinding scDialogBinding;

    public SantaCustomDialog(Context context, String dialogType, int heightPx, int logoPath,
                             String title, String desc, String coins,
                             String btn1Text, String btn2Text, String hintText1, String hintText2) {
        super(context);
        scDialogBinding = LayoutDialogBinding.inflate(getLayoutInflater());
        setContentView(scDialogBinding.getRoot());
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getAttributes().windowAnimations = android.R.anim.fade_in;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(getWindow().getAttributes());
        layoutParams.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
        getWindow().setAttributes(layoutParams);

        WindowManager.LayoutParams layoutParams2 = getWindow().getAttributes();
        layoutParams2.height = heightPx;
        getWindow().setAttributes(layoutParams2);

        scDialogBinding.blurView.setSciFiDarkOverlay();

        // Default hidden
        scDialogBinding.btniklan.setVisibility(View.GONE);

        if (dialogType.equalsIgnoreCase("coin")) {
            scDialogBinding.lottieView.setVisibility(View.VISIBLE);
            scDialogBinding.imageView.setVisibility(View.GONE);
            scDialogBinding.editText1.setVisibility(View.GONE);
            scDialogBinding.editText2.setVisibility(View.GONE);

            scDialogBinding.btn1.setVisibility(View.GONE);
            scDialogBinding.btn2.setVisibility(View.VISIBLE);
            scDialogBinding.btniklan.setVisibility(View.VISIBLE);

            scDialogBinding.btn2.setOnClickListener(view -> {
                if (onDialogButtonClickListener != null) {
                    onDialogButtonClickListener.onDialogButtonClick();
                }
                dismiss();
            });

            scDialogBinding.btniklan.setOnClickListener(view -> {
                if (onRewardedButtonClick != null) {
                    onRewardedButtonClick.run();
                }
            });

        } else if (dialogType.equalsIgnoreCase("cointanpaiklan")) {
            scDialogBinding.lottieView.setVisibility(View.VISIBLE);
            scDialogBinding.imageView.setVisibility(View.GONE);
            scDialogBinding.editText1.setVisibility(View.GONE);
            scDialogBinding.editText2.setVisibility(View.GONE);

            scDialogBinding.btn1.setVisibility(View.GONE);
            scDialogBinding.btn2.setVisibility(View.VISIBLE);
            scDialogBinding.btniklan.setVisibility(View.GONE);

            scDialogBinding.btn2.setOnClickListener(view -> {
                if (onDialogButtonClickListener != null) {
                    onDialogButtonClickListener.onDialogButtonClick();
                }
                dismiss();
            });

            scDialogBinding.btniklan.setOnClickListener(view -> {
                if (onRewardedButtonClick != null) {
                    onRewardedButtonClick.run();
                }
            });

        } else if (dialogType.equalsIgnoreCase("tidur")) {
            scDialogBinding.lottieView.setVisibility(View.VISIBLE);
            scDialogBinding.imageView.setVisibility(View.GONE);
            scDialogBinding.coinLyt.setVisibility(View.GONE);
            scDialogBinding.editText1.setVisibility(View.GONE);
            scDialogBinding.editText2.setVisibility(View.GONE);
            scDialogBinding.btn1.setVisibility(View.GONE);
            scDialogBinding.btn2.setVisibility(View.VISIBLE);
            scDialogBinding.btniklan.setVisibility(View.GONE);

            // 🔥 Ganti bagian ini supaya pakai lotti lain
            scDialogBinding.lottieView.setAnimation("tidur.json"); // ambil dari assets/
            scDialogBinding.lottieView.playAnimation();

            scDialogBinding.btn2.setOnClickListener(view -> {
                if (onDialogButtonClickListener != null) {
                    onDialogButtonClickListener.onDialogButtonClick();
                }
            });

        } else if (dialogType.equalsIgnoreCase("input")) {
            scDialogBinding.lottieView.setVisibility(View.GONE);
            scDialogBinding.imageView.setVisibility(View.VISIBLE);
            scDialogBinding.coinLyt.setVisibility(View.GONE);
            scDialogBinding.editText1.setVisibility(View.VISIBLE);
            scDialogBinding.btniklan.setVisibility(View.GONE);

            if (hintText2.equalsIgnoreCase("null")) {
                scDialogBinding.editText2.setVisibility(View.GONE);
            }
            scDialogBinding.editText1.setHint(hintText1);
            scDialogBinding.editText2.setHint(hintText2);

        } else if (dialogType.equalsIgnoreCase("internet")) {
            scDialogBinding.lottieView.setVisibility(View.GONE);
            scDialogBinding.imageView.setVisibility(View.VISIBLE);
            scDialogBinding.coinLyt.setVisibility(View.GONE);
            scDialogBinding.editText1.setVisibility(View.GONE);
            scDialogBinding.editText2.setVisibility(View.GONE);
            scDialogBinding.btn1.setVisibility(View.GONE);
            scDialogBinding.btn2.setVisibility(View.VISIBLE);
            scDialogBinding.btniklan.setVisibility(View.GONE);

            scDialogBinding.btn2.setOnClickListener(view -> {
                if (onDialogButtonClickListener != null) {
                    onDialogButtonClickListener.onDialogButtonClick();
                }
            });

        } else if (dialogType.equalsIgnoreCase("normal")) {
            scDialogBinding.lottieView.setVisibility(View.GONE);
            scDialogBinding.imageView.setVisibility(View.VISIBLE);
            scDialogBinding.coinLyt.setVisibility(View.GONE);
            scDialogBinding.editText1.setVisibility(View.GONE);
            scDialogBinding.editText2.setVisibility(View.GONE);
            scDialogBinding.btn1.setVisibility(View.VISIBLE);
            scDialogBinding.btn2.setVisibility(View.VISIBLE);
            scDialogBinding.btniklan.setVisibility(View.GONE);

            scDialogBinding.btn2.setOnClickListener(view -> {
                if (onDialogButtonClickListener != null) {
                    onDialogButtonClickListener.onDialogButtonClick();
                }
            });

        } else if (dialogType.equalsIgnoreCase("insufficient")) {
            scDialogBinding.lottieView.setVisibility(View.GONE);
            scDialogBinding.imageView.setVisibility(View.VISIBLE);
            scDialogBinding.coinLyt.setVisibility(View.VISIBLE);
            scDialogBinding.dialogCoinIcon.setImageResource(R.drawable.ic_clock);
            scDialogBinding.coins.setTextSize(15);
            scDialogBinding.editText1.setVisibility(View.GONE);
            scDialogBinding.editText2.setVisibility(View.GONE);
            scDialogBinding.btn1.setVisibility(View.VISIBLE);
            scDialogBinding.btn2.setVisibility(View.VISIBLE);
            scDialogBinding.btniklan.setVisibility(View.GONE);

            scDialogBinding.btn2.setOnClickListener(view -> {
                if (onDialogButtonClickListener != null) {
                    onDialogButtonClickListener.onDialogButtonClick();
                }
            });
        }

        // Set common content
        scDialogBinding.imageView.setImageResource(logoPath);
        scDialogBinding.title.setText(title);
        scDialogBinding.desc.setText(desc);
        scDialogBinding.coins.setText(coins);
        scDialogBinding.btn1Text.setText(btn1Text);
        scDialogBinding.btn2Text.setText(btn2Text);

        scDialogBinding.btn1.setOnClickListener(view -> dismiss());
    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener listener) {
        this.onDialogButtonClickListener = listener;
    }

    public void setOnRewardedButtonClickListener(Runnable listener) {
        this.onRewardedButtonClick = listener;
    }

    public interface OnDialogButtonClickListener {
        void onDialogButtonClick();
    }

    public void hideRewardedButton() {
        if (scDialogBinding != null && scDialogBinding.btniklan != null) {
            scDialogBinding.btniklan.setVisibility(View.GONE);
        }
    }

}
