/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version 5.0.5
 * @author martiz
 * @author cris
 */
public class TablaDeTokens {
    private ArrayList<String> reservadas;
    private ArrayList tokenizar;
    private ArrayList<String> token,lexema, tipo;
    //private ArrayList<Integer> linea;
        
    public TablaDeTokens (ArrayList tokenizar){        
        //String lexema,token,tipo;

        reservadas = new ArrayList();
        agregarReservadas();
        
        this.tokenizar = tokenizar;
        this.token = new ArrayList();                
        this.lexema = new ArrayList();
        this.tipo = new ArrayList();
        
        ArrayList conte = getTokenizar();
        ArrayList reserv = getReservadas();
        String[] comp = {"<",">","<=",">=","=","/="};
        String[] arit = {"*","+","-","/","(",")"};
        String act = "";
        String tip = "";//ultimo tipo de variable registrado
        boolean esHacer = false, coma = false, esComp = false;
        for(int i = 0; i < conte.size();i++){
            act = conte.get(i).toString();
            if(act.charAt(0) >= 48 && act.charAt(0) <= 57){ //Si empieza con un numero no es ni reservada ni variable
                boolean tn = true;
                if(esNLinea(act)){
                    agregarToken(act, "No. Linea", "-"); 
                }else{
                    for(int j = 1; j < act.length();j++){
                        if(!(act.charAt(j) >= 48 && act.charAt(j) <= 57)){  
                            tn = false;
                        }
                    }
                    if(tn == false){
                        agregarToken(act, "Desconocido", "-");
                    }
                    else{
                        //validacion de hacer
                        if (coma == true){
                            if(tip.equals("cadena")){
                                agregarToken(act, "tipo.Dif", "entero");
                                coma = false;
                                continue;
                            }
                            coma = false;
                        }
                        //fin validacion de hacer
                        agregarToken(act, "numero", "entero");
                    }
                }
            }
            else{
                if(act.charAt(0) == 35){
                    if(act.charAt(act.length()-1) == 35){
                        //validacion de hacer
                        if (coma == true){
                            if(tip.equals("entero")){
                                agregarToken(act.substring(1, act.length()-1), "tipo.Dif", "cadena");
                                coma = false;
                                continue;
                            }
                            coma = false;
                        }
                        //fin validacion de hacer
                        agregarToken(act.substring(1, act.length()-1), "literal", "cadena");
                    }
                    else{
                        agregarToken(act, "Desconocido", "-");
                    }
                }
                else{
                    if(act.charAt(0) == '!'){
                        
                        if(esResevada(getLexema(i-1).toLowerCase())){
                            agregarToken(act, "error", "-");
                        }else{
                            if(coma == true){
                                agregarToken("", "valor NULO", "nulo");
                                coma = false;
                            }
                            agregarToken(act, "fin_de_linea", "-");
                        }
                    }
                    else{
                        if(act.equals(",")){
                            agregarToken(act, "Op.Asign", "-");
                            coma = true;
                        }
                        else{
                            boolean tom = false;
                            /*for(int x = 0; x <reserv.size(); x++){
                                if(act.toLowerCase().equals(reserv.get(x).toString())){
                                    tdt.agregarToken(act, reserv.get(x).toString(), "-");
                                    tom = true;
                                }
                            }*/
                            if(esResevada(act.toLowerCase())){
                                    if(act.equals("hacer")){
                                        esHacer = true;
                                        agregarToken(act, act, "asignar valor");
                                    }else if(act.equals("Mientras")){
                                        agregarToken(act, act, "ciclo");
                                        esComp = true;
                                    }else if(act.equals("Si") || act.equals("si")){
                                        agregarToken(act, act, "condicion");
                                        esComp = true;
                                    }else if(act.equals("o_si")){
                                        agregarToken(act, act, "condicion");
                                        esComp = true;
                                    }else if(act.equals("tons")){
                                        /*if(esComp == true){
                                            String pt = tipo.get(i-1).toString();//anterior tipo
                                            String cd = tipo.get(i-2).toString();//ante anterior tipo
                                            String pc = tipo.get(i-3).toString();//primer tipo
                                            //System.out.println(pc+" "+cd+" "+pt);
                                            if(pt.equals("ciclo") || cd.equals("ciclo") || pc.equals("ciclo"))
                                                agregarToken(act, "ErrorCond", "-");
                                            else{
                                                if(pc.equals(pt)){
                                                    if(cd.equals("comp")){
                                                        agregarToken(act, act, "cond.Cumplida");
                                                    }else{
                                                        agregarToken(act, "ErrorCond", "-");
                                                    }
                                                } else {
                                                    agregarToken(act, "ErrorDifValCond", "-");
                                                }
                                            }
                                            esComp = false;
                                        }else{
                                            agregarToken(act, "ErrorSM", "-");
                                        }*/
                                        esComp = false;
                                        agregarToken(act, act, "-");
                                    }else{
                                        agregarToken(act, act, "-");
                                    }
                                    tom = true;
                                    
                            }
                            for(int x = 0; x <comp.length; x++){
                                if(act.equals(comp[x])){
                                    agregarToken(act, "Op.comparacion", "comp");
                                    tom = true;
                                }
                            }
                            for(int x = 0; x <arit.length; x++){
                                if(act.equals(arit[x])){
                                    agregarToken(act, "op.aritmetica", "-");
                                    tom = true;
                                }
                            }
                            if(tom == false){
                                int aux;
                                String anterior;
                                aux = existeID(act); 
                                if(i>0)
                                    anterior = (getToken(i-1)).toLowerCase();
                                else
                                    anterior = "";
                                //if(aux != -1){                                                                   
                                    if(anterior.equals("procedimiento") || anterior.equals("cadena")
                                            ||anterior.equals("caracter")||anterior.equals("entero")){
                                        if(aux != -1) //Existe
                                            agregarToken(act, "ID ya declarado", anterior);
                                        else{
                                            //validacion de hacer
                                            if (coma == true){
                                                if(tip.equals("cadena")){
                                                    agregarToken(act, "tipo.Dif", "entero");
                                                    coma = false;
                                                    continue;
                                                }
                                                coma = false;
                                            }
                                            //fin validacion de hacer
                                            agregarToken(act, "id", anterior);
                                            //ultimo tipo de variable registrado
                                            tip = tipo.get(i).toString();
                                            //fin tipo
                                            esHacer = false;
                                        }
                                    }else{
                                        if(aux != -1){ //Existe
                                            if(anterior.equals("leer")){
                                                anterior = getTipo(aux);
                                                if(!anterior.equals("cadena")){
                                                    agregarToken(act, "error", "-");   
                                                    continue;
                                                }
                                            }
                                            //validacion de hacer
                                            if (coma == true){
                                                if(!tip.equals(getTipo(aux))){
                                                    agregarToken(act, "tipo.Dif", getTipo(aux));
                                                    coma = false;
                                                    continue;
                                                }
                                                coma = false;
                                            }
                                            //fin validacion de hacer
                                            agregarToken(act, "id", getTipo(aux));  
                                            //validacion de hacer
                                            //ultimo tipo de variable registrado
                                            tip = tipo.get(i).toString();
                                            //fin tipo
                                            esHacer = false;
                                        }else
                                            agregarToken(act, "Desconocido", "-"); 
                                    } 
                                /*}else 
                                    if(anterior.equals("procedimiento") || anterior.equals("cadena")
                                            ||anterior.equals("caracter")||anterior.equals("entero")){
                                        //agregarToken(act, "id", anterior);
                                    }else{
                                        agregarToken(act, "Desconocido", "-");                                        
                                    }      */                             
                            }
                        }
                    }
                }
            }
        }    
    }
    
