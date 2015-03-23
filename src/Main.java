import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.*;
import java.util.Scanner;

// http://stackoverflow.com/questions/1657066/java-regular-expression-finding-comments-in-code
//String clean = original.replaceAll( "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/", "$1 " );

public class Main
{
    String targetFile;
    String inputString;
    String inputStringWOLComments;
    String inputStringWOBlockComments;
    String remainingInputString;
    String[] inputArray;
    String[][] inputFile;
    ArrayList<Token> tokens;
    Pattern myFloat;
    Pattern myNum;
    Pattern myKeyword;
    Pattern mySymbols;
    Pattern myID;
    Pattern myNextFloat;
    Pattern myNextNum;
    Pattern myNextKeyword;
    Pattern myNextSymbols;
    Pattern myNextID;
    Matcher myFloatMatcher;
    Matcher myNumMatcher;
    Matcher myKeywordMatcher;
    Matcher mySymbolMatcher;
    Matcher myIdMatcher;
    Matcher myNextFloatMatcher;
    Matcher myNextNumMatcher;
    Matcher myNextKeywordMatcher;
    Matcher myNextSymbolMatcher;
    Matcher myNextIdMatcher;
    int depth;
    int scope;
    int tokenCounter;
    int scopeDepth;
    ArrayList<HashMap> symbolTabel;

    class Token
    {
        private String myLexum;
        private String type;
        private int scopeDepth;

        Token(String inputToken)
        {
            myLexum=new String(inputToken);
        }

        public void setLexum(String input)
        {
            myLexum=input;
        }
        public String getLexum() {return myLexum;}

        public String getToken()
        { return myLexum;}

        public void setScope(int inputScope)
        {
            scopeDepth=inputScope;
        }
        public int getScope()
        {return scopeDepth;}

        public void setType(String newType)
        {
            type=newType;
        }

        public String getType()
        {
            return type;
        }

        public String toString()
        {
            String output = new String();
            output ="Token: \""+myLexum+"\"\n";
            output+="Type: "+type+" Scope: "+scopeDepth+"\n";
            return output;
        }
    }


    public static void main(String[] args) {
        Main myMain = new Main();
        System.out.print("Aaron Wagner COP 5625 P1 \n \n");
        myMain.targetFile=args[0];
        myMain.loadFile();
        myMain.makePatterns();
        myMain.depth=0;
        myMain.scope=0;
        myMain.tokens = new ArrayList<Token>();
        for (int i=0; i<myMain.inputArray.length; i++)
        {
            System.out.print("I="+i+":~"+myMain.inputArray[i]);
        }
        for (int i=0; i<myMain.inputArray.length; i++)
        {
            myMain.inputStringWOLComments="";
            myMain.inputStringWOLComments=myMain.removeLineComments(myMain.inputArray[i]);
            //myMain.inputStringWOLComments=myMain.removeLineComments(myMain.inputStringWOBlockComments);
            myMain.makeTokens(myMain.inputStringWOLComments);
            //System.out.print("MONITOR:END OF LINE\n");

        }
        myMain.tokenCounter=0;
        myMain.makeEndToken();
        myMain.program();
        /*Token endToken=new Token("$")
        myMain.tokens.add();
        */
    }
    public void makeEndToken()
    {
        Token endtoken= new Token("$");
        endtoken.setType("end");
        tokens.add(endtoken);
    }

    public void makePatterns()
    {
        

        //myFloat;

        myNum=myNum.compile("\\A\\d+");
        myKeyword=myKeyword.compile("\\A(else|if|int|return|void|while)&&(\\b|\\B)");
        mySymbols=mySymbols.compile("\\A\\+|\\A-|\\A\\*|\\A/|\\A>=|\\A<=|\\A==|\\A!=|\\A=|\\A;|\\A,|\\A\\(|\\A\\)|\\A\\[|\\A\\]|\\A\\{|\\A\\}|\\A<|\\A>");
        myFloat=myFloat.compile("\\A(\\d+(\\.\\d+)?(E(\\+|\\-)?\\d+)?)");
        //myFloat=myFloat.compile("\\A((([1-9]((([1-9]+\\.[0-9]*)|([1-9]*\\.[0-9]+))([E][-+]?[0-9]+)?)|(([1-9]+\\.[0-9]*)|([1-9]*\\.[0-9]+)|([1-9]+))([E][-+]?[0-9]++\\.[0-9]*)|([1-9]*\\.[0-9]+))([E][-+]?[0-9]+)?)|(([1-9]+\\.[0-9]*)|([1-9]*\\.[0-9]+)|([1-9]+))([E][-+]?[0-9]+)");
        myID=myID.compile("\\A[a-zA-Z]+"); //This is catching on spaces and empty strings:TODO FIX
        myNextNum=myNextNum.compile("\\A\\d+");
        myNextKeyword=myNextKeyword.compile("(else|if|int|return|void|while)&&(\\b|\\B)");
        myNextSymbols=myNextSymbols.compile("\\+|-|\\*|/|<=|>=|==|!=|=|;|,|\\(|\\)|\\[|\\]|\\{|\\}|<|>");
        myNextFloat=myNextFloat.compile("(\\d+(\\.\\d+)?(E(\\+|\\-)?\\d+)?)");
        myNextID=myNextID.compile("[a-zA-Z]");
        //myID.;
        int pants=0; //debug variable pants for breakpoint

    }

