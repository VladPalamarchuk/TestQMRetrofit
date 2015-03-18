package qm.vp.kiev.qmhttplib;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


public class QMProgress extends Dialog {

    public QMProgress(Context context) {
        super(context, R.style.niceProgressDialogStyle);
    }

    @Override
    public void show() {
        if (!isShowing()) {
            init();

            setCancelable(false);

            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            getWindow().getAttributes().windowAnimations = R.style.niceProgressDialogAnimation;

            super.show();
        }
    }

    private void init() {
        setContentView(prepareView());
    }

    private View prepareView() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        relativeLayout.setBackgroundResource(R.drawable.nice_progress_dialog);

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5,
                getContext().getResources().getDisplayMetrics());
        relativeLayout.setPadding(padding, padding, padding, padding);

        relativeLayout.setLayoutParams(params);

        ProgressBar progressBar = new ProgressBar(getContext());
        progressBar.setIndeterminate(true);

        relativeLayout.addView(progressBar);

        return relativeLayout;
    }
}

