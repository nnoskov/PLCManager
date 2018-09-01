package com.home.hhb.PLCManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import static com.home.hhb.PLCManager.LaptopServer.LOG_TAG;

public class ToolsActivity extends AppCompatActivity {

    private Button mButtonOpen = null;
    private Button mButtonSend = null;
    private Button mButtonClose = null;
    private Button mButtonSave = null;
    private LaptopServer mServer;
    private EditText plcIP;
    private String mPlcIP;
    private EditText plcPort;
    private int mPlcPort;
    private SharedPreferences mSocketParam;
    private EditText mStringCommand = null;
    private TextView mResponse = null;
    private ImageView mConnectionStatus = null;
    final String SAVED_IP = "saved_ip";
    final String SAVED_PORT = "saved_port";
    String stringResponse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

        /*Добавление стрелки возврата на экшенбар активити*/
        if (getSupportActionBar() != null){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle(R.string.title_tools_action_bar);
        }

        /*Получеине объектов из loyout-a*/
        mButtonOpen = (Button) findViewById(R.id.button_open_connection);
        mButtonSend = (Button) findViewById(R.id.button_send_connection);
        mButtonClose = (Button) findViewById(R.id.button_close_connection);
        mButtonSave = (Button) findViewById(R.id.btnSave);

        plcIP = (EditText) findViewById(R.id.plcIp);
        plcPort = (EditText) findViewById(R.id.plcPort);

        mStringCommand = (EditText) findViewById(R.id.mStringForSend);
        mResponse = (TextView) findViewById(R.id.mStringReturn);
        mConnectionStatus = (ImageView) findViewById(R.id.mConnectionStatus);

        /*Загрузка параметров сокета при запуске Activity в тектовые поля*/
        loadSocketParam(plcIP,plcPort);

        /*подготовка параметров сокета*/
        socketPreparation(plcIP,plcPort);

        /*Ввод IP адреса в тестовое поле для воода IP*/
        plcIP.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN&&(i == KeyEvent.KEYCODE_ENTER)){

                    /*Сохраняем IP адрес из EditText в строку*/
                    mPlcIP = plcIP.getText().toString();
                    return true;
                }
                return false;
            }
        });

        /*Ввод порта в тестовое поле для ввода*/
        plcPort.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN&&(i == KeyEvent.KEYCODE_ENTER)){

                    /*Сохраняем порт числом int*/
                    mPlcPort = Integer.parseInt(plcPort.getText().toString());
                    return true;
                }
                return false;
            }
        });

        /*Деактивируем кнопки send и close*/
        mButtonSend.setEnabled(false);
        mButtonClose.setEnabled(false);
        mStringCommand.setEnabled(false);

        /*Установка слушателя на кнопку сохранения параметров соединения*/
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSocketParam(plcIP, plcPort);
            }
        });

        /*установка слушателя на кнопку открытия соединения*/
        mButtonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Создаем объект для работы с сервером*/
                mServer = new LaptopServer(mPlcIP,mPlcPort);

                /*открываем соединение. Открытие должно происходить в отдельном потоке от ui*/
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mServer.openConnection();

                            /* устанавливаем активные кнопки для отправки данных
                            и закрытия соединения. Все данные по обработке интерфейса должны
                            обрабатываться в UI потоке, а так как мы сейчас находимся в отдельном
                            потоке, нам необходимо вызвать метод runOnUiThread()
                            */
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mButtonSend.setEnabled(true);
                                    mButtonClose.setEnabled(true);
                                    mStringCommand.setEnabled(true);
                                    mConnectionStatus.setImageResource(android.R.drawable.presence_online);
                                }
                            });

                        } catch (Exception e){
                            Log.e (LOG_TAG, e.getMessage());
                            mServer = null;
                        }
                    }
                }).start();
            }
        });

        /*установка слушателя на кнопку отправки данных*/
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mServer == null ) {
                    Log.e(LOG_TAG,"Сервер не найден");
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            /*отправляем данные на контроллер*/
                            String stringCommand = new String(mStringCommand.getText().toString());
                            // TODO: 28.07.2018 Настроить передачу данных в пакет согласно мануалу
                            byte [] mDataForSend = DataConverter.getBytes(stringCommand);
                            mServer.sendData(mDataForSend);

                            /*Получаем ответные данные с контроллера*/
                            byte[] mDataResponse = mServer.getResponseData();
                            int responseCount = mServer.getResponseCount();
                            if (responseCount >0){
                                stringResponse = DataConverter.getString(mDataResponse);

                                /*Установка текста в поле "response command"*/
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mResponse.setText(stringResponse);
                                    }
                                });

                            }else
                                //если получили -1, значит прервался поток данных
                                if (responseCount == -1){
                                    Log.i(LOG_TAG,"Поток данных прервался");
                                    mServer.closeConnection();
                                }
                        }catch (Exception e){
                            Log.e(LOG_TAG, e.getMessage() + " Class Name: "+ this.getClass().getName());
                        }
                    }
                }).start();
            }
        });

        /*установка слушателя на кнопку закрытия соединения*/
        mButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Закрываем соединение*/
                mServer.closeConnection();

                /*устанавливаем неактивными кнопки отправки и закрытия*/
                mButtonClose.setEnabled(false);
                mButtonSend.setEnabled(false);
                mConnectionStatus.setImageResource(android.R.drawable.presence_offline);
            }
        });
    }
    /*При нажатии на кнопку возврата, завершаем активити Tools*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Метод для сохранения параметров сокета IP + PORT
     * @param pPlcIP, pPlcPort
     */
    protected void saveSocketParam (EditText pPlcIP, EditText pPlcPort){
        mSocketParam = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = mSocketParam.edit();
        ed.putString(SAVED_IP,pPlcIP.getText().toString());
        ed.commit();
        ed.putString(SAVED_PORT,pPlcPort.getText().toString());
        ed.commit();
        //Toast.makeText(this, "Parameters saved",Toast.LENGTH_SHORT).show();
        socketPreparation(pPlcIP,pPlcPort);
    }
    /**
     * @param pPlcIP, pPlcPort
     * Метод для загрузки последних сохраненных параметров
     * (используется при загрузке Activity)
     * */
    protected void loadSocketParam (EditText pPlcIP, EditText pPlcPort){
        mSocketParam = getPreferences(MODE_PRIVATE);
        String ipParam = mSocketParam.getString(SAVED_IP,"");
        pPlcIP.setText(ipParam);
        String portParam = mSocketParam.getString(SAVED_PORT,"");
        pPlcPort.setText(portParam);
        /*if (ipParam != "" && portParam != ""){
            Toast.makeText(this, "Parameters loaded",Toast.LENGTH_SHORT).show();
        }*/
    }
    /*
     * Преобраование теста из полей в необходимые для создания сокета
     * */
    protected void socketPreparation (EditText pPlcIP, EditText pPlcPort){
        try {
            mPlcIP = pPlcIP.getText().toString();
            mPlcPort = Integer.parseInt(pPlcPort.getText().toString());
        }catch (Exception e){
            Log.e(LOG_TAG,"Пустые значения полей IP:PORT " +e.getMessage());
        }

    }
    /*
     * Сохранение параметров сокета при закрытии Activity
     * */
    protected void onDestroy(){
        super.onDestroy();
        saveSocketParam(plcIP,plcPort);
    }

}
