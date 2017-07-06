package es.elb4t.pictionary;

import android.app.Dialog;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ActividadPrincipal extends AppCompatActivity {
    List<String> palabras = new ArrayList<String>();
    Button btnComenzar;
    TextView txtPalabra, txtLocalTimer, txtRemoteTimer;
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
    RemotePresentation remotePresentation;
    RemotePresentation localPresentation;
    private Paint mPaint;
    private DisplayManager mDisplayManager;
    MiCountDownTimer timer = null;
     RelativeLayout root;
    int tiempoDibujar = 60;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
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

        tiempoDibujar = obtenerPreferencias();
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

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            mostrarPalabraADibujar();
                        }
                    }, 500);
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            showLocalPresentation();
                            timer = new MiCountDownTimer(tiempoDibujar * 1000,1000);
                            timer.start();
                        }
                    }, 5000);

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.cuenta_atras) {
            final Dialog dialog = new Dialog(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
            dialog.setContentView(R.layout.conf_tiempo_dibujar);
            dialog.setTitle("Tiempo cuenta atrás para dibujar");
            dialog.setCancelable(false);


            final EditText ed = (EditText)dialog.findViewById(R.id.editTiempoDibujar);
            ed.setText(tiempoDibujar+"");


            Button bGuardar =(Button)dialog.findViewById(R.id.bGuardar);
            bGuardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tiempoDibujar = Integer.parseInt(ed.getText().toString());
                    guardarPreferencias(tiempoDibujar);

                    Log.e("DIALOG","OK-----");
                    dialog.hide();
                }
            });
            Button bCancelar =(Button)dialog.findViewById(R.id.bCancelar);
            bCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("DIALOG","cancel-----");
                    dialog.cancel();
                }
            });
            dialog.show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void showRemotePresentation() {
        remotePresentation = new RemotePresentation(this, remoteDisplay);
        remotePresentation.local = false;
        remotePresentation.show();
    }

    private void mostrarPalabraADibujar() {
        String palabra = "";
        Double aleatorio;
        aleatorio = Math.random() * (palabras.size() - 1);
        Integer numero;
        numero = aleatorio.intValue();
        palabra = palabras.get(numero);
        txtPalabra.setText(palabra);
    }

    private void showLocalPresentation() {
        localPresentation =
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
            root = (RelativeLayout) findViewById(R.id.pantallaDibbujar);
            if (local == false) {
                dvRemote = new DrawingView(getApplicationContext());
                root.addView(dvRemote);
                txtRemoteTimer = new TextView(getApplicationContext());
                txtRemoteTimer.setLayoutParams(new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                txtRemoteTimer.setGravity(Gravity.CENTER_HORIZONTAL);
                txtRemoteTimer.setTextSize(25);
                root.addView(txtRemoteTimer);
            } else {
                dvLocal = new DrawingView(getApplicationContext());
                dvLocal.setVistaLocal(local);
                root.addView(dvLocal);
                txtLocalTimer = new TextView(getApplicationContext());
                txtLocalTimer.setLayoutParams(new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                txtLocalTimer.setGravity(Gravity.CENTER_HORIZONTAL);
                txtRemoteTimer.setTextSize(25);
                root.addView(txtLocalTimer);
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

        @Override
        public void onBackPressed() {
            //super.onBackPressed();
            timer.cancel();
            timer.onFinish();
            Log.e("BBACK","Back pressed-----");
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

    public class MiCountDownTimer extends CountDownTimer {

        public MiCountDownTimer(long starTime, long interval) {
            super(starTime, interval);

        }

        @Override
        public void onFinish() {
            hideLocalPresentation(localDisplay);
            hideRemotePresentation(remoteDisplay);
            Log.e("TIME","onFINISH---------");
        }

        @Override
        public void onTick(long millisUntilFinished) {
            txtLocalTimer.setText(millisUntilFinished/1000+"");
            txtRemoteTimer.setText(millisUntilFinished/1000+"");
        }

    }

    public int obtenerPreferencias() {
        SharedPreferences prefs = getSharedPreferences("pictionary", Context.MODE_PRIVATE);
        tiempoDibujar = prefs.getInt("tiempoDibujar", 60);
        return tiempoDibujar;
    }

    public void guardarPreferencias(int tiempo) {
        SharedPreferences prefs = getSharedPreferences("pictionary", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("tiempoDibujar", tiempo);
        editor.commit();
    }
}