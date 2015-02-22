import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
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
    }

    public void makePatterns()
    {

        //myFloat;

        myNum=myNum.compile("\\A\\d+");
        myKeyword=myKeyword.compile("\\A(else|if|int|return|void|while)&&(\\b|\\B)");
        mySymbols=mySymbols.compile("\\A\\+|\\A-|\\A\\*|\\A/|\\A>=|\\A<=|\\A==|\\A!=|\\A=|\\A;|\\A,|\\A\\(|\\A\\)|\\A\\[|\\A\\]|\\A\\{|\\A\\}|\\A<|\\A>");
        myFloat=myFloat.compile("\\A((([1-9]+\\.[0-9]*)|([1-9]*\\.[0-9]+))([E][-+]?[0-9]+)?)|(([1-9]+\\.[0-9]*)|([1-9]*\\.[0-9]+)|([1-9]+))([E][-+]?[0-9]+)");
        myID=myID.compile("\\A[a-zA-Z]+"); //This is catching on spaces and empty strings:TODO FIX
        myNextNum=myNextNum.compile("\\A\\d+");
        myNextKeyword=myNextKeyword.compile("(else|if|int|return|void|while)&&(\\b|\\B)");
        myNextSymbols=myNextSymbols.compile("\\+|-|\\*|/|<=|>=|==|!=|=|;|,|\\(|\\)|\\[|\\]|\\{|\\}|<|>");
        myNextFloat=myNextFloat.compile("((([1-9]+\\.[0-9]*)|([1-9]*\\.[0-9]+))([E][-+]?[0-9]+)?)|(([1-9]+\\.[0-9]*)|([1-9]*\\.[0-9]+)|([1-9]+))([E][-+]?[0-9]+)");
        myNextID=myNextID.compile("[a-zA-Z]");
        //myID.;
        int pants=0; //debug variable pants for breakpoint

    }

    public String removeLineComments(String input)
    {
        System.out.print("Input:"+input+"\n");
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
            System.out.print(nextToken.toString()+"\n");
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
            // remainingInputString+="  ";
        }
        //System.out.print("Remaining length: "+remainingInputString.length()+"\n"+remainingInputString);

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
    public void matchType(String input)
    {
         Token testToken=tokens.get(tokenCounter);
         if (testToken.getType().equals(input))
         {
             tokenCounter++;
         }
        else
         {
             System.out.print("Error Token: "+testToken.toString()+"\nIs not of type: "+input+"\n Program will not parse.");
         }
    }
    public void matchType(Collection<String> inputs)
    {
        Token testToken=tokens.get(tokenCounter);
        Boolean match=false;
        for (String inputStringType: inputs) {
            if (testToken.getType().equals(inputStringType)) {
                match=true
                tokenCounter++;
            }
        }
        if (!match)
        {
            System.out.print("Error Token: "+testToken.toString()+"\nIs not of the accepted calling types.\n Program will not parse.");
            System.exit(-1);
        }
    }

    public void program() //S
    {
        declaration_list();
    }
    public void declaration_list() //A
    {
        Token nextToken=getToken();

    }
    public void declaration_list_prime() //A'
    {
        if ()//test for
            declaration();
        declaration_list_prime();
        else
        //@
    }
    public void declaration()  //B
    {

    }
    public void var_declaration()  //C
    {}
    public void fun_declaration()  //D
    {}
    public void  type_specifier() //E
    {}
    public void parameters()  //F
    {}
    public void compound_statement() //G
    {}
    public void parameter_list() //H
    {}
    public void parameter() //I
    {}
    public void local_declaration() //J
    {}
    public void statement_list() //K
    {}
    public void statement()  //L
    {}
    public void expressions_statement()  //M
    {}
    public void selection_statement() //N
    {}
    public void iteration_statement() //O
    {}
    public void return_statement()  //P
    {}
    public void expression() //Q
    {}
    public void variable() //R
    {}
    public void simple_expression() //T
        {}
    public void additive_expression() //U
    {}
    public void relop() //V
    {}
    public void addop() //W
    {}
    public void term() //X
    {}
    public void term_prime() //X'
    {}
    public void mulop() //Y
    {}
    public void factor() //Z
    {}
    public void call() //1
    {}
    public void call_prime() //1'
    {}
    public void args() //2
    {}
    public void args_list() //3
    {}
    public void args_list_prime() //3'
    {}



}
//