package com.example.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button button;
    private View links;
    private int status = MyService.STATUS_STOPPED;

    private void updateServiceStatus(int status) {
        this.status = status;
        button.setText(MyService.STATUS_NAME[status]);
        button.setEnabled(MyService.STATUS_STOPPED == status || MyService.STATUS_READY == status);
        links.setVisibility(MyService.STATUS_READY == status ? View.VISIBLE: View.INVISIBLE);
    }

    private static class MyHandler extends Handler {
        private final MainActivity activity;

        public MyHandler(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (Consts.MSG_STATUS != msg.what)
                return;
            this.activity.updateServiceStatus(msg.arg1);
        }
    }
    private final Handler handler = new MyHandler(this);
    private final Messenger incoming = new Messenger(handler);

    private Messenger outgoing = null;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // when the service is connected, we create the Messenger to send messages
            outgoing = new Messenger(service);
            Message req = Message.obtain();
            req.replyTo = incoming;
            req.what = Consts.MSG_STATUS;
            // send a message
            try {
                outgoing.send(req);
            } catch (RemoteException e) {
                updateServiceStatus(MyService.STATUS_STOPPED);
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // nullify the messenger in case the service is disconnected unexpectedly,
            // e.g. the remote process crashes
            outgoing = null;
            updateServiceStatus(MyService.STATUS_STOPPED);
        }
    };


    @Override
    public void onClick(View v) {
        if (MyService.STATUS_STOPPED == status) {
            startMyService();
        } else {
            stopMyService();
        }
    }

    private void stopMyService() {
        if (null != outgoing) {
            Message req = Message.obtain();
            req.replyTo = incoming;
            req.what = Consts.MSG_STOP;
            try {
                outgoing.send(req);
            } catch (RemoteException e) {
                updateServiceStatus(MyService.STATUS_STOPPED);
            }
        }
    }

    private void startMyService() {
        updateServiceStatus(MyService.STATUS_STARTING);
        final Intent service = new Intent(this, MyService.class);
        //this.startService(service);
        if (!this.bindService(service, connection, BIND_AUTO_CREATE)) {
            Toast.makeText(this, "Bind service failed!", Toast.LENGTH_LONG).show();
            this.unbindService(connection);
            //updateServiceStatus(MyService.STATUS_STOPPED);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = this.findViewById(R.id.button);
        button.setOnClickListener(this);

        links = this.findViewById(R.id.links);

        startMyService();
    }

    public static Intent newInstance(@NonNull final Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        return intent;
    }
}
