package com.example.bursel.gobang;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    private ChessPanel panel;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window=getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        panel=(ChessPanel)findViewById(R.id.main_panel);
        builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("游戏结束");
        builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        builder.setPositiveButton("再来一局", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                panel.restartGame();
            }
        });
        panel.setOnGameListener(new ChessPanel.onGameListener() {
            @Override
            public void onGameOver(int i) {
                String str="";
                if(i==ChessPanel.WHITE_WIN){
                    str="白方胜利";
                }
                else if(i==ChessPanel.BLACK_WIN){
                    str="黑方胜利";
                }
                builder.setMessage(str);
                builder.setCancelable(false);
                AlertDialog dialog=builder.create();
                Window dialogWindow=dialog.getWindow();
                WindowManager.LayoutParams params=new WindowManager.LayoutParams();
                params.x=0;
                params.y=panel.getUnder();
                dialogWindow.setAttributes(params);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        });
    }
}
