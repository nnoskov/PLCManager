package com.home.hhb.PLCManager;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;


public class LaptopServer {

    public static final String LOG_TAG = "ToyopucManager";

    //ip адрес сервера, который принимает соединения
    private String mServerName;

    //номер порта на который сервер принимает соединения
    private int mServerPort;

    //сокет через который приложение общается с сервером
    private Socket mSocket = null;

    //количество байт в сообщении ответе
    private int count;

    public LaptopServer(String serverName, int serverPort) {
            mServerName = serverName;
            mServerPort = serverPort;
    }

    /**
     * Открытие нового соединения. Если сокет уже открыт, то он закрывается.
     * @throws Exception
     *      Если не удалось открыть сокет
     */
    public void openConnection () throws Exception {

        /*освобождаем ресурсы*/
        closeConnection();

        try {
            /*создаем новый сокет, указываем на каком компьютере и порту запущен наш процесс
            * который будет принимать наше соединение*/
            mSocket = new Socket(mServerName,mServerPort);

        } catch (IOException e){
            throw new Exception("Невозможно создать сокет: " + e.getMessage());
        }

    }

    /**
     * Метод для закрытия сокета, по которому мы общались.
     */
    public void closeConnection (){

        /*проверяем сокет. Если он не закрыт, то закрываем его и освобождаем соединение.*/
        if (mSocket != null && !mSocket.isClosed()){

            try {
                mSocket.close();
            }catch (IOException e){
                Log.e(LOG_TAG,"Невозможно закрыть сокет: "+ e.getMessage());
                } finally {
                    mSocket = null;
                }
        }
        mSocket = null;

    }

    /**
     * Метод для отправки данных по сокету
     *@param data
     *          данные, которые будут отправлены
     *@throws Exception
     *          если невозможно отправить данные
     */
     public void sendData(byte[] data) throws Exception {
         if (mSocket == null || mSocket.isClosed()){
             throw new Exception("Невозможно отправить данные. Сокет не создан или закрыт");
         }
         /*Отправка данных*/
         try {
             mSocket.getOutputStream().write(data);
             mSocket.getOutputStream().flush();
         } catch (Exception e){
             throw new Exception("Невозможно отправить данные" + e.getMessage()+"Class name: "
                     +this.getClass().toString());
         }
     }
    /**
     * Метод для получения ответных данных по сокету
     *@return  responseData
     *          данные, которые будут получены
     *@throws Exception
     *          если невозможно отправить данные
     */
     public byte[] getResponseData() throws Exception {

         byte[] buffer = new byte [1024*4];
         if (mSocket == null || mSocket.isClosed()){
             throw new Exception("Невозможно принять данные. Сокет не создан или закрыт");
         }
         /*получение данных*/
         try {
             count = mSocket.getInputStream().read(buffer);
             byte[] responseData = new byte[count];
             System.arraycopy(buffer,0,responseData,0,count);
             return responseData;

         } catch (Exception e){
             throw new Exception("Невозможно получить данные "+e.getMessage()+" Class name: "
                      +this.getClass().toString());
         }
    }

    /*Метод возвращает реальное количество байт, которые были получены*/
    public int getResponseCount(){
         return count;
    }


    @Override
    protected void finalize () throws Throwable {
         super.finalize();
         closeConnection();
    }

}