    public boolean reportarTokensDesconocidos(){
        String token, main = "", end = ""; 
        int linea = 1;  
        boolean error = false;
        
        if (size() >= 4){
            main = getLexema(1) + getLexema(2) + getLexema(3);
            end = getLexema(size()-1);
        }
        if(!main.equals("Procedimientoprincipaliniciar")){
            System.out.println("No se encontro el procedimiento principal");
            return true;
        }
        
        for(int i = 0; i< size(); i++){
            token = getToken(i);
            if(token.equals("No. Linea"))                
                linea = Integer.parseInt(getLexema(i).substring(0, getLexema(i).length()-1));
            else if(token.equals("Desconocido")){
                System.out.printf("Simbolo \"%s\" desconocido en linea %d\n",getLexema(i), linea);
                error = true;
            }else if(token.equals("ID ya declarado")){
                System.out.printf("Variable \"%s\" ya declarada\n",getLexema(i));
                error = true;
            }else if(token.equals("error")){
                System.out.printf("Declaración invalida en linea %d\n", linea);                
                error = true;
            }else if(token.equals("tipo.Dif")){
                System.out.printf("Asignacion a variable otro tipo de dato distinto, %d\n", linea);                
                error = true;
            }else if(token.equals("valor NULO")){
                System.out.printf("No se le asigna ningun valor a esta variable, %d\n", linea);                
                error = true;
            }else if(token.equals("ErrorCond")){
                System.out.printf("Condicion invalida o inexistente, %d\n", linea);                
                error = true;
            }else if(token.equals("ErrorDifValCond")){
                System.out.printf("Simbolo no valido o comparacion de tipos distintos, %d\n", linea);                
                error = true;
            }else if(token.equals("ErrorSM")){
                System.out.printf("No se encontro un Si o un Mientras que maneje la condicion, %d\n", linea);                
                error = true;
            }
        }
        if(!end.toLowerCase().equals("fin_proc")){
            System.out.println("No se encontro el fin del procedimiento principal");
            error = true;
        }
        return error;
    }
    
    public boolean esNLinea(String va){ 
        Pattern pat = Pattern.compile("[0-9]+[:]");
        Matcher mat = pat.matcher(va);              
        if(mat.find())
            return true;
        else
            return false;     
        
    }
    
    /**
     * Yomi yomi 
     */
    private void agregarReservadas(){
        reservadas.add("procedimiento");
        reservadas.add("iniciar");
        reservadas.add("cadena");
        reservadas.add("caracter");
        reservadas.add("entero");
        reservadas.add("si");
        reservadas.add("o_si");
        reservadas.add("fin_si");        
        reservadas.add("hacer");
        reservadas.add("escribir");
        reservadas.add("mientras");        
        reservadas.add("desde");
        reservadas.add("hasta");
        reservadas.add("tons");
        reservadas.add("fin_mientras");
        reservadas.add("fin_proc");
        reservadas.add("fin_desde");
        reservadas.add("fin");
        reservadas.add("leer");
        reservadas.add("leern");
    }
    
    public void agregarToken(String lexema, String token, String tipo/*, int linea*/){
        this.lexema.add(lexema);
        this.token.add(token);
        this.tipo.add(tipo);
        //this.linea.add(linea);//Esto por ahora no
    }     
    
    /**
     * Regresa el indice de la priemra ocurrencia
     * @param id
     * @return 
     */
    public int existeID(String id){
        int i = lexema.indexOf(id);;
        if(i > -1)
            if(token.get(i).equals("id")) //Busca que sea ID        
                return i;                
        return -1;
    }
    
    public int size(){
        return token.size();
    }

    public ArrayList<String> getReservadas() {
        return reservadas;
    }
    
    public boolean esResevada(String lexema){
        if(reservadas.contains(lexema))
            return true;                
        return false;
    }

    public ArrayList<String> getTokenizar() {
        return tokenizar;
    }

    public ArrayList<String> getToken() {
        return token;
    }
    
    public String getToken(int i) {
        return token.get(i);
    }

    public ArrayList<String> getLexema() {
        return lexema;
    }
    
    public String getLexema(int i) {
        return lexema.get(i);
    }

    public ArrayList<String> getTipo() {
        return tipo;
    }
    
    public String getTipo(int i) {
        return tipo.get(i);        
    }
    
}