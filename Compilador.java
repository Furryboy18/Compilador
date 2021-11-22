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
    ArrayList contenido;  
    int cs, ds, es, num = 0; 
    Stack<String> bloques;
    
    /**
     * Lee el archivo y crea el codigo en ensamblador   
     * Actualmente cuenta con opciones para escribir, leer
     * hacer operaciones y deciciones.
     * @param archivo
     * @return 
     */
    public ArrayList compilar(File archivo){           
        contenido = new ArrayList();
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
            bloques = new Stack<String>();
            
            String line, aux, aux2;
            String esc = "";
            char operando;
            int index, fo1;
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
                                //System.out.println(line);
                                
                                aritmetica(line.substring(index + 1 ,line.length() -1));
                                
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
                            index = indexOp(line);                                     
                            System.out.println(line.charAt(index));
                            
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
                                if(esc.charAt(0)=='('){ //Es un bloque
                                    fo1 = esc.lastIndexOf(')');
                                   
                                    bloqueLogico(esc.substring(1, fo1),"bl",0);                                    
                                    contenido.add(cs++, "       mov ax,dx");
                                }else if(esc.charAt(0)<65){ //no tiene letras es constante
                                    contenido.add(cs++, "       mov ax,"+esc);                                
                                }else{                                    
                                    contenido.add(cs++, "       Lea bx,"+esc);
                                    contenido.add(cs++, "       mov ax,[bx]");
                                }
                                
                                //segunda parte de la decision
                                if(aux.charAt(0)=='('){ //Es un bloque
                                    fo1 = aux.lastIndexOf(')');                                    
                                    bloqueLogico(aux.substring(1, fo1),"blq",0);                              
                                    
                                }else if(aux.charAt(0)<65){ //no tiene letras es constante
                                    contenido.add(cs++, "       mov dx,"+aux);                                                                                                                                                                                                          
                                }else{                                    
                                    contenido.add(cs++, "       Lea bx,"+aux);
                                    contenido.add(cs++, "       mov dx,[bx]");                                                                                                      
                                }
                                                                
                                //Considera los saltos con signo de ensamblador
                                switch(operando){                                            
                                     case '>':  
                                        contenido.add(cs++, "       cmp ax,dx");
                                        if(line.charAt(index+1)== '='){ //es numero constante
                                             contenido.add(cs++, "       jl "+ bloques.peek()+ ";>=");     
                                        }else{                                                                                                
                                            contenido.add(cs++, "       jle "+ bloques.peek()+ ";>");     
                                        }
                                        break;                                                
                                    case '<':   
                                        contenido.add(cs++, "       cmp ax,dx");
                                        if(line.charAt(index+1)== '='){ //es numero constante                                                
                                            contenido.add(cs++, "       jg "+ bloques.peek()+ ";<=");  
                                        }else{
                                            contenido.add(cs++, "       jge "+ bloques.peek()+ ";<");  
                                        }
                                        break;
                                    case '/': 
                                        contenido.add(cs++, "       cmp ax,dx");
                                        contenido.add(cs++, "       je "+ bloques.peek()+ ";=/");  
                                        break;
                                    case 'ó':                   
                                        contenido.add(cs++, "       OR ax,dx");               
                                        contenido.add(cs++, "       cmp ax,0");                
                                        contenido.add(cs++, "       je "+ bloques.peek()+ ";ó");  
                                    break;
                                    case 'í':     
                                        contenido.add(cs++, "       AND ax,dx");               
                                        contenido.add(cs++, "       cmp ax,0");           
                                        contenido.add(cs++, "       je "+ bloques.peek()+ ";í");  
                                    break;
                                    default:    
                                        contenido.add(cs++, "       cmp ax,dx");
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
                        
                        else if(line.equals("mientras")){                                                         
                            line = entrada.nextLine(); 
                            System.out.println(line); 
                            
                            index = indexOp(line);
                            operando = line.charAt(index);
                            aux = line.substring(0, index);                                                                                    
                            
                            contenido.add(cs++, "   ;mientras");           
                            bloques.push("in"+num);                            
                            contenido.add(cs++, "       "+ bloques.peek()+":"); 
                            
                            aritmetica(aux);
                            //System.out.println("artimetica1 "+aux);
                            //asigna al registro SI el valor gaurdado en ax(ensamblador) por aritmetica()
                            contenido.add(cs++, "       mov si, ax");  
                            
                            aux = line.substring(index+1, line.length() - 4);
                            aritmetica(aux);                            
                            //asigna al registro BP el valor guardado en ax(ensamblador) por aritmetica()                            
                            contenido.add(cs++, "       mov bp, ax"); 
                            
                            
                            contenido.add(cs++, "       cmp si,bp");           
                                                        
                            switch(operando){                                            
                                case '>':                                              
                                    if(line.charAt(index+1)== '='){ //es numero constante                                         
                                        bloques.push("sa"+num);
                                        contenido.add(cs++, "       jl "+ bloques.peek()+ ";>=");    
                                                                                
                                    }else{                                                                                                                                       
                                        bloques.push("sa"+num);
                                        contenido.add(cs++, "       jle "+ bloques.peek()+ ";>");                                           
                                    }
                                    break;                                                
                                case '<':                                        
                                    if(line.charAt(index+1)== '='){ //es numero constante                                                                                        
                                        bloques.push("sa"+num);
                                       contenido.add(cs++, "       jg "+ bloques.peek()+ ";<=");                                         
                                    }else{                                        
                                        bloques.push("sa"+num);
                                        contenido.add(cs++, "       jge "+ bloques.peek()+ ";<");                                          
                                    }
                                    break;
                                case '/':                                                                            
                                    bloques.push("sa"+num);
                                    contenido.add(cs++, "       je "+ bloques.peek()+ ";=/");                                      
                                    break;
                                default:                                                 
                                    bloques.push("sa"+num);
                                    contenido.add(cs++, "       jne "+ bloques.peek()+ ";=");                                          
                            }                                                                                                                                                                                                                                    
                        }
                        
                        else if(line.equals("fin_mientras")){
                            line = entrada.nextLine();                            
                            System.out.println(line); 
                            /*
                             *Se deben voltear lo bloques en el mientras para que queden de la fomra
                               in:
                               cmp..
                               jnc sa                               
                               ---
                               jmp in                               
                               sa:                            
                            */
                            aux = bloques.pop();                            
                            contenido.add(cs++, "       jmp "+ bloques.pop()+ ";=/");                              
                            contenido.add(cs++, "       "+aux+":"); 
                        }
                        
                        else if(line.equals("desde")){     
                            contenido.add(cs++, "       push cx");  
                            line = entrada.nextLine(); 
                            System.out.println(line);                             
                            
                            index = line.indexOf(',');
                            aux = line.substring(0, index).strip(); //Variable contador
                            fo1 = line.indexOf("hasta");                            
                            esc = line.substring(index+1, fo1).strip(); // Valor inicial del contador
                            //System.out.println("aux: "+aux+"\nesc: "+esc);
                            
                            //Leer base de la variable contador
                            contenido.add(cs++, "       Lea bx,"+aux);                            
                                                         
                            //Leer valor inicial
                            if(esc.charAt(0)<65){ //no tiene letras es constante
                                contenido.add(cs++, "       mov dx,"+esc);
                            }else{                             
                                contenido.add(cs++, "       mov ax,bx");     
                                contenido.add(cs++, "       Lea bx,"+esc);
                                contenido.add(cs++, "       mov dx,[bx]");
                                contenido.add(cs++, "       mov bx,ax"); 
                            }
                            /*En este punto dx tiene el valor incial y bx la 
                            direccion base de la variable contador
                            */
                            
                            //Asignamos a la variable contador el valor inicial
                            contenido.add(cs++, "       mov [bx],dx");                                
                            
                            //Valor final del ciclo                                            
                            esc = line.substring(fo1+5, line.length()-4).strip();
                            //System.out.println("fin:"+esc);
                            
                            if(esc.charAt(0)<65){ //no tiene letras es constante
                                contenido.add(cs++, "       mov cx,"+esc);                                
                            }else{                
                                contenido.add(cs++, "       mov ax,bx");                                                                
                                contenido.add(cs++, "       Lea bx,"+esc);                                                                
                                contenido.add(cs++, "       mov cx,[bx]");                                                                                                                                
                                contenido.add(cs++, "       mov bx,ax");                                                                
                            } 
                            //cx tiene el vlaor final del ciclo
                            
                            bloques.push("in"+num);                            
                            contenido.add(cs++, "       "+bloques.peek()+":");  
                            contenido.add(cs++, "       push cx");
                            contenido.add(cs++, "       push bx");
                        }
                        
                        else if(line.equals("fin_desde")){   
                            line = entrada.nextLine();                            
                            System.out.println(line);                                                                                                               
                                                                                                                
                            contenido.add(cs++, "       pop bx"); 
                            contenido.add(cs++, "       pop cx"); 
                            contenido.add(cs++, "       inc word ptr [bx]");                             
                            contenido.add(cs++, "       cmp [bx],cx");                             
                            contenido.add(cs++, "       jl "+ bloques.pop()+ ";<");                                                                 
                            contenido.add(cs++, "       pop cx");  
                                                                                                              
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
    
    /**
     * Recibe un linea y regresa el index donde hay un operando de comparacion.
     * @param line
     * @return index 
     */
    public int indexOp(String cad){            
        int i, end;
        if(cad.charAt(0)=='('){
            i=1;
            end=cad.length()-1;
        }else{
            i=0;
            end=cad.length();
        }            
        boolean flag = false, par = false;
        for(i = i; i < end; i++){
            if(par == false){
                if(cad.charAt(i) == '+' || cad.charAt(i) == '-' || cad.charAt(i) == '/' || cad.charAt(i) == '*' 
                              || cad.charAt(i) == '<' || cad.charAt(i) == '>' || cad.charAt(i) == '='
                              || cad.charAt(i) == 'ó' || cad.charAt(i) == 'í'){
                                    
                    return i;
                }
                else if(cad.charAt(i) == '('){
                    par = true;
                }
            } else{
                if(cad.charAt(i) == ')'){
                    par = false;
                }
            }
       }
       return -1;//no se encontro el indice
    }     
    
    /**
     * Lee una expresión aritmetica y pone el resultado en el registro ax de
     * ensamblador.
     * @param expresion 
     */
    public boolean aritmetica(String expresion){
        int fo1, fo2, index = 0;
        String aux;
        char operando;
        boolean bloque = false;
        int bloq1, bloq2 = 0;

        System.out.println(">>"+expresion+"<");
                       
        operando = '+';
        
//        index++;
        if(expresion.equals("")){                 
            return false;
        }
        
        //Se deja ax(ensamblador) en 0 para ir agregando lo que se desea asignar     
        contenido.add(cs++, "       mov ax, 0"); 
        fo1 = index;
        if(expresion.charAt(0)=='('){
            expresion=expresion.substring(1, expresion.length()-2);
        }        

        while(fo1<expresion.length()-1){                     
            bloq1=expresion.indexOf('(',index);          
                
            if(bloq1 != -1){ //SI Hay un bloque a continuacion           
                bloq2=expresion.lastIndexOf(')');                                          
            }else{
                //Si no hau un bloque bloq1 tiene un valor maximo
                bloq1=expresion.length()+2; 
            }
            
            //La posición del operando
            fo1=indexOp(expresion.substring(index));                 
            fo1=fo1+index;    
            
            //fo1 tiene la primera ocurrencia de un operando
            //Si un bloque va primero que el operando buscamos el operando despues del bloque
            if(fo1>bloq1){                   
                bloque = true;
                contenido.add(cs++, "       push ax");                 
                aux=(expresion.substring(bloq1+1, bloq2));                      
                aritmetica(aux);           
                contenido.add(cs++, "       mov dx,ax"); 
                contenido.add(cs++, "       pop ax"); 
                fo1=indexOp(expresion.substring(bloq2+1));
                fo1=fo1+bloq2+1;                
            }else{
                //Se asigna a aux la variable antes del operando
                if(index>fo1)   {        
                    aux = expresion.substring(index).strip();    
                    fo1=expresion.length();
                }else
                    aux = expresion.substring(index,fo1).strip();                  
            }       
        
            //Se hace la operación necesaria con la variable y el operando anterior
            switch(operando){                                            
                case '-':
                    contenido.add(cs++, "       \n;resta");                         
                    if(bloque){
                        contenido.add(cs++, "       sub ax, dx"); 
                        contenido.add(cs++, "       jo exrang");
                        bloque= false;
                    }else if(aux.charAt(0)<58){ //es numero constante
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
                    if(bloque){
                        contenido.add(cs++, "       imul dx"); 
                        contenido.add(cs++, "       jo exrang");
                        bloque= false;
                    }else if(aux.charAt(0)<58){ //es numero constante                                                
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
                    if(bloque){
                        contenido.add(cs++, "       idiv dx"); 
                        contenido.add(cs++, "       jo exrang");
                        bloque= false;                    
                    }else if(aux.charAt(0)<58){ //es numero constante     
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
                        if(bloque){
                            contenido.add(cs++, "       add dx"); 
                            contenido.add(cs++, "       jo exrang");
                            bloque= false;                        
                        }else if(aux.charAt(0)<58 ){ //es numero constante                                                
                            contenido.add(cs++, "       add ax, "+ aux); 
                            contenido.add(cs++, "       jo exrang");
                        }else{
                            contenido.add(cs++, "       Lea bx, "+ aux);
                            if(aux.equals("c")){
                                //System.out.println("¿que pado?");
                            }
                            contenido.add(cs++, "       add ax, [bx]");  
                            contenido.add(cs++, "      jo exrang");
                        }
                    }                                                                                            
            }
            
            //El operando encontrado es guardado para la siguiente ronda
            if(fo1<expresion.length()-1)
                operando = expresion.charAt(fo1);        
            index = fo1+1;  
        }
        return true;
    }
   
    /**
     * Guarda en el registro DX el resultado 1 o 0 
     * de una operacion logica.
     * @param bloque String con el contenido de los parentesis
     */
    private int bloqueLogico(String bloque, String etiq, int i) {        
        int index; 
        char operando;
        String pv,sv;
        //Se determina el index donde está el operando
        index = indexOp(bloque);                          
        
        operando = bloque.charAt(index);                                                      
        pv = bloque.substring(0, index).strip(); //Primer variable                                
       
        if(bloque.charAt(index+1)== '='){
            sv = bloque.substring(index+2).strip();
        }else{
            sv = bloque.substring(index+1).strip();
        }
        //Guardamos ax que tiene cosas importantes y para que no sepierd en el procedimeitno
        contenido.add(cs++, "       push ax");
        //Primera parte de la decision
        if(pv.trim().charAt(0)=='('){ //Es un bloque            
            index = pv.lastIndexOf(')');
            etiq=etiq+i;
            i = bloqueLogico(pv.substring(1, index), etiq, i) + 1;  
            contenido.add(cs++, "       mov ax,dx");            
        }else if(pv.charAt(0)<65){ //no tiene letras es constante
            contenido.add(cs++, "       mov ax,"+pv);                    
        }else{
            contenido.add(cs++, "       Lea bx,"+pv);
            contenido.add(cs++, "       mov ax,[bx]");
        }
                                
        //segunda parte de la decision
        if(sv.trim().charAt(0)=='('){ //Es un bloque
            System.out.println("SEGUNDA ES UN BLOQUE "+i);
            index = sv.lastIndexOf(')');                    
            etiq=etiq+i;
            i = bloqueLogico(pv.substring(1, index), etiq, i) + 1;        
        }else if(sv.charAt(0)<65){ //no tiene letras es constante
            contenido.add(cs++, "       mov dx,"+sv);                                                        
        }else{                                    
            contenido.add(cs++, "       Lea bx,"+sv);
            contenido.add(cs++, "       mov dx,[bx]");
            
        }
                              
        //Guardamos en dx el resultado 1 o 0 de la operacion
        //Considera los saltos con signo de ensamblador
        contenido.add(cs++, "       mov dx,0");   
        bloques.push(etiq+num+i);
        switch(operando){                                            
            case '>':           
                contenido.add(cs++, "       cmp ax,dx");
                if(bloque.charAt(index+1)== '='){ //es numero constante
                    contenido.add(cs++, "       jl "+ bloques.peek()+ ";>=");     
                }else{                                                                                                
                    contenido.add(cs++, "       jle "+ bloques.peek()+ ";>");     
                }
            break;                                                
            case '<':      
                contenido.add(cs++, "       cmp ax,dx");
                if(bloque.charAt(index+1)== '='){ //es numero constante                                                
                    contenido.add(cs++, "       jg "+ bloques.peek()+ ";<=");  
                }else{
                    contenido.add(cs++, "       jge "+ bloques.peek()+ ";<");  
                }
            break;
            case '/':      
                contenido.add(cs++, "       cmp ax,dx");
                contenido.add(cs++, "       je "+ bloques.peek()+ ";=/");  
            break;
            case 'ó':                   
                contenido.add(cs++, "       OR ax,dx");               
                contenido.add(cs++, "       cmp ax,0");                
                contenido.add(cs++, "       je "+ bloques.peek()+ ";ó");  
            break;
            case 'í':     
                contenido.add(cs++, "       AND ax,dx");               
                contenido.add(cs++, "       cmp ax,0");           
                contenido.add(cs++, "       je "+ bloques.peek()+ ";í");  
            break;
            default:  
                contenido.add(cs++, "       cmp ax,dx");
                contenido.add(cs++, "       jne "+ bloques.peek()+ ";=");                                                                                           
         }      
        contenido.add(cs++, "       dec dx");  //Hacer dx 1
        contenido.add(cs++, "       "+bloques.pop()+":");         
                
        //Sacamos las cosas importantes de ax
        contenido.add(cs++, "       pop ax");
        
        return i;
    }
}

