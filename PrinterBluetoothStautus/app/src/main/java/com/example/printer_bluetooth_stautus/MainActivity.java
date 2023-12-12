package com.example.printer_bluetooth_stautus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    String theBtMacAddress = "AC:3F:A4:F2:B0:63";
    Connection thePrinterConn ;

    String logvTag = "ZZZVVV";
    String zplCancelJobs = "~JA";
    String zplPrint01= """
        ^XA^FO20,20^A0N,25,25^FDThis is a ZPL test.^FS^XZ
    """;

    String getSgdValue;

    String rcvBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOpen = (Button)findViewById(R.id.btnOpen);
        Button btnClose = (Button)findViewById(R.id.btnClose);
        Button btnStat = (Button)findViewById(R.id.btnBtStat);
        Button btnPause = (Button)findViewById(R.id.btnPause);
        Button btnUnpause = (Button)findViewById(R.id.btnUnpause);
        Button btnPrint = (Button)findViewById(R.id.btnPrint);
        Button btnCancel = (Button)findViewById(R.id.btnCancel);
        Button btnPrinterStat = (Button)findViewById(R.id.btnPrinterStat);
        Button btnJobCount = (Button)findViewById(R.id.btnJobCount);
        TextView tvJobCount = (TextView) findViewById(R.id.tvJobCount);

        btnOpen.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   logv("btn clicked");
                   openBluetooth(theBtMacAddress);
               }
           }
        );

        btnClose.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   logv("btn clicked");
                   closeBluetooth();
               }
           }
        );

        btnStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getConnStatus();
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSGD("device.pause", "");
            }
        });

        btnUnpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSGD("device.unpause", "");
            }
        });

        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendZPL(zplPrint01);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendZPL(zplCancelJobs);
            }
        });

        btnPrinterStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPrinterStat();
            }
        });

        btnJobCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    String sgd = "device.host_status";
                    getSgdValue = getSGD(sgd);
                    Thread.sleep(500);

                    logv("hoststatus: " + getSgdValue);

                    rcvBuffer = getReceiveBuffer(getSgdValue);
                    tvJobCount.setText(rcvBuffer);

                } catch (Exception e) {
                    // Handle communications error here.
                    e.printStackTrace();
                }

            }
        });



    }



    public void logv(String msg) { Log.v(logvTag, msg);}

    public void getPrinterStat() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    ZebraPrinter printer = ZebraPrinterFactory.getInstance(thePrinterConn);
                    PrinterStatus printerStatus = printer.getCurrentStatus();
                    if (printerStatus.isReadyToPrint) {
                        logv("Ready To Print");
                    } else if (printerStatus.isPaused) {
                        logv("Cannot Print because the printer is paused.");
                    } else if (printerStatus.isHeadOpen) {
                        logv("Cannot Print because the printer head is open.");
                    } else if (printerStatus.isPaperOut) {
                        logv("Cannot Print because the paper is out.");
                    } else {
                        logv("Cannot Print.");
                    }
                } catch (Exception e) {
                    // Handle communications error here.
                    e.printStackTrace();
                    logv("Cannot Print.");
                }
            }
        }).start();
    }

    public void openBluetooth(String btmac) {

        new Thread(new Runnable() {
            public void run() {
                try {
                    // Instantiate connection for given Bluetooth&reg; MAC Address.
                    thePrinterConn = new BluetoothConnection(btmac);

                    // Initialize
                    Looper.prepare();

                    // Open the connection - physical connection is established here.
                    thePrinterConn.open();

                    Looper.myLooper().quit();
                } catch (Exception e) {
                    // Handle communications error here.
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void closeBluetooth() {

        new Thread(new Runnable() {
            public void run() {
                try {
                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(500);

                    // Close the connection - physical connection is established here.
                    thePrinterConn.close();

                } catch (Exception e) {
                    // Handle communications error here.
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void getConnStatus() {

        new Thread(new Runnable() {
            public void run() {
                try {
                    Boolean connStat = thePrinterConn.isConnected();

                    if (connStat){
                        logv("Bluetooth is connected.");
                    } else {
                        logv("Bluetooth is unconnected.");
                    }

                } catch (Exception e) {
                    // Handle communications error here.
                    e.printStackTrace();
                    logv("Bluetooth is unconnected.");
                }
            }
        }).start();
    }

    public void sendZPL(String zpl) {

        new Thread(new Runnable() {
            public void run() {
                try {
                    // Send the data to printer as a byte array.
                    thePrinterConn.write(zpl.getBytes());

                } catch (Exception e) {
                    // Handle communications error here.
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setSGD(String sgd, String value) {

        new Thread(new Runnable() {
            public void run() {
                try {
                    SGD.SET(sgd, value, thePrinterConn);

                } catch (Exception e) {
                    // Handle communications error here.
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String getSGD(String sgd) {

        new Thread(new Runnable() {
            public void run() {
                try {
                    getSgdValue = SGD.GET(sgd, thePrinterConn);
                    logv("try:" + getSgdValue);

                } catch (Exception e) {
                    // Handle communications error here.
                    e.printStackTrace();
                }
            }
        }).start();
        return getSgdValue;
    }

    public String getReceiveBuffer(String hoststatus){
        /* Get eee data from host-status

        01234567890123456789012345678901
        --------------------------------
        159,0,0,0211,000,0,0,0,000,0,0,0
        000,0,0,0,0,2,3,0,00000000,1,002
        0000,0
         */

        String rcvBuffer = "null";

        if (hoststatus.length() > 17){
            rcvBuffer = hoststatus.substring(13,16);
        }

        return rcvBuffer;
    }

}