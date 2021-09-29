/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author martiz
 * @author cris
 */
public class Archivos {
    public File abrirArchivo(String ruta){        
        File a = new File(ruta);           
        return a;      
    }
    
    /**
     * 
     * @param archivo
     * @return 
     */
    public String leerArchivo(File archivo){
        try {                
            Scanner entrada = new Scanner(archivo);
            String line = "";
            while (entrada.hasNextLine()) {                
                line += entrada.nextLine() +"\n";
            }                        
            entrada.close();
            return line;
        } catch (FileNotFoundException ex) {
            return "";
        }        
    }
    
    public void guardarArchivo(String ruta, String texto, String ext){
        FileWriter fw= null;
        if(!texto.equals("")){                    
            try{
                int index, auxindex;
                String sub;                       
                index = ruta.indexOf('.');

                //Busca el indice de la ruta donde esta el ultimo '.' para la extension
                do{     
                    auxindex = ruta.indexOf('.', index+1);
                    if(auxindex != -1){ 
                        index = auxindex;
                    }else
                        break;
                }while(true);

                ruta = ruta.substring(0, index+1) + ext; //la ruta pero con la nueva extension            

                //checa si ya existe y lo borra para que no se sobreescriba
                File archivo1 = new File(ruta); 
                archivo1.delete();  

                fw=new FileWriter(ruta,true);                        
                fw.write(texto);
                System.out.println("\nArchivo guardado, cheque la ruta: "+ruta);
            }
            catch(IOException e){
                System.out.println("Error: "+e.getMessage());
            }
            finally{
                try{
                    if(fw!=null)
                        fw.close();
                }
                catch(IOException e){
                    System.out.println("Error:"+e.getMessage());
                }
            }     
        }
    }
        
}