    public String removeLineComments(String input)
    {
       // System.out.print("Input:"+input+"\n");
        input+="  ";
        boolean foundline;
        boolean found;
        String output=new String();
        for (int i=0; i<input.length()-2; i++)
        {
            foundline=false;
            found=false;
            //System.out.println("**"+input.substring(i,i+1)+"** I="+i);
            if (input.substring(i,i+2).equals("//")&&depth<1)
            {
                //System.out.print("\n I FOUND A COMMENT!!!!!\n");
                for (int j=i+1; j<input.length()-1; j++)
                {
                    foundline=false;
                    if (input.substring(j,j+1).equals("\n"))
                    {
                        //System.out.print("\nI FOUND THE END OF A COMMENT!!!!!\n");
                        foundline=true;
                        i=j;
                        break;
                    }
                    else
                    {
                        continue;
                    }

                }

            }
            if (input.substring(i,i+2).equals("/*"))
            {
                //System.out.print("\n I FOUND A BLOCK COMMENT!!!!!\n");
                depth++;
                i=i+1;
                continue;
            }
            if (!foundline&&depth<1)
            {
                output += input.charAt(i);
            }
            if (input.substring(i,i+2).equals("*/")&&depth>0)
            {
                //System.out.print("\nI FOUND THE END OF A BLOCK COMMENT!!!!!\n");
                depth--;
                i=i+2;
                continue;
            }
        }
        //System.out.print("Output:~"+output+"~\n");
        return output;
    }
    public String removeBlockComments(String input)
    {
        input+="  ";
        boolean found;

        String output=new String();
        for (int i=0; i<input.length()-2; i++)
        {
            found=false;
            //System.out.println("--"+input.substring(i,i+2)+"-- I="+i);
            if (input.substring(i,i+2).equals("/*"))
            {
                //System.out.print("\n I FOUND A BLOCK COMMENT!!!!!\n");
                depth++;
                i=i+1;
                continue;
            }
            if (input.substring(i,i+2).equals("*/"))
            {
                //System.out.print("\nI FOUND THE END OF A BLOCK COMMENT!!!!!\n");
                depth--;
                i=i+2;
                continue;
            }
            if (depth<1)
            {
                output += input.charAt(i);
            }
        }
        return output;
    }

    public void loadFile() {
        //<tDone odo> fix the removal of linebreaks
        // This block of code converts the input filestream into an array of strings.
        try
        {
            FileInputStream inputStream = new FileInputStream(targetFile);
            DataInputStream dataInput = new DataInputStream(inputStream);
            BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(dataInput));
            String tempinputString;
            inputString="";

            ArrayList<String> inputList = new ArrayList<String>();

            while ((tempinputString = (inputBuffer.readLine())) != null)
            {
                //tempinputString = tempinputString.trim();
                if ((tempinputString.length()!=0))
                {
                    inputList.add(tempinputString+"\n");
                }
                inputString+=tempinputString+"\n";
            }
            inputArray = (String[]) inputList.toArray(new String[inputList.size()]);
            inputFile = new String[inputArray.length][];
            for (int i=0; i<inputArray.length; i++)
            {
                inputFile[i]=inputArray[i].split("(\\s+)"); // this needs to be "(\\s+) (not a float word or number)"
            }
            //inputString=new String (tempinputString);
            //int pants=0;
        }
        catch (Exception e)
        {
            // Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void makeTokens(String input)
    {

        Token nextToken;

        remainingInputString=input+"   ";
        while (remainingInputString.length()>0)
        {
            remainingInputString=remainingInputString.trim();
            if (remainingInputString.length()==0) break;
            //System.out.print("in make Tokens: Remaining length: "+remainingInputString.length()+"\n");

            nextToken=getNextToken(remainingInputString);
            nextToken.setScope(scope);
            //System.out.print(nextToken.toString()+"\n");

            if (nextToken.getLexum().equals("{"))
            {
                scope++;
                //System.out.print("INCREASING SCOPE");
            }
            if (nextToken.getLexum().equals("}"))
            {
                scope--;
                //System.out.print("DECREASING SCOPE");

            }
            if (nextToken.getClass().equals("id"))
            {
                //store the token in the symbol table
            }
            tokens.add(nextToken);
            // remainingInputString+="  ";
        }
        //System.out.print("Remaining length: "+remainingInputString.length()+"\n"+remainingInputString);
        /*

        Token endToken=new Token("$");
        System.out.print("Tokens \n ---------------------------------------------\n");
        for (int i=0; i<tokens.size(); i++)
        {
            System.out.print(tokens.get(i).toString()+"\nI:"+i+"\n");
        }
        tokens.add(endToken);
        */
    }


    public Token getNextToken(String input)
    {
        Token returnedToken;
        Boolean found=false;
        int start=input.length();
        int end=input.length();
        input=input.trim();
        // System.out.print("Input for getNextToken:\n"+ input);
        //check for keywords
        //System.out.print("\n+\n+\nINPUT\n\""+input+"\n\"\n+\n+\n");
        //System.out.print("\n Input in get next token: "+input+"\n");
        myKeywordMatcher= myKeyword.matcher(input.trim());
        myIdMatcher=myID.matcher(input.trim());
        myFloatMatcher=myFloat.matcher(input.trim());
        myNumMatcher=myNum.matcher(input.trim());
        mySymbolMatcher=mySymbols.matcher(input.trim());

        myNextNumMatcher=myNextNum.matcher(input.trim());
        myNextSymbolMatcher=myNextSymbols.matcher(input.trim());
        myNextKeywordMatcher=myNextKeyword.matcher(input.trim());
        myNextIdMatcher=myNextID.matcher(input.trim());
        myNextFloatMatcher=myNextFloat.matcher(input.trim());
        /*

        //myNum.compile("\\d+");
        //myKeyword.compile("else|if|int|return|void|while");
        //mySymbols.compile("\\+|-|\\*|/|<|<=|>|==|!=|=|;|,|\\(|\\)|\\[|\\]|\\{|\\}");
        //myFloat.compile("\\d+(\\.\\d+)?(E(\\+|\\-)?\\d+)?");
        //myID.compile("\\p{Alpha}*");
        myNextNum.compile("\\d+");
        myNextKeyword.compile("else|if|int|return|void|while");
        myNextSymbols.compile("\\+|-|\\*|/|<|<=|>|==|!=|=|;|,|\\(|\\)|\\[|\\]|\\{|\\}");
        myNextFloat.compile("\\d+(\\.\\d+)?(E(\\+|\\-)?\\d+)?");
        myNextID.compile("\\p{Alpha}*");
         */



        if (myKeywordMatcher.find())
        {
            returnedToken = new Token(myKeywordMatcher.group());
            returnedToken.setType("keyword");
            remainingInputString=input.substring(myKeywordMatcher.end(), input.length());
        }
        else if (myIdMatcher.find())
        {
            returnedToken = new Token(myIdMatcher.group());
            returnedToken.setType("id");
            //        myKeyword=myKeyword.compile("\\A(else|if|int|return|void|while)&&(\\b|\\B)");

            if (returnedToken.getLexum().equals("if")||returnedToken.getLexum().equals("else")||returnedToken.getLexum().equals("int")||returnedToken.getLexum().equals("return")||returnedToken.getLexum().equals("void")||returnedToken.getLexum().equals("while"))
            {
                returnedToken.setType("keyword");
            }

            remainingInputString=input.substring(myIdMatcher.end(), input.length());
        }
        else if (myFloatMatcher.find())
        {
            returnedToken = new Token(myFloatMatcher.group());
            returnedToken.setType("float");
            remainingInputString=input.substring(myFloatMatcher.end(), input.length());
        }
        else if (myNumMatcher.find())
        {
            returnedToken = new Token(myNumMatcher.group());
            returnedToken.setType("int");
            remainingInputString=input.substring(myNumMatcher.end(), input.length());
        }
        else if (mySymbolMatcher.find())
        {
            returnedToken= new Token(mySymbolMatcher.group());
            returnedToken.setType("Symbol");
            remainingInputString=input.substring(mySymbolMatcher.end(), input.length());
        }
        else
        {
            //REDUNDANT
            /*
            myNextKeywordMatcher= myNextKeyword.matcher(input.trim());
            myNextNumMatcher=myNextNum.matcher(input.trim());
            myNextSymbolMatcher=myNextSymbols.matcher(input.trim());
            myNextFloatMatcher=myNextFloat.matcher(input.trim());
            myNextIdMatcher=myNextID.matcher(input.trim());
            */

            if (myNextKeywordMatcher.find())
            {
                start=myNextKeywordMatcher.start();
                end=myNextKeywordMatcher.end();

            }
            if (myNextIdMatcher.find())
            {
                int myNextidStart=myNextIdMatcher.start();
                if (myNextidStart<start)
                {
                    start=myNextidStart;
                    end=myNextIdMatcher.end();
                }
            }
            if (myNextFloatMatcher.find())
            {
                int myNextFloatStart=myNextFloatMatcher.start();
                if (myNextFloatStart<start)
                {
                    start=myNextFloatStart;
                    end=myNextFloatMatcher.end();
                }
            }
            if (myNextNumMatcher.find())
            {
                int myNextNumStart=myNextNumMatcher.start();
                if (myNextNumStart<start)
                {
                    start=myNextNumStart;
                    end=myNextNumMatcher.end();
                }
            }
            if (myNextSymbolMatcher.find())
            {
                int myNextSymbolStart=myNextSymbolMatcher.start();
                if (myNextSymbolStart<start)
                {
                    start=myNextSymbolStart;
                    end=myNextSymbolMatcher.end();
                }
            }
            //System.out.print("ERROR: \n"+input.substring(0,start)+"~");
            //System.out.print("\nNEXT GOOD TOKEN:\n"+input.substring(start,end));
            returnedToken = new Token (input.substring(0,start));
            returnedToken.setType("error");
            remainingInputString=input.substring(start,input.length());
            //System.out.print("Remaining:"+remainingInputString+"\n");

            // in an error look for the next good
        }
        return  returnedToken;

    }


    /*
    Remove line comments from http://stackoverflow.com/questions/16394787/checking-for-a-not-null-not-blank-string-in-java
    */

  /*
    public static String removeCommentt(String code)
    {
        final int outsideComment=0;
        final int insideLineComment=1;
        final int insideblockComment=2;
        final int insideblockComment_noNewLineYet=3; // we want to have at least one new line in the result if the block is not inline.

        int currentState=outsideComment;
        String endResult="";
        Scanner s= new Scanner(code);
        s.useDelimiter("");
        while(s.hasNext()){
            String c=s.next();
            switch(currentState){
                case outsideComment:
                    if(c.equals("/") && s.hasNext()){
                        String c2=s.next();
                        if(c2.equals("/"))
                            currentState=insideLineComment;
                        else if(c2.equals("*")){
                            currentState=insideblockComment_noNewLineYet;
                        }
                        else
                            endResult+=c+c2;
                    }
                    else
                        endResult+=c;
                    break;
                case insideLineComment:
                    if(c.equals("\n")){
                        currentState=outsideComment;
                        endResult+="\n";
                    }
                    break;
                case insideblockComment_noNewLineYet:
                    if(c.equals("\n")){
                        endResult+="\n";
                        currentState=insideblockComment;
                    }
                case insideblockComment:
                    while(c.equals("*") && s.hasNext()){
                        String c2=s.next();
                        if(c2.equals("/")){
                            currentState=outsideComment;
                            break;
                        }

                    }

            }
        }
        s.close();
        return endResult;
    }
*/

    public boolean checkTokenForKeyWord(String input){
        return input.matches("\\B[else][if][int][return][void][while][float]\\B");
    }

    public boolean checkTokenForSpecialSymbol(String input) {

        return input.matches("^A[\\+][-][*][/][<][<=][>][==][!=][=][;][,][\\(][\\)][\\[][\\]][\\{][\\}]^Z");

    }

    public void checkTokenForID() {

    }

    public boolean checkTokenForNum(String input) {
        //may have to
        return input.matches("\\b[0-9]+\\b");
    }

    public void checkTokenForFloat() {

    }
    /*
    Note for recursive decent parser methods method  are annotated with a the symbols for the LL(1) compliant derived language

    */
    public Token getToken()
    {

        Token testToken;
        do {
            testToken = tokens.get(tokenCounter);
            tokenCounter++;
        }
        while (testToken.getType().equals("error"));
        return (testToken);
    }
    public boolean matchType(String input)
    {
         Token testToken=tokens.get(tokenCounter);
         if (testToken.getType().equals(input))
         {

             if (input.equals("$")) {
                 System.out.println("Accepted: " + tokens.get(tokenCounter).toString() + "\n");
             }
             else
             {
                 System.out.println("Accepted: "+tokens.get(tokenCounter).toString()+"\n");
             }
                 tokenCounter++;

             return true;
         }
        return false;

    }
    public boolean lookType(String input)
    {
        Token testToken=tokens.get(tokenCounter);
        if (testToken.getType().equals(input))
        {
            //tokenCounter++;
            return true;
        }
        return false;

    }
    public boolean match(String input)
    {
        Token testToken=tokens.get(tokenCounter);
        if (testToken.getLexum().equals(input))
        {
            System.out.println("Accepted: "+tokens.get(tokenCounter).toString()+"\n");
            tokenCounter++;
            return true;
        }
        return false;

    }

    public boolean look(String input)
    {
        Token testToken=tokens.get(tokenCounter);
        if (testToken.getLexum().equals(input))
        {
            //tokenCounter++;
            return true;
        }
        return false;

    }
    public boolean match(Collection<String> inputs)
    {
        Token testToken=tokens.get(tokenCounter);
        Boolean match=false;
        for (String inputStringType: inputs) {
            if (testToken.getLexum().equals(inputStringType)) {
                System.out.println("Accepted: "+tokens.get(tokenCounter).toString()+"\n");
                tokenCounter++;
                return true;
            }
        }
        return false;
    }
    public boolean look(Collection<String> inputs)
    {
        Token testToken=tokens.get(tokenCounter);
        Boolean match=false;
        for (String inputStringType: inputs) {
            if (testToken.getLexum().equals(inputStringType)) {
                //tokenCounter++;
                return true;
            }
        }
        return false;
    }

    public boolean matchType(Collection<String> inputs)
    {
        Token testToken=tokens.get(tokenCounter);
        Boolean match=false;
        for (String inputStringType: inputs) {
            if (testToken.getType().equals(inputStringType)) {
                System.out.println("Accepted: "+tokens.get(tokenCounter).toString()+"\n");
                match=true;
                tokenCounter++;
                return true;
            }
        }
        return false;

    }
    public boolean lookType(Collection<String> inputs)
    {
        Token testToken=tokens.get(tokenCounter);
        Boolean match=false;
        for (String inputStringType: inputs) {
            if (testToken.getType().equals(inputStringType)) {
                match=true;
                //tokenCounter++;
                return true;
            }
        }
        return false;

    }
    public void error(String expected)
    {
        System.out.println("Error "+expected+" expected. \n Have: \"" +
                tokens.get(tokenCounter).getLexum()+
                "\"\n Of type: "+tokens.get(tokenCounter).getType()+
                "\n on Token # "+tokenCounter+"\n");
        System.exit(-1);
    }
// This starts the recusive decent methods
    /////////////////////////////////////////////////////////////////////////////////
    public void program() //S->EaCA
    {
       // System.out.print("DECENDING \n***********************************************");
        //System.out.println("program\n TokenCounter:" + tokenCounter + "Token: " + tokens.get(tokenCounter).toString());
        scopeDepth=0;
        type_specifier();//E
        if (matchType("id")){}//a
        else
        {
            error("Type id");
        }
        stemmed_decleration();//C
        declaration_list();//A
        if (match("$"))
        {
            System.out.println("Accepted");
            System.exit(0);
        }

    }
    public void declaration_list() //A->BA | @
    {
        System.out.println("declaration list\n TokenCounter:"+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        //check for first of B
        if (look("int")||look("void")||look("float"))//First of B {d b n}
        {
            declaration();
            declaration_list();
        }
        else if (match("$"))
        {
            System.out.println("Accepted TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter-1).toString());
            System.exit(0);
        }
        else return;
    }

    public void declaration()  //B->EaC
    {
        System.out.println("declaration\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        type_specifier();//E
        if (matchType("id"))
            {

            }//a
        else
        {
            error("Type id");
        }
        stemmed_decleration();//C

    }

    public void stemmed_decleration()  //C->4 |(F)G
    {
        System.out.println("stemmed declaration\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (look(";")||look("["))
        {
            stemmed_vardecleration();//4
        }
        else if(match("("))//(F)G
        {
            parameters();//F
            if (match(")"))
            {
                compound_statement();
            }
            else {error(")");}
        }
        else {error("(");}

    }
    public void stemmed_vardecleration() //4 -> ; | [c]
    {
        System.out.println("stemmed var declaration\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match(";"))
        {
            return;
        }
        else if (match("["))
        {
            expression();

            if (match("]"))
            {
                if (match(";")){return;}
                else {error(";");}
            }
            else
            {
                error("]");
            }
        }
        else
        {
            error("[");
        }

    }
    public void fun_declaration()  //D->a50 | @
    {
        System.out.println("function declaration\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (matchType("id"))//a
        {
            parameter_list_prime();//5
            declaration_prime(); //0
        }
        else
        {
            //check follows of D
            if (look(")"))
            {
                return;
            }
            else
            {
                error (")");
            }
        }
    }
    public void declaration_prime() //0 -> ,I0|@
    {
        System.out.println("decleartion prime\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if(match(","))
        {
            parameter();   //I
            declaration_prime();
        }
        else if (look(")"))//look for follows of 0
        {return;}
        else{error(",");}
    }
    public void  type_specifier() //E-> d | b | n
    {
        System.out.println("type specifier\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match("int"))//d
        {
            return;
        }
        else if (match("void"))//b
            {return;}
        else if(match("float"))//n
            {return;}
        else {error("Type Specifier");}

    }
    public void parameters()  //F-> ED
    {
        System.out.println("parameter\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        type_specifier();
        fun_declaration();
    }
    public void compound_statement() //G-> {JK}
    {
        System.out.println("compound statement\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match("{"))
        {
            scopeDepth++;
            local_declaration();
            statement_list();
            if (match("}"))
            {
                scopeDepth--;
                return;
            }
            else
            {
                error("}");
            }
        }
        else
        {
            error("}");
        }
    }
    public void parameter_list() //H
    {
        //System.out.println("parameter list\n this method is empty and will need to be populated\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
    }
    public void parameter_list_prime() //5
    {
        System.out.println("parameter list prime\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match("["))
        {
            if (match("]"))
            {
                if (look(",")||look(")")) ///check follows of 5
                {
                    return;
                }
                else
                {
                    error ("}");
                }

            }
        } else if (look(",")||look(")")) ///check follows of 5
            {
                return;
            }
            else
            {
                error ("}");
            }

    }
    public void parameter() //I-> Ea5
    {
        System.out.println("parameter\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        type_specifier();
        if (matchType("id"))//a
        {//5
            parameter_list_prime();
        }
        else {error("parameter" );}
    }
    public void local_declaration() //J->BJ | @
    {
        System.out.println("local declaration\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (look("int")||look("float")||look("void"))
        {
            declaration();//C
            local_declaration();//J

        }
        else if (look("while")||look("if")||lookType("int")|| lookType("float")||lookType("num")||lookType("id")||look(";")||look("(")||look("{")||look("return")||look("}"))//followsb of J)
        {
            return;
        }
        else
        {
            error ("statment ");
        }
    }
    public void statement_list() //K->LK
    {
        System.out.println("statment list\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        //check first of statment
        if (look("while")||look("if")||lookType("num")||lookType("int")|| lookType("float")||lookType("id")||look(";")||look("(")||look("{")||look("return"))
        {
            statement();
            statement_list();
        }
        else if (look("}"))//check follows of K
        {
            return;
        }
        else {error("}");}
    }
    public void statement()  //L-> M | G | N | O | P
    {
        System.out.println("statement\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (lookType("num")||lookType("id")||look(";")||look("(")||lookType("int")|| lookType("float") )//first of M
        {
            expressions_statement();
        }
        else if (look("{"))//first of G:compound_statement
        {
            compound_statement();;
        }
        else if (look("if"))//first of N:selection_statment
        {
            selection_statement();
        }
        else if(look("while")) //first of O:iteration_statment
        {
            iteration_statement();
        }
        else if(look("return")) //first og P :return_statement
        {
            return_statement();
        }
        else{error ("statment ");}

    }
    public void expressions_statement()  //M->Q; | ;
    {
        System.out.println("expression statement\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (lookType("num")||lookType("id")||look("(")||lookType("int")|| lookType("float"))
        {
            expression();
            if (match(";"))
            {
                return;
            }
            else
            {error(";");}
        }
        else if (match(";"))
        {return;}
        else {error("expression statment");}
    }
    public void selection_statement() //N-> e(Q)L6  //Todo fix if else
    {
        System.out.println("selection statement\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match("if"))//e
        {
            if (match("("))
            {
                expression();
                if (match(")"))
                {
                    statement();
                    local_declarations_prime();
                }
                else{error("selection )");}
            }
            else{error("(");}
        }
    }
    public void local_declarations_prime()//6 -> fL | @
    {
        System.out.println("local declaration prime\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match("else"))
        {
            if (look("while")||look("if")||lookType("num")||lookType("int")|| lookType("float")||lookType("id")||look(";")||look("(")||look("{")||look("return"))//check first of statement
            {
                statement();
            }
            else{error("statement");}
        }
        else if (look("while")||look("if")||lookType("num")|| lookType("int")|| lookType("float")||lookType("id")||look(";")||look("(")||look("{")||look("return")||look("}"))
        {
         return;//check follows of 6
        }
        else {error("local declaration");}
    }
    public void iteration_statement() //O-> g(Q)L
    {
        System.out.println("iteration statement\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match("while"))
        {
            //(Q)L
            if (match("("))
            {
                expression();
                if (match(")"))
                {
                    statement();
                }
                else {error(")");}
            }
            else
            {error("(");}
        }
        else
        {error("while ");}
    }
    public void return_statement()  //P-> h7
    {
        System.out.println("return statment\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match("return"))//h
        {
            stemmed_return_statement();
        }
        else
        {error("return");}
    }
    public void stemmed_return_statement() //7->;|Q;
    {
        System.out.println("stemmed return statment\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match(";"))
        {return;}
        else  if (lookType("num")||lookType("int")||lookType("float")||lookType("id")||look("("))//todo check for first of Q
        {
            expression();
            if (match(";"))
            {return;}
            else {error("; ");}
        }
        else {error("; ");}
    }
    public void expression() //Q-> R=Q | T
            /*Correction
           Q->  var=expression | simple expression
           R=Q|T    T->U9  U->Xu  u->WXu|@ X-> Zy
                    T->Xu9
                    T->Zyu9   Z->(Q)|R|1|c  1->a(2) R->a8 Z c-num
            Q -> (Q)yu9 | cyu9 | am
            m -> 8yu9 | (2)yu9 | 8=Q
            */

    {
        System.out.println("expression\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (matchType("id"))
        {
            stemmed_expression();
            //if (match("="))

        }
        else if (match("("))
        {
            //Q->(Q)yu9
            expression(); //Q expression
            if (match(")"))
            {}
            else {error(")");}
            term_prime(); //y term prime
            additive_expression_prime();// u term_prime
            stemmed_other_expression();//9

        }
        else if (matchType("num"))
        {
            term_prime(); //y term prime
            additive_expression_prime();// u term_prime
            stemmed_other_expression(); //9
        }
        else if (matchType("int"))
        {
            term_prime(); //y term prime
            additive_expression_prime();// u term_prime
            stemmed_other_expression(); //9
        }
        else if (matchType("float"))
        {
            term_prime(); //y term prime
            additive_expression_prime();// u term_prime
            stemmed_other_expression(); //9
        }
        else {error ("expression");}

    }
    public void stemmed_expression() //m -> 8yu9 | (2)yu9 | 8=Q
    {
        System.out.println("stemmed expression\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match("("))
        {
            args(); //2
            if (match(")"))
            {

                term_prime();  //y
                additive_expression_prime(); //u
                stemmed_other_expression(); //9
            }
            else
            {error (")");}

        }
        else if (look("["))
        {
            stemmed_variable();  //8
            if (match("="))
            {
                expression();
            }
            else
            {
                stemmed_variable(); //8
                term_prime();  //y
                additive_expression_prime(); //u
                stemmed_other_expression(); //9
            }
        }
        else if (match("="))
        {
            expression();
        }
        else
        {
            stemmed_variable(); //8
            term_prime();  //y
            additive_expression_prime(); //u
            stemmed_other_expression(); //9
        }
    }
    public void variable() //R-> a8
    {
        System.out.println("variable\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (matchType("id"))
        {stemmed_variable();}
        else{error("id");}
    }
    public void stemmed_variable() //8->[Q]|@
    {
        System.out.println("stemmed variable\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match("["))
        {
            expression();
            if (match("]"))
            {

                return;
            }
            else {error("]");}
        }
        else if (look("=")||look("*")||look("/")||look("+")||look("-")||look("!")||look(">")||look("<")||look("]")||look(";")||look(")")||look(",")||look("(")||look("<=")||look(">=")||look("==")|look("!="))//check follows of 8
        {return;}
        else {error("operand expected");}
    }
    public void simple_expression() //T-> U9
    {
        System.out.println("simple expression\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        additive_expression();
        stemmed_other_expression();
    }
    public void stemmed_other_expression() //9->VU |@
    {
        System.out.println("stemmed expression\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (look("!")||look(">")||look("=")||look("<")||look("<=")||look(">=")||look("==")||look("!="))
        {
            relop();
            additive_expression();
        }
        else if(look("]")||look(";")||look(")")||look(","))//follows of 9
        {return;}
        else{error("stemmed expression");}

    }

    public void additive_expression() //U-> Xu
    {
        System.out.println("additive expression\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        term();
        additive_expression_prime();
    }
    public void additive_expression_prime() //u->WXu |@
    {
        System.out.println("additive expression prime\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (look("+")||look("-"))
        {
            addop();
            term();
            additive_expression_prime();
        }
        else if (look("!")||look(">")||look("=")||look("=")||look("<")||look("]")||look(";")||look(")")||look(",")||look("<=")||look(">=")||look("==")||look("!="))//follows of u
        {
            return;
        }
        else {error("right hand of additive expression or end of expression");}
    }
    public void relop() //V -> <x | >x | == | !=
    {
        System.out.println("relop\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match("<"))
        {}
        else  if (match(">")){return;}
        else if(match("==")){return;}
        else if (match("!=")){return;}
        else if (match(">=")){return;}
        else if (match("<=")){return;}
        //else if (match("=")){return;}
        /*
        else if (lookType("int")||lookType("float")||lookType("num")){return;}
        else if (lookType("id")){return;}
        else if (lookType("(")){return;}

        else
        {error("relative operation ");}
        */
    }
    public void addop() //W-> + | -
    {
        System.out.println("additive operation\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match("+"))
        {return;}
        else if (match("-"))
        {return;}
            else {error("additive operand");}
    }
    public void term() //X-> Zy
    {
        System.out.println("term\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        factor();
        term_prime();
    }
    public void term_prime() //y-> YZy | @
    {
        System.out.println("term prime\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (look("*")||look("/"))
        {
            mulop();  //this is an error
            factor();
            term_prime();
        }

        else if (look("+")||look("-")||look("!")||look(">")||look("=")||look("<")||look("]")||look(";")||look(")")||look(");")||look(",")||look("<=")||look(">=")||look("==")||look("!="))//follows y
        {
            return;
        }
        else
        {error ("operand, ',', or ';' ");}
    }
    public void mulop() //Y-> * | /
    {
        System.out.println("multiplication operation\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match ("*"))
        {
            return;
        }
        else if (match("/"))
        {
            return;
        }
        else
        {
            error ("multiplication operand ");
        }
    }
    public void factor() /*Z -> (Q) | R | 1 | c
                         //R->a8
                        //1->a(2)
                        correction Z->(Q)|a new|c
                        new -> 8 |(2)

                        */
    {
        System.out.println("factor\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match("("))
        {
            expression();
            if( match (")"))
            {
                return;
            }
            else {error(")");}
        }
        else if (matchType("id"))//first of R and 1
        {
            variable_call_discriminator();
        }
        else if (matchType("num"))
        {return;}
        else if (matchType("int"))
        {return;}
        else if (matchType("float"))
        {return;}
        else {error (" '(expression)', variable, call or number type ");}

    }
    public void variable_call_discriminator() //new -> 8 |(2) | =Q
    {
        System.out.println("variable or call discriminator\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match("(")) {
            args();
            if (match(")"))
            {return;}
            else{error(")");}
        }
        //first of 8 and follows of 8
        else if (look("[")||look("=")||look("*")||look("/")||look("+")||look("-")||look("!")||look(">")||look("<")||look("]")||look(";")||look(")")||look(",")||look("==")||look("!=")||look(">=")||look("<="))
        {
            stemmed_variable();
        }

    }
    public void call() //1-> a(2)
    {
        System.out.println("call\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (matchType("id "))
        {
            if (match("("))
            {
                args();
                if (match(")"))
                {return;}
                else {error(")");}
            }
            else {error("( ");}
        }
        else {error("id ");}
    }
    public void call_prime() //1'
    {
        System.out.println("call prime\n This method is empty and will require population\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
    }
    public void args() //2 -> 3 | @
    {
        System.out.println("arguments\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
      if (lookType("num")||lookType("int")||lookType("float")||lookType("id")||look("("))
      {
          args_list();
      }
        else if (look(")"))//follows of 2
      {return;}
        else {error("arguments");}
    }
    public void args_list() //3-> Qv
    {
        System.out.println("arguments list\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        expression();
        if (look(")"))
        {return;}
        else {args_list_prime();}
    }
    public void args_list_prime() //v-> ,Qv | @
    {
        System.out.println("arguments list prime\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        if (match(","))
        {
         expression();
         args_list_prime();
        }
        else if (look(")"))//follows of v
        {return;}
        else {error(",");}

    }
}
//