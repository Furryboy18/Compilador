/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladordefinitivo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 *
 * @author martiz
 * @author cris
 */
public class Depurador {
    public String depurarArchivo(File archivo){
        try {    
            Scanner entrada = new Scanner(archivo);
            int numL = 0;
            String line = "";
            String linAct = "";
            while (entrada.hasNextLine()) {
                linAct = depurar(entrada.nextLine());//Se depura cada linea
                numL++;//numero de línea, solo se imprime si es valida la linea
                if(linAct == null){//Si la depuración regresa nulo, significa que se debe de elminar la línea
                    continue;
                }
                line += numL+": "+linAct +"\n";
            }                        
            entrada.close();
            return line;
        } catch (FileNotFoundException ex) {
            return "";
        }        
    }
    
    public static String depurar(String linea){
        //assd11*++}}{} mientras sea carcter diferente de # y 1 y a poner espacio
        String resp = "";
        char aux = ' ';
        int cont = 0;
        boolean ban = true;
        if(linea.equals(""))                            //Verifica que la linea no este vacía
            return null;
        while(linea.charAt(cont) == 32 || linea.charAt(cont) == 9){//Elimina los espacios o tabulaciones del principio (sangría)
            cont++;
            if(cont == linea.length()){
                return null;
            }
        }
        
        do{
            if(cont >= linea.length()){                 //verifica su aún existen caracteres en la línea
                break;
            }
            if(linea.charAt(cont) == 32 && cont > 0){   ///////////////ESPACIO/////////////////
                if(linea.charAt(cont-1) == 32){         //si el anterior caracter es un espacio, no se conserva el actual
                    cont++;
                    continue;
                }
                else{
                    resp += linea.charAt(cont);
                    cont++;
                }
            }
            else if(linea.charAt(cont) == 45 && cont < linea.length()-1){////////////////////COMENTARIO///////////////////
                if(linea.charAt(cont+1) == 45){//Si el siguiente caracter es un -, significa que si es un comentario y no es necesario imprimir mas desde aqui
                    ban = false;
                }
                else{
                    resp += linea.charAt(cont);
                    cont++;
                }
            }
            else if(linea.charAt(cont) == 35){///////////////////HASHTAG #/////////////////
                resp += linea.charAt(cont);
                do{                     //Se va a permitir cualquier caracter hasta que se encuentre otro # o se termine la cadena
                    cont++;
                    if(linea.length() == cont){
                        ban = false;
                        break;
                    }
                    resp += linea.charAt(cont);
                }while(linea.charAt(cont) != 35);
                cont++;
            }
            else if(linea.charAt(cont) == 33){//////////////////// el "!" ////////////////////                
                resp += " ";
                resp += linea.charAt(cont);
                ban = false;                //Si se encuentra con un ! significa que se termino la linea y ya no es necesario imprimir mas
            }
            else{
                resp += linea.charAt(cont); //Si no es ninguno de los anteriores casos se guarda lo que sea que se encuentre
                cont++;
            }
        }while(ban == true);
        
        if(resp.equals("")){                //Verifica que no este vacía la cadena(en caso de que exista espacios seguidos de un comentario)
            return null;
        }
        else if(resp.charAt(resp.length()-1) == 32)//si se quedo algun espacio al final de la línea, se elimina y envía
            return resp.substring(0, resp.length()-2);
        return resp;                        // Si no hay problemas se envía la cadena :)
    }
}
