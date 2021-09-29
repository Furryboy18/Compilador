/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * @version 4.4 Con condiciones if else
 * @author martiz
 */
public class Compilador {              
              
    
    /**
     * Lee el archivo y crea el codigo en ensamblador   
     * Actualmente cuenta con opciones para escribir, leer
     * hacer operaciones y deciciones.
     * @param archivo
     * @return 
     */
    public ArrayList compilar(File archivo){
        int cs, ds, es, num = 0;        
        ArrayList contenido = new ArrayList();
        Pattern patNoLinea = Pattern.compile("[0-9]+[:]");                     
        
       // Pattern patAritmetico = Pattern.compile("[+]|[-]|[/]|[*]");
        //Pattern patComparacion = Pattern.compile("[<=?]|[>=?]|[=]"); 
        //Pattern patNumero = Pattern.compile("[0-9]+");        
        Matcher mat ;  
        /**
         * Variables con las lineas donde se puede insertar codigo
         * si se agrega algo en extra sumar 1 a ds y cs
         * si se agrega algo a datos sumar 1 a cs
        */
        es = 4;        
        ds = 7;
        cs = 10;
               
        try {                   
            Scanner entrada = new Scanner(archivo);
            ArrayList varTipo = new ArrayList(); 
            ArrayList varNom = new ArrayList();  
            
            //Sirve para guardar etiquetas que hacen referencia a bloques de codigo   
            Stack<String> bloques = new Stack<String>();
            
            String line, aux, aux2;
            String esc = "";
            char operando;
            int index, fo1, fo2;
            boolean entrada_numerica = false;
            boolean excepcion = false;
            
            contenido.add("pila segment para stack 'stack'");
            //Como decia tanis de no dejar la pila vacia dejo esto	
            contenido.add("   db 100 dup (?)"); 
            contenido.add("pila ends");
            contenido.add("extra segment para public 'data'");
            contenido.add("extra ends");
            contenido.add("datos segment para public 'data'");
            contenido.add("   txtexrang db 10,\"¡Valor de tipo entero fuera del rango!$\""); 
            contenido.add("datos ends");
            contenido.add("codigo segment para public 'code'");
            contenido.add("assume ds:datos , ss:pila , cs:codigo , es:extra");            
            contenido.add("codigo ends");
            contenido.add("   end ppal");     
            
            
            
            while (entrada.hasNextLine()) {                                 
                    try{
                        
                        line = entrada.next().toLowerCase(); //Para leer lexema por lexema                     
                        System.out.print(line+" ");                        
                        
                        mat = patNoLinea.matcher(line); 
                        
                        //guarda el numero de linea para usar como auxiliar al nombrar variables
                        if(mat.find() && !excepcion){  //if(¿Es numero de linea?)
                            num = Integer.parseInt(line.substring(0, line.length()-1));
                        }
                        else if(line.equals("procedimiento") ){
                            varTipo.add(line);                            
                            esc = entrada.next();
                            varNom.add(esc);
                            System.out.print(esc+" ");
                            if(esc.equals("principal")){                                
                                contenido.add(cs++,"   public ppal");
                                contenido.add(cs++,"ppal proc far");
                                contenido.add(cs++,"      push ds");
                                contenido.add(cs++,"      mov ax,0");
                                contenido.add(cs++,"      push ax");
                                contenido.add(cs++,"      mov ax,datos");
                                contenido.add(cs++,"      mov ds,ax");
                                contenido.add(cs++,"      mov ax,extra");
                                contenido.add(cs++,"      mov es,ax");                                  
                                if(entrada.next().equals("iniciar")){
                                    System.out.print("iniciar \n");
                                    entrada.nextLine();
                                }
                            }
                            //else crear procedimiento 
                        }   
                        else if(line.equals("fin_proc")){
                            contenido.add(cs++,"   jmp bien");                            
                            contenido.add(cs++,"   exrang:");
                            contenido.add(cs++,"   LEA DX, txtexrang");
                            contenido.add(cs++,"   MOV AH,9");
                            contenido.add(cs++,"   INT 21H");                                                       
                            contenido.add(cs++,"   bien:");
                            contenido.add(cs++,"   ret");
                            contenido.add(cs++,"ppal endp");                            
                        }
                        else if(line.equals("cadena")){
                            //Toda cadena tiene 2 bytes al comienzo por si se hace lectura.
                            //Toda cadena tiene una longitud maxima de 50 caracteres
                            line = entrada.next(); 
                            varNom.add(line);
                            varTipo.add("cadena");
                            System.out.print(line+" ");
                            contenido.add(ds++, "   "+ line + " db 51, ?, 51 dup (\"$\")"); 
                            cs++;                                                                                    
                        }                        
                        else if(line.equals("caracter") ){
                            line = entrada.next();                            
                            System.out.print(line+" ");
                            varNom.add(line);
                            varTipo.add("caracter");
                            contenido.add(ds++, "   "+ line + " db 0"); 
                            cs++;
                        }                        
                        else if(line.equals("entero" )){
                            line = entrada.next();
                            System.out.print(line+" ");
                            varNom.add(line);
                            varTipo.add("entero");
                            contenido.add(ds++, "   "+ line + " dw 0"); 
                            cs++;
                        }                        
                        else if(line.equals("hacer")){                             
                            line = entrada.nextLine();                            
                            System.out.println(line);
                            //Obtiene la variable antes de la ',' (op de asignacion)                                                                                                                                      
                            index = line.indexOf(',');
                            if(line.indexOf('#')==-1){   
                                //Determina la variable a la cual se asignaran los valores 
                                esc = line.substring(0, index).strip();
                                                                
                                //Se deja ax(ensamblador) en 0 para ir agregando lo que se desea asignar
                                contenido.add(cs++, "       mov ax, 0"); 
                                
                                fo1 = index;                                
                                operando = '+';
                                index++;
                                while(fo1<line.length()-1){
                                                                      
                                    fo1 = line.indexOf('+', index);
                                    if (fo1 == -1 )
                                        fo1 = line.length()-1;                                
                                    fo2 = line.indexOf('-', index);
                                    if (fo2 != -1 )                                    
                                        if(fo2<fo1)
                                            fo1 = fo2;
                                    fo2 = line.indexOf('*',index);
                                    if (fo2 != -1 )                                    
                                        if(fo2<fo1)
                                            fo1 = fo2;
                                    fo2 = line.indexOf('/',index);
                                    if (fo2 != -1 )                                    
                                        if(fo2<fo1)
                                            fo1 = fo2;  
                                                                                        
                                    //Poner en aux el variable depsues del operando
                                    aux = line.substring(index,fo1).strip();
                                                                                                                                            
                                    //Primer operando
                                    switch(operando){                                            
                                        case '-':
                                            contenido.add(cs++, "       \n;resta");           
                                            if(aux.charAt(0)<58){ //es numero constante
                                                
                                                contenido.add(cs++, "       sub ax, "+ aux); 
                                                contenido.add(cs++, "       jo exrang");
                                            }else{                                                                                                
                                                contenido.add(cs++, "       Lea bx, "+ aux);
                                                contenido.add(cs++, "       sub ax, [bx]");  
                                                contenido.add(cs++, "       jo exrang");
                                            }
                                            break;                                                
                                        case '*':
                                            contenido.add(cs++, "       \n;multi"); 
                                            if(aux.charAt(0)<58){ //es numero constante                                                
                                                contenido.add(cs++, "       mov bx, "+ aux); 
                                                contenido.add(cs++, "       imul bx");
                                                contenido.add(cs++, "       jo exrang");
                                            }else{
                                                contenido.add(cs++, "       Lea bx, "+ aux);
                                                contenido.add(cs++, "       imul word ptr [bx]");  
                                                contenido.add(cs++, "       jo exrang");
                                            }
                                            break;
                                        case '/':
                                            contenido.add(cs++, "       \n;divi"); 
                                            if(aux.charAt(0)<58){ //es numero constante     
                                                contenido.add(cs++, "       mov dx, 0"); 
                                                contenido.add(cs++, "       mov bx, "+ aux); 
                                                contenido.add(cs++, "       idiv bx"); 
                                                contenido.add(cs++, "       jo exrang");
                                                //contenido.add(cs++, "       mov ah,0"); 
                                            }else{
                                                contenido.add(cs++, "       mov dx, 0"); 
                                                contenido.add(cs++, "       Lea bx, "+ aux);
                                                contenido.add(cs++, "       idiv word ptr [bx]"); 
                                                contenido.add(cs++, "       jo exrang");
                                                //contenido.add(cs++, "       mov ah,0"); 
                                            }
                                            break;
                                        default:                                                        
                                            if(aux.length()>0){
                                                contenido.add(cs++, "       \n;sumaa"); 
                                                if(aux.charAt(0)<58 ){ //es numero constante                                                
                                                    contenido.add(cs++, "       add ax, "+ aux); 
                                                    contenido.add(cs++, "       jo exrang");
                                                }else{
                                                    contenido.add(cs++, "       Lea bx, "+ aux);
                                                    contenido.add(cs++, "       add ax, [bx]");  
                                                    contenido.add(cs++, "       jo exrang");
                                                }
                                            }                                                                                            
                                    }
                                    operando = line.charAt(fo1);        
                                    index = fo1+1;  
                                }
                                //asigna a la variable que deseamos el valor gaurdado en ax(ensamblador) anteriormente
                                contenido.add(cs++, "       Lea bx, "+esc);  
                                contenido.add(cs++, "       mov [bx], ax");  
                                
                            }else{
                                System.out.print("Realizar operaciones para asignar cadenas");
                                //esc = line.substring(0, index).strip(); //la variable despues del ,
                            }   //line = esc.substring(esc.indexOf('#')+1, esc.length()-3);
                                                                                                      
                            
                        }                                                
                        else if(line.equals("escribir")){                          
                            esc = entrada.nextLine();
                            System.out.print(esc + " ");                              
                            if(esc.charAt(1)=='#'){ //Es literal
                                esc= esc.substring(2, esc.length()-3);  
                                contenido.add(ds++, "   x" + num + " db " + "\""+ esc+ "$\""); 
                                cs++;
                                contenido.add(cs++, "      LEA dx, x"+num);
                                contenido.add(cs++, "      MOV ah, 9h");
                                contenido.add(cs++, "      INT 21H"); 
                            }
                            else{ 
                                esc= esc.substring(1, esc.length()-2); 
                                index = varNom.indexOf(esc);                                
                                if(esc.charAt(0)<58 || varTipo.get(index).equals("entero")){ //Es un numero
                                    
                                    if(esc.charAt(0)<58){ //es numero constante
                                        contenido.add(cs++, "       mov ax, "+esc);
                                    }else{ //es numero variable
                                        contenido.add(cs++, "       Lea bx, "+esc);
                                        contenido.add(cs++, "       mov ax, [bx]");
                                    }
                                    //esta primera parte es para ver si es negativo
                                    contenido.add(cs++, "       cmp ax,0 ");
                                    contenido.add(cs++, "       jge ns"+num);
                                    contenido.add(cs++, "           mov cx,ax");
                                    contenido.add(cs++, "           mov dl,45");
                                    contenido.add(cs++, "           MOV AH,2");
                                    contenido.add(cs++, "           INT 21H");
                                    contenido.add(cs++, "           mov ax,cx");
                                    contenido.add(cs++, "           neg ax");
                                    contenido.add(cs++, "       ns"+num+":");                                                                                                            
                                                                                                            
                                    //aqui imprime el positivo en ax
                                    contenido.add(cs++, "       mov bp, 0ah");
                                    contenido.add(cs++, "       mov cx,00");
                                    contenido.add(cs++, "       sa"+num+":");
                                    contenido.add(cs++, "               mov dx,00");
                                    contenido.add(cs++, "       	div bp");
                                    contenido.add(cs++, "       	push dx");
                                    contenido.add(cs++, "       	inc cx");
                                    contenido.add(cs++, "       	cmp ax,00");
                                    contenido.add(cs++, "          jne sa"+num);
                                    contenido.add(cs++, "       imp"+num+":");
                                    contenido.add(cs++, "           pop dx");
                                    contenido.add(cs++, "           add dx,30h");
                                    contenido.add(cs++, "           MOV AH,2");
                                    contenido.add(cs++, "           INT 21H ");
                                    contenido.add(cs++, "       loop imp"+num);                                    
                                }else { //Es una variable cadena                                          
                                    //Esta primera parte es para agregar el $ al final de la cadena
                                    contenido.add(cs++, "       Lea bx, "+esc);
                                    contenido.add(cs++, "       mov dl,[bx+1]");
                                    contenido.add(cs++, "       mov dh,0");
                                    contenido.add(cs++, "       mov si,dx");
                                    contenido.add(cs++, "       add bx,2	");
                                    contenido.add(cs++, "       Mov [bx+si],36"); 
                                    //Esta es la interrupcion para escribir en pantalla 
                                    contenido.add(cs++, "       mov dx,bx");
                                    contenido.add(cs++, "       Mov ah, 9h");
                                    contenido.add(cs++, "       INT 21H");   
                                }
                            }                 
                            contenido.add(cs++, "       MOV dl,10 "); //Salto de linea
                            contenido.add(cs++, "       MOV AH,2 ");
                            contenido.add(cs++, "       INT 21H");  
                            
                            System.out.println("");
                        }
                        
                        else if(line.equals("leer")){ //Esto viene en el programa TE02B12     
                                /**
                                 * Esta parte asume que toda variable ´puesta en lectura
                                 * esta declarada en ensamblador para ser de lectura (con 
                                 * los dos bytes al comienzo que indican tamaño de la 
                                 * cadena y caracteres ingresado)
                                 */                                
                                line = entrada.next();
                                System.out.print(line+" ");                                
                                contenido.add(cs++, "       ;leer el dato");
                                contenido.add(cs++, "       lea dx, "+line);
                                contenido.add(cs++, "       mov ah, 0ah");
                                contenido.add(cs++, "       int 21h");                                
                                                                                                                                
                                contenido.add(cs++, "       MOV dl,10 ");
                                contenido.add(cs++, "       MOV AH,2 ");
                                contenido.add(cs++, "       INT 21H");                                
                        }    
                        else if(line.equals("leern")){   // Lectura para numeros                                                         
                                line = entrada.next();
                                System.out.print(line+" ");
                                
                                if(!entrada_numerica){
                                    contenido.add(ds++, "   n db 6, ?, 6 dup (?)"); 
                                    cs++;
                                    entrada_numerica=true;
                                }                                
                                                                
                                contenido.add(cs++, ";leer el dato");
                                contenido.add(cs++, "       lea dx, n");
                                contenido.add(cs++, "       mov ah, 0ah");
                                contenido.add(cs++, "       int 21h");                                
                                
                                contenido.add(cs++, "       mov bx, dx");       
                                contenido.add(cs++, "       inc BX");       
                                contenido.add(cs++, "       MOV CH,00");       
                                contenido.add(cs++, "       MOV CL, [BX]");       
                                contenido.add(cs++, "       mov si,0Ah");       
                                contenido.add(cs++, "       MOV ah,0");
                                contenido.add(cs++, "       inc BX");
                                contenido.add(cs++, "       mov al, [BX]");
                                contenido.add(cs++, "       sub al,30h");
                                contenido.add(cs++, "       cmp cx,1");
                                contenido.add(cs++, "       je sa"+num);
                                contenido.add(cs++, "       dec cx");
                                contenido.add(cs++, "       cal"+num+":");
                                contenido.add(cs++, "       mul si");
                                contenido.add(cs++, "       jc nrang"+num);
                                contenido.add(cs++, "       inc bx");
                                contenido.add(cs++, "       mov DL, [BX]");
                                contenido.add(cs++, "       sub DL,30h");
                                contenido.add(cs++, "       add AX,DX");
                                contenido.add(cs++, "       jc nrang"+num);
                                contenido.add(cs++, "       loop cal"+num);
                                contenido.add(cs++, "       sa"+num+":");
                                contenido.add(cs++, "       jmp bien"+num);
                                contenido.add(cs++, "       nrang"+num+":");
                                contenido.add(cs++, "       mov ax, 0");
                                contenido.add(cs++, "       bien"+num+":");
                                contenido.add(cs++, "       Lea bx,"+line);
                                contenido.add(cs++, "       mov [bx],ax");                                                                                              
                                
                                contenido.add(cs++, "       MOV dl,10 "); //Salto de linea
                                contenido.add(cs++, "       MOV AH,2 ");
                                contenido.add(cs++, "       INT 21H");                                
                        }   
                        
                        else if(line.equals("si") || line.equals("o_si")  ){   
                            /*
                             Todo if esta encerrado bajo dos etiquetas de modo
                             que todo if tiene su propio bloque local y pone un 
                             bloque mayor para encerrar todo en caso de la 
                             existencia de una sentencia o_si
                            */
                            if(line.equals("si")){
                                bloques.push("si"+num);                                
                            }else{  //'o_si' implica que hubo un 'si' antes
                                aux= bloques.pop();  //Se elimina el bloque local del if anterior        
                                //en dado caso que se cumpla la condicion se saltara 
                                //    a la etiqueta del bloque mayor                                
                                contenido.add(cs++, "       jmp "+bloques.peek()); 
                                contenido.add(cs++, "       "+aux+":"); //Etiqueta del bloque local
                            }
                            bloques.push("osi"+num); //agregar nombre etiqueta local
                                
                            line = entrada.nextLine();                            
                            System.out.println(line);                                                        
                            
                            //Se determina el index donde está el operando
                            index = line.indexOf('<');
                            if(index == -1){
                                index = line.indexOf('>');
                                if(index == -1){
                                    index = line.indexOf('\\');
                                    if (index == -1)
                                        index = line.indexOf('=');                                        
                                }
                            }
                            
                            operando = line.charAt(index);
                            if(line.indexOf('#') == -1){ //No hay una cadena                            
                                esc = line.substring(0, index).strip(); //Primer variable
                                
                                fo1 = line.indexOf("tons");
                                
                                if(line.charAt(index+1)== '='){
                                    aux = line.substring(index+2, fo1).strip();                                       
                                }else{
                                    aux = line.substring(index+1, fo1).strip();
                                }
                                
                                //Primera parte de la decision
                                if(esc.charAt(0)<65){ //no tiene letras es constante
                                    contenido.add(cs++, "       mov ax,"+esc);
                                }else{                                    
                                    contenido.add(cs++, "       Lea bx,"+esc);
                                    contenido.add(cs++, "       mov ax,[bx]");
                                }
                                
                                //segunda parte de la decision
                                if(aux.charAt(0)<65){ //no tiene letras es constante
                                    contenido.add(cs++, "       cmp ax,"+aux);
                                }else{                                    
                                    contenido.add(cs++, "       Lea bx,"+aux);
                                    contenido.add(cs++, "       cmp ax,[bx]");
                                }
                                
                                //Considera los saltos con signo de ensamblador
                                switch(operando){                                            
                                     case '>':                                              
                                        if(line.charAt(index+1)== '='){ //es numero constante
                                             contenido.add(cs++, "       jl "+ bloques.peek()+ ";>=");     
                                        }else{                                                                                                
                                            contenido.add(cs++, "       jle "+ bloques.peek()+ ";>");     
                                        }
                                        break;                                                
                                    case '<':                                        
                                        if(line.charAt(index+1)== '='){ //es numero constante                                                
                                            contenido.add(cs++, "       jg "+ bloques.peek()+ ";<=");  
                                        }else{
                                            contenido.add(cs++, "       jge "+ bloques.peek()+ ";<");  
                                        }
                                        break;
                                    case '/':                                        
                                        contenido.add(cs++, "       je "+ bloques.peek()+ ";=/");  
                                        break;
                                    default:             
                                       contenido.add(cs++, "       jne "+ bloques.peek()+ ";=");                                                                                           
                                }                                
                            }//Else es cadena
                        }
                        
                        else if(line.equals("fin_si")){ 
                            
                            line = entrada.nextLine();                            
                            System.out.println(line);                            
                            esc = bloques.pop();                              
                            contenido.add(cs++, "       "+ esc + ": ;"+num);                            
                            contenido.add(cs++, "       "+ bloques.pop() + ":");
                        }
                                                                                                                                                                                            
                        else if(line.equals("!")){                            
                            System.out.print("\n");
                            entrada.nextLine();
                        }
                        
                        
                        
                    }catch(NoSuchElementException ex){}                                                            
            }                        
            entrada.close();
            
        } catch (FileNotFoundException ex) {}    
        return contenido;
    }
}

