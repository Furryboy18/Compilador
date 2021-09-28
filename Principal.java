package compiladordefinitivo;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * @version 4.0
 * @author martiz
 * @author cris
 */
public class Principal {
    public static TablaDeTokens tdt = null;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
            // TODO code application logic here
            Archivos archivos = new Archivos();
            Depurador depurar = new Depurador();
            Compilador compilador = new Compilador();
            Scanner leer = new Scanner(System.in);
            
            String ruta, nombre;
            System.out.print("Ingresar la ruta del archivo: ");
            ruta = leer.nextLine();
            System.out.print("Ingresar el nombre del archivo: ");                        
            nombre = leer.next();
                                    
            File depurado = archivos.abrirArchivo(ruta+"/"+nombre);      
            if(depurado.isFile()){
                String texto;
                ArrayList contenido;
                ArrayList token = null;
                //texto esta depurado
                texto = depurar.depurarArchivo(depurado);                                  
                contenido = separarLexemas(texto);
                
                
                tdt = new TablaDeTokens(contenido);
                token = tdt.getToken();
                System.out.println("\nVersión de compilador XD: 4.0.1");
                System.out.println("\nTabla de tokens:");
                    System.out.printf("\n%-20s| %29s |%19s\n","Lexema","Token","Tipo");
                    System.out.print("---------------------------------------------------------------------------");                 
                for(int i = 0; i < token.size(); i ++){
                     System.out.printf("\n%-20s %30s %20s",tdt.getLexema(i),tdt.getToken(i),tdt.getTipo(i));
                }
                System.out.println("\n ");
                
                //Si no hay error en el depurado lo guardamos
                if(!tdt.reportarTokensDesconocidos()){
                    archivos.guardarArchivo(depurado.getPath(),texto,"cm"); 
                    int index = nombre.indexOf('.');
                    nombre = nombre.substring(0, index+1) + "cm";

                    File archivo = new File(ruta+"\\"+nombre);                  
                    if(archivo.isFile()){
                        ArrayList<String> compilado;   
                        StringBuilder str = new StringBuilder();  
                        
                        System.out.println("Contenido del archivo: "); 
                        compilado = compilador.compilar(archivo);                                        
                        //System.out.println(compilado); 
                                
                        texto="";
                        while (!compilado.isEmpty()) {
                            str.append(compilado.get(0)).append("\n");
                            compilado.remove(0);
                        }        

                        texto = str.toString();
                        archivos.guardarArchivo(ruta+"\\"+nombre,texto,"asm");

                    }else        
                        System.out.println("Codigo objeto no encontrado."); 
                }
                               
            }else        
                System.out.println("Archivo no encontrado.");   
 
            
    }
    
    /**
     * Lee el archivo
     * @param archivo
     * @return 
     */
    public static ArrayList separarLexemas(String archivo){
        ArrayList contenido = new ArrayList();
        Scanner entrada = new Scanner(archivo);
        String line;
        boolean cadena;
        int bi;
        char aux;
        
        while (entrada.hasNextLine()) { //Hay que poner cada lexema por sepado
            line = entrada.nextLine();
            bi = 0;
            cadena = false;
            //ei = end index, bi = end index
            for(int ei = 0; ei< line.length(); ei++){
                aux =line.charAt(ei);
                if(aux == ':' && bi==0){
                    contenido.add(line.substring(bi, ei+1));
                    bi=ei+1;
                }else if(!cadena){
                    //ei+1 es cuando queremos el caracter
                    //ei es cuando no lo queremos
                    if(aux=='#'){
                        cadena = true;
                    }else if(aux == ' '){ //no guarda espacios
                        if(bi != ei)
                            contenido.add(line.substring(bi, ei));
                        bi=ei+1;
                    }else if(aux == '<' || aux == '>'){
                        if(bi != ei){
                            contenido.add(line.substring(bi, ei)); //guarda lo anteiror
                        }
                        if(line.charAt(ei+1)=='='){ 
                            contenido.add(line.substring(ei, ei+2)); //Guarda el simbolo
                            ei++;
                        }else
                            contenido.add(line.substring(ei, ei+1)); //Guarda el simbolo
                        bi=ei+1;
                    }else if(aux >=48 && aux <=57){ //numero
                        continue;
                    }else if((aux >=65 && aux <=90) || aux =='Ñ'){ //mayuscula incluye ñ
                        continue;
                    }else if((aux >=97 && aux <=122) || aux =='ñ'){ //minuscula
                        continue;
                    }else if(aux == '_'){ //tros
                        continue;
                    }else {
                        if(bi != ei){
                            contenido.add(line.substring(bi, ei));   //guarda lo anteiror
                        }
                        contenido.add(line.substring(ei, ei+1)); //Guarda el simbolo
                        bi=ei+1;
                    }
                }else{
                    if(line.charAt(ei)=='#'){
                        cadena = false;
                        contenido.add(line.substring(bi, ei+1));
                        bi=ei+1;
                    } 
                }
            }
            //guarda lo que quedo
            if(bi != line.length())                
                contenido.add(line.substring(bi, line.length()));                
        }
        entrada.close();
        return contenido;
    }                    
    
    public static boolean validarExt(String nombre){
        //Validar extension del archivo
        int index, auxindex;        
        String ext = "cm";
        index = nombre.indexOf('.');
        //Busca el indice de la ruta donde esta el ultimo '.' para la extension
         do{     
            auxindex = nombre.indexOf('.', index+1);
            if(auxindex != -1){ 
                index = auxindex;
            }else
                break;
        }while(true);
        if(index != -1){        
            if((nombre.substring(index+1, nombre.length())).equals(ext)){
                return true;
            }
        }
        return false;
    }
                
}
