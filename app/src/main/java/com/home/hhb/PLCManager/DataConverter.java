package com.home.hhb.PLCManager;

import android.util.Log;

import java.math.BigInteger;
import java.util.Arrays;

import static com.home.hhb.PLCManager.LaptopServer.LOG_TAG;

public class DataConverter {

    public DataConverter(){

    }

    /**
     * Метод для подготовка строки для преобразования в массив байтов
     * @param notReadyString
     *                  команда в виде строки с разделителем "-"
     * @throws Exception
     *                  если указана строка не в формате HEX
     */
    private static String stringRegDeletion  (String notReadyString) {

        String readyString = null;

        try{
            readyString = notReadyString.replaceAll("-","");
        }catch (Exception e){
            Log.e(LOG_TAG,"Проблемы с удалением тире: " + e.getMessage());
        }
        Log.i(LOG_TAG,"Входная строка после обработки: "+ readyString);
        return readyString;
    }


    /**Статический метод преобразует команду в массив двоичных данных для отправки в контроллер
     * @param stringCommand
     *           Передаваемая команда в формате последовательности символов формате HEX
     *           ex. 00-00-05-00-3D-45-1A-63-D7
     * @return data
     *          двоичный массив данных для отправки в контроллер
     *
     * */
    public static byte[] getBytes (String stringCommand) {

       Log.i(LOG_TAG,"Входящая команда в виде строки: "+ stringCommand);

       String readyStringCommand = stringRegDeletion(stringCommand);

       int len = readyStringCommand.length();

           byte[] data = new byte[len / 2];
                for (int i = 0; i < len; i += 2) {
                    data[i / 2] = (byte) ((Character.digit(readyStringCommand.charAt(i), 16) << 4)
                    + Character.digit(readyStringCommand.charAt(i+1), 16));
                }
            Log.i(LOG_TAG,"Массив байтов перед отправкой: "+ Arrays.toString(data));
            return data;
     }


     /**
      * Статический метод возвращает строку в HEX формате из полученного массива байтов
      * @param byteResponse
      *             массив данных, полученный от контроллера
      * @return readyStringResponse
      *             возвращаемя строка в HEX формате c
      *
      * */
     public static String getString (byte[] byteResponse){

         BigInteger bi = new BigInteger(1,byteResponse);

         Log.i(LOG_TAG,"Массив байтов полученный от контроллера: "+ Arrays.toString(byteResponse));
         String stringResponseHex = String.format("%0"+(byteResponse.length <<1)+"X",bi);
         Log.i(LOG_TAG,"Строка в формате HEX полученная от контроллера: "+ stringResponseHex);

         return stringRegAddition(stringResponseHex);
         //return stringResponseHex;
     }

     private static String stringRegAddition (String notReadyString){

         String readyString = "";

         int len = notReadyString.length()/2 - 1;

         /*Првоерка на полноту байтов*/
         if ((notReadyString.length() % 2) != 0) {
               Log.i(LOG_TAG,"Присутствует неполный байт в команде ответе: ");
                return readyString;
         }else{
                try {
                    int j = 2;
                    StringBuffer sb = new StringBuffer(notReadyString);
                            for (int i=1; i <= len; i++){
                            sb.insert(j,"-");
                            j += 3;
                        }
                readyString = sb.toString();
             }catch (Exception e){
                    Log.e(LOG_TAG, e.getMessage());
                }
         }
         return readyString;
     }

}


