package es.elb4t.pictionary;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ActividadPrincipal extends AppCompatActivity {
    List<String> palabras = new ArrayList<String>();
    Button btnComenzar;
    TextView txtPalabra;
    DrawingView dvRemote;
    DrawingView dvLocal;
    Display localDisplay;
    Display remoteDisplay;
    Integer anchoLocal = 0;
    Integer anchoRemote = 0;
    Integer altoLocal = 0;
    Integer altoRemote = 0;
    Float proporcionAncho = 1.0f;
    Float proporcionAlto = 1.0f;
    Presentation remotePresentation;
    Presentation localPresentation;
    private Paint mPaint;
    private DisplayManager mDisplayManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        palabras.add("Refugio antiaéreo");
        palabras.add("Hormiga");
        palabras.add("Luciérnaga");
        palabras.add("Chile");
        palabras.add("Tigre");
        palabras.add("Castor");
        palabras.add("Italia");
        palabras.add("Abuelo");
        palabras.add("Galaxia");

        txtPalabra = (TextView) findViewById(R.id.txtPalabra);
        btnComenzar = (Button) findViewById(R.id.btnComenzar);
        btnComenzar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDisplayManager = (DisplayManager)
                        getSystemService(Context.DISPLAY_SERVICE);

                Display[] displays;
                displays = mDisplayManager.getDisplays();
                int width;
                int height;
                if (displays.length == 2) {
                    for (Display display : displays) {
                        DisplayMetrics metrics = new DisplayMetrics();
                        display.getMetrics(metrics);
                        width = metrics.widthPixels;
                        height = metrics.heightPixels;
                        if (display.getDisplayId() == 0) {
                            localDisplay = display;
                            anchoLocal = width;
                            altoLocal = height;
                        } else {
                            remoteDisplay = display;
                            anchoRemote = width;
                            altoRemote = height;
                            showRemotePresentation();
                        }
                    }
                    Float aux = anchoLocal / 1.0f;
                    proporcionAncho = anchoRemote / aux;
                    aux = altoLocal / 1.0f;
                    proporcionAlto = altoRemote / aux;
                    String palabra = "";
                    Double aleatorio;
                    aleatorio = Math.random() * (palabras.size() - 1);
                    Integer numero;
                    numero = aleatorio.intValue();
                    palabra = palabras.get(numero);
                    txtPalabra.setText(palabra);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            showLocalPresentation();
                        }
                    }, 5000);
                }
            }
        });
    }

    private void showRemotePresentation() {
        RemotePresentation remotePresentation = new RemotePresentation(this, remoteDisplay);
        remotePresentation.local = false;
        remotePresentation.show();
    }

    private void showLocalPresentation() {
        RemotePresentation localPresentation =
                new RemotePresentation(this, localDisplay);
        localPresentation.local = true;
        localPresentation.show();
    }

    private void hideRemotePresentation(Display display) {
        if (remotePresentation == null) {
            return;
        }
        remotePresentation.dismiss();
    }

    private void hideLocalPresentation(Display display) {
        if (localPresentation == null) {
            return;
        }
        localPresentation.dismiss();
    }


    private final class RemotePresentation extends Presentation {
        Boolean local = false;

        public RemotePresentation(Context context, Display display) {
            super(context, display);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.remoto);
            if (local == false) {
                dvRemote = new DrawingView(getApplicationContext());
                setContentView(dvRemote);
            } else {
                dvLocal = new DrawingView(getApplicationContext());
                dvLocal.setVistaLocal(local);
                setContentView(dvLocal);
            }
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(12);
        }
    }

    public class DrawingView extends View {
        public int width;
        public int height;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;
        Context context;
        private Paint circlePaint;
        private Path circlePath;
        private Boolean vistaLocal;

        public void setVistaLocal(Boolean esLocal) {
            vistaLocal = esLocal;
        }

        public Boolean getVistaLocal() {
            return vistaLocal;
        }

        public DrawingView(Context c) {
            super(c);
            context = c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath(mPath, mPaint);
            canvas.drawPath(circlePath, circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    if (vistaLocal == true) {
                        dvRemote.touch_start(x * proporcionAncho, y * proporcionAlto);
                        dvRemote.invalidate();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    if (vistaLocal == true) {
                        dvRemote.touch_move(x * proporcionAncho, y * proporcionAlto);
                        dvRemote.invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    if (vistaLocal == true) {
                        dvRemote.touch_up();
                        dvRemote.invalidate();
                    }
                    break;
            }
            return true;
        }
    }
}