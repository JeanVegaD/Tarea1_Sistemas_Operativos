/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package t1so;

import static java.lang.Math.abs;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class backend {
    
    //variables de clase
    private Path filePath;
    private ArrayList<String> instructionsFromFile = new ArrayList<String>();//lista de instrucciones obtenidas del archivo

    private int initialPC = 0;
    
    
    private ArrayList<String> mainmemory = new ArrayList<String>(100);
    /* Memoria reservada en el arreglo
        0 - PC
        1 - AC
        2 - IR
        3 - AX
        4 - BX
        5 - CX
        6 - DX
    */
    
    public backend(){

    }
    
    //retorna la memoria principal
    public ArrayList<String> getMainMemory(){
        return  mainmemory;
    }
    
    //retorna el tamano de las instrucciones
    public int getSizeASM(){
        return instructionsFromFile.size();
    }
    
    //retorna una lista con las instrucciones en formato asm
    public ArrayList<String> getASM(){
        return instructionsFromFile;
    }
    
    //Inicializa nuevamente la memoria
    public void cleanMemory(){
        for (int i = 0; i < 100; i++) {
            mainmemory.add("0");
        }
    }
    
    //obtiene la ruta del archivo y la carga en el la variable
    public void setFilePath(String p_filePath){
        filePath= Paths.get(p_filePath);
    }
    
    //crea un array con las intrucciones obtenidas en del archivo
    public void readFile(){
        try {
             instructionsFromFile= (ArrayList<String>) Files.readAllLines(filePath, StandardCharsets.UTF_8);//lectura del archivo
        } catch (Exception ex) {
            instructionsFromFile = null;
        }
    }
    
    //Identificar si el string se puede parsear en un numero
    public boolean tryParseInt(String value) {  
     try {  
         Integer.parseInt(value);  
         return true;  
      } catch (NumberFormatException e) {  
         return false;  
      }  
}
    //lee y valida el archivo
    public boolean validateFile(){
         boolean res= true;
         int pcTemp=  Integer.parseInt(mainmemory.get(0)) ;
         
         for (String instruction :instructionsFromFile ){
            String line = "";
            String[] subinst = instruction.split("\\s+|,");
    
            if(subinst.length>1 && subinst.length<4){
                String acction= replace(subinst[0]);
                String memory= replace(subinst[1]);
                if(!acction.equals("unknown") && !memory.equals("unknown")){
                    line+= acction;
                    line+= memory;
                    if(subinst.length==3){
                        if (tryParseInt(subinst[2])) {  
                            int numero = Integer.parseInt(subinst[2]);  // We now know that it's safe to parse
                            line+=converValuetoBinary(numero);
                            mainmemory.add(pcTemp, line);
                            pcTemp+=1;
                        }
                        else{
                            res= false;
                        }
                    }else{
                        line+= "00000000";
                        mainmemory.add(pcTemp, line);
                        pcTemp+=1;
                    }
                    
                }else{
                    res= false;
                }
            }else{
                res= false;
            }        
        }
        return res;
    }
 
    //inicializa la memoria
    public void initializeMemory(){
        initialPC = calculatePC();
        mainmemory.set(0,Integer.toString(initialPC));
        mainmemory.set(2, Integer.toString(initialPC+1));
    }
    
 
    public int calculatePC(){
        int sizeOfInstrucctions = instructionsFromFile.size();
        int init = 50- sizeOfInstrucctions;
        int resultPc = new Random().nextInt(init);
        return 50+resultPc;
    }
    
    //convierte el asm en una linea binaria 
    public boolean convertFileTobinary(){
        for (String instruction :instructionsFromFile ){
            String line = "";
            String[] subinst = instruction.split("\\s+|,");
            
            line+= replace(subinst[0]);
            line+= replace(subinst[1]);
        }
        return true;
    }
    
    //remplaza el strign de entrada por el numero en binario correspondiente 
    public String replace (String p_replace){
        String res = "";
        switch(p_replace.toLowerCase()){
            case "load":
                res = "001" ;
                break;
           case "store":
                res = "010" ;
                break;
           case "mov":
                res = "011" ;
                break;
           case "sub":
                res = "100" ;
                break;
           case "add":
                res = "101" ;
                break;
            case "ax":
                res = "0001" ;
                break;
            case "bx":
                res = "0010" ;
                break;
            case "cx":
                res = "0011" ;
                break;
            case "dx":
                res = "0100" ;
                break;
            default:
                res = "unknown" ;
                break;
        }
        return res;
    }
    
    //convierte el numero entero en binario con signo
    private String converValuetoBinary(int number){
        String value = "";
        String sign = "0";
         if(number>0){
            value = Integer.toBinaryString(number);
         }
        else{
            sign = "1";//en caso de que el valor sea negativo se le asigna un 1 al primer bit
            value = Integer.toBinaryString(abs(number));
        }
        int leftNumbers = 7 - value.length();
        //rellenar de 0 para completar los bits
        for(int i=0 ; i < leftNumbers; i++){
            value = "0"+value;
        }
        value = sign + value;
        return value;
    }
    
    //ejecuta la primera instruccion del archivo 
    public void executeFirstInstruction(String instruction){
        String acction = instruction.substring(0, 3);
        String register = instruction.substring(3, 7);
        String number = instruction.substring(7, 15);;
        
        executeInstruction_aux(acction,register,number); 
    }
    
    //ejecuta la siguiente instruccion cuando es llamada con el boton de next 
    public void executeInstruction(String instruction){
        String acction = instruction.substring(0, 3);
        String register = instruction.substring(3, 7);
        String number = instruction.substring(7, 15);;
        
        executeInstruction_aux(acction,register,number);
        
        //suma al pc
        mainmemory.set(0, Integer.toString(Integer.parseInt(mainmemory.get(0))+1));
        if(Integer.parseInt(mainmemory.get(0))+1 != initialPC+instructionsFromFile.size()){
            mainmemory.set(2, Integer.toString(Integer.parseInt(mainmemory.get(2))+1));
        }
        else{
             mainmemory.set(2, "NULL");
        }   
    }
    
    
    //determina la accion que debe reaizarza para guardar su valor en memoria
    public void executeInstruction_aux(String action, String register, String number){
        switch(action){
            case "001": //load
                if(number.equals("00000000")){
                    mainmemory.set(1,mainmemory.get(getIndexByMemory(register)));  
                }else{
                    mainmemory.set(getIndexByMemory(register),convertIntSttoBinarySt(number));
                }
                
                break;
           case "010"://store
                if(number.equals("00000000")){
                    mainmemory.set(getIndexByMemory(register),mainmemory.get(1));  
                }else{
                    mainmemory.set(getIndexByMemory(register),convertIntSttoBinarySt(number));
                }
                break;
           case "011"://mov
                if(number.equals("00000000")){
                    mainmemory.set(1,mainmemory.get(getIndexByMemory(register)));  
                }else{
                    mainmemory.set(getIndexByMemory(register),convertIntSttoBinarySt(number));
                }
                break;
           case "100"://sub
                if(number.equals("00000000")){
                    int n1 = Integer.parseInt(mainmemory.get(getIndexByMemory(register)));
                    int n2 = Integer.parseInt(mainmemory.get(1));
                    int res = n2-n1;
                    mainmemory.set(1,Integer.toString(res));
                }else{
                    int n1 = Integer.parseInt(mainmemory.get(getIndexByMemory(register)));
                    int n2 = Integer.parseInt(convertIntSttoBinarySt(number));
                    int res = n2-n1;
                    mainmemory.set(getIndexByMemory(register),Integer.toString(res));
                }
                break;
           case "101"://add
               if(number.equals("00000000")){
                    int n1 = Integer.parseInt(mainmemory.get(getIndexByMemory(register)));
                    int n2 = Integer.parseInt(mainmemory.get(1));
                    int res = n1+n2;
                    mainmemory.set(1,Integer.toString(res));
                }else{
                    int n1 = Integer.parseInt(mainmemory.get(getIndexByMemory(register)));
                    int n2 = Integer.parseInt(convertIntSttoBinarySt(number));
                    int res = n1+n2;
                    mainmemory.set(getIndexByMemory(register),Integer.toString(res));
                }
                break;    
        }
    }
    
    //convierte el string de un numero en binario con signo
    public String convertIntSttoBinarySt(String number){
        String signo = number.substring(0,1);
        String numero = number.substring(1,8);
        
        int intNumber = Integer.parseInt(numero,2);
        
        if(signo.equals("1")){
            return Integer.toString(intNumber*-1);
        }else{
            return Integer.toString(intNumber);
        }
    }
    
    //obiene el indice de la memoria segun su representacion en ASM
    public int getIndexByMemory(String memory){
        int res = 0;
        switch(memory){
            case "0001":
                res = 3;
                break;
            case "0010":
                res = 4;
                break;
            case "0011":
                res = 5;
                break;
            case "0100":
                res = 6 ;
                break;
        }
        return res;
    }
    
    
}
