import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.*;

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
    Token declerationRegister;
    Token callRegister;
    Boolean debug;
    Boolean debugMethods;
    ArrayList<String> outputFile;
    ArrayList<String> backpatchedOutputFile;
    int lineNumber;
    int tempCounter;
    boolean backpatchRegister;
    String backPatchLabel;

    int backpatchIndex;
    class Backpatch
    {

        String backLabel;
        int targetlinenumber;

        Backpatch (String label, int target)
        {
            backLabel=label;
            targetlinenumber=target;
        }

    }

    class Token
    {
        private String myLexum;
        private String type;
        private String assignedType;
        private int scopeDepth;
        private boolean function;
        private boolean array;
        private int length;
        private ArrayList<String> parameterList;
        ArrayList<Token> argumentsList;
        boolean returned;

        Token(String inputToken)
        {
            myLexum=new String(inputToken);
            length=0;
            array=false;
            function=false;
            parameterList=new ArrayList<String>();
            argumentsList=new ArrayList<Token>();
            returned=false;
        }

        public void setLexum(String input)
        {
            myLexum=input;
        }
        public String getLexum() {return myLexum;}


        public String getToken()
        { return myLexum;}

        public void setFunction() {function=true;}
        public void setParameter(String parameter) {parameterList.add(parameter); function=true;}

        public void setLength(int length) {
            this.length = length;
            array=true;
        }
        public void makeArray()
        {
            array=true;
        }
        public Boolean isArray()
        {
            return array;
        }
        public ArrayList<String>getParameterList()
        {
            return parameterList;
        }
        public int getLength()
        {
            return length;
        }

        public void setScope(int inputScope)
        {
            this.scopeDepth=inputScope;
        }
        public int getScope()
        {return this.scopeDepth;}

        public void setType(String newType)
        {
            type=newType;
        }
        public void setAssignedType(String input)
        {
            assignedType=input;
        }
        public String getAssignedType()
        {
            return assignedType;
        }

        public String getType()
        {
            return type;
        }

        public String toString()
        {
            String output = new String();
            output ="Token: \""+myLexum+"\"\n";
            output+="Type: "+type+". Scope: "+this.scopeDepth+"\n";
            output+="Assigned Type:"+assignedType+"\n";

            return output;
        }
    }

    class TypeValue
    {
        String type;
        double value;
        ArrayList<String> parameters;
        
    }

    public static void main(String[] args) {
        Main myMain = new Main();
        //System.out.print("Aaron Wagner COP 5625 P1 \n \n");
        myMain.targetFile=args[0];
        myMain.loadFile();
        myMain.makePatterns();
        myMain.depth=0;
        myMain.scope=0;
        myMain.tokens = new ArrayList<Token>();
        myMain.debug =false;
        myMain.debugMethods=false;
        for (int i=0; i<myMain.inputArray.length; i++)
        {
            if (myMain.debug) System.out.print("I="+i+":~"+myMain.inputArray[i]);
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
        //Todo refactor and move this to program then redfine as private
        myMain.symbolTabel=new ArrayList<HashMap>();
        myMain.symbolTabel.add(new HashMap<String, Token>());
        myMain.makeEndToken();
        myMain.program();
        System.out.print("File before backpatching\n"+myMain.outputFile);
        myMain.backpatch();

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
            String possibleFloat=(myFloatMatcher.group());
            Matcher myNumchecker=myNum.matcher(possibleFloat);
            myNumchecker.find();
            String possibleInt=(myNumchecker.group());
            if (possibleFloat.equals(possibleInt))
            {
                returnedToken=new Token(possibleInt);
                returnedToken.setType("int");
            }
            else
            {
                returnedToken=new Token(possibleFloat);
                returnedToken.setType("float");
            }

            //returnedToken.setType("float");
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
    public void backpatch(){}

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
                 System.out.println("ACCEPT: " + tokens.get(tokenCounter).toString() + "\n");
                 System.out.println("ACCEPT");
             }
             else
             {

                     if (debug){System.out.println("Accepted: "+tokens.get(tokenCounter).toString()+"\n");}

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
            if (debugMethods){
                System.out.println("Accepted: "+tokens.get(tokenCounter).toString()+"\n");
            }
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
                if (debugMethods){
                    System.out.println("Accepted: "+tokens.get(tokenCounter).toString()+"\n");
                }
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
                if (debugMethods){
                    System.out.println("Accepted: "+tokens.get(tokenCounter).toString()+"\n");
                }
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

    /*public boolean compareType(String inputOne, String inputTwo)
    {
        if ((inputOne!=null)&&(inputTwo!=null))
        {
            if (inputOne.equals(inputTwo)) {
                return true;
            } else {
                System.out.println("REJECT\n");
                if (debug) System.out.println("Type mismatch: " + inputOne + " with " + inputTwo + ".");
                System.exit(-1);
                return false;
            }
        }
        else
        {
            return true;
        }
    }*/

    public boolean compareType (Token left, Token right)
    {

        Boolean output=false;
        if (left==null)
        {

            reject("left operator is null");
        }

        if (right==null)
        {
            return true;
        }


        System.out.print("comparing "+left.toString()+" and "+right.toString()+"\n");

        if (left.getAssignedType()==null)
        {
            if (!(left.getType().equals("id")))
            {
                if(!right.getType().equals("id"))
                {
                    if (left.getType().equals(right.getType()))
                    {
                        return true;
                    }
                    else
                    {
                        reject("type mismatch between "+left.getLexum()+" and "+right.getLexum());
                    }
                }
                else
                {
                    if (left.getType().equals(right.getAssignedType()))
                    {
                        return true;

                    }
                    else
                    {
                        reject("type mismatch between "+left.getLexum()+" and "+right.getLexum());
                    }
                }
            }
            else {
                reject("left operator type is null\nLeft: \n" + left.toString() + "\n");
            }
        }
        else
        if (right.getAssignedType()==null)
        {
            if (left.getAssignedType().equals(right.getType()))
            {
                output=true;
                return output;
            }
            else {
                reject("type mismmatch between "+left.getLexum()+" and "+right.getLexum());
            }
        }
        else
        if (left.getAssignedType().equals(right.getAssignedType()))
        {

            return true;
        }
        else
        {
            reject("type mismmatch between "+left.getLexum()+" and "+right.getLexum());
        }
        return output;

    }
    public Token addTemp(Token input)
    {


        tempCounter++;
        String output="temp"+Integer.toString(tempCounter);
        Token tempToken=new Token(output);
        if (input.assignedType!=null)
        {
            tempToken.setAssignedType(input.getAssignedType());
        }
        else
        {
            tempToken.setAssignedType(input.getType());
        }

        symbolTabel.get(scopeDepth).put(tempToken.getLexum(), tempToken);
        allocate(tempToken);
        return tempToken;
    }

    public boolean compareType(Collection<String> inputs)
    {
        boolean matches=true;

        for (String inputString: inputs)
        {
            for (String otherInputString: inputs)
            {
                if ((inputString!=null)&&(otherInputString!=null))
                {
                    if (inputString.equals(otherInputString)) {
                        matches = true;
                    } else {
                        System.out.println("REJECT\n");
                        System.out.println("Type mismatch: " + inputString + " with " + otherInputString + ".");
                        System.exit(-1);
                        matches = false;
                    }
                }
                else
                {
                    continue;
                }
            }
        }
        return matches;
    }



    public String getType(String lexum)
    {
        boolean found=false;
        String output=null; //will return null or call error if not found
        Token storedToken;
        for (int i=symbolTabel.size(); i>-1; i--)
        {
            storedToken=(Token) symbolTabel.get(i).get(lexum);
            if (storedToken!=null)
            {
                found = true;
                storedToken.getAssignedType();
                break;
            }
            if (found) break;
        }
        return output;
    }

    public void error(String expected)
    {
            System.out.println("Error " + expected + " expected. \n Have: \"" +
                    tokens.get(tokenCounter).getLexum() +
                    "\"\n Of type: " + tokens.get(tokenCounter).getType() +
                    "\n on Token # " + tokenCounter + "\n");

        System.exit(-1);
    }

    public Token findToken(String lexum)
    {
        Token output=new Token("");
        HashMap <String, Token> holder;
        for (int i=symbolTabel.size()-1; i>-1; i--)
        {

            holder=symbolTabel.get(i);
            output=holder.get(lexum);
            if (output==null)
            {
                continue;
            }
            else
            {
                break;
            }
        }
        return output;
    }
    public Token checkToken(String lexum)
    {
        Token output;

        output=findToken(lexum);
        if (output==null)
        {
            reject(lexum+" not defined in symbol table.");
        }
        return output;
    }
    public Token checkToken(Token checkingToken)
    {
        if (!checkingToken.getType().equals("id"))
        {
            checkingToken.assignedType=checkingToken.getType();
            return checkingToken;
        }
        Token output;
        output=findToken(checkingToken.getLexum());

        if (output==null)
        {
            reject(checkingToken.getLexum()+" not defined in symbol table.");
        }
        else
        {
            checkingToken.setAssignedType(output.getAssignedType());
        }
        return output;
    }
    public void reject(String message)
    {

        if (debug){System.out.print(message+"\n");}
        //printSymbolTabel();
        System.out.print("REJECT\n");
        System.exit(-1);
    }
    public void printSymbolTabel()
    {
        System.out.println("SymbolTabel: \n");
        for (int i=0; i<symbolTabel.size(); i++)
        {
            System.out.println("Scope:"+i);
            System.out.print(symbolTabel.get(i).toString()+"\n");


        }
    }

    public void addLine (String operator, String param1, String param2, String target)
    {
        lineNumber++;
        if (outputFile==null)
        {
            outputFile=new ArrayList<String>();
        }
        if (!backpatchRegister) {
            outputFile.add(lineNumber + "\t" + operator + "\t" + param1 + "\t" + param2 + "\t" + target);
        }
        else
        {
            outputFile.add(lineNumber + "\t" + operator + "\t" + param1 + "\t" + param2 + "\t" + target + "\t" +backPatchLabel);
        }

    }
    public void addLine (String operator, String param1, String param2, String target, String backpatchLabel)
    {
        lineNumber++;
        if (outputFile==null)
        {
            outputFile=new ArrayList<String>();
        }
        outputFile.add(lineNumber+"\t"+operator+"\t"+param1+"\t"+param2+"\t"+target+"\t"+backPatchLabel);
    }
    public void storeToken(Token input)
    {
        Token checker=null;
        if (input!=null)
        {
            checker=(Token)symbolTabel.get(scopeDepth).put(input.getLexum(), input);
            if (checker!=null)
            {
                reject(checker.getLexum()+" of type "+checker.getAssignedType()+" allready exists for this scope when attempting to define"+input.getLexum()+" of type "+input.getAssignedType() );
            }
        }
    }
    public void storeIfNewToken(Token input)
    {
        Token checker=null;
        if (input!=null)
        {
            checker=(Token)symbolTabel.get(scopeDepth).put(input.getLexum(), input);
            if (checker!=null)
            {
                symbolTabel.get(scopeDepth).put(input.getLexum(), checker);
            }
        }
    }
    public Token getIntermediate (Token left, Token right)
    {
        if (right==null)
        {
            return left;
        }
        else
        {
            return right;
        }
    }
    public void allocate (Token theDeclared)
    {
        if (!(theDeclared.getAssignedType()!=null)&&!(theDeclared.getAssignedType().equals("void")))
            //should not need to check for a void allocation
        {
            if (theDeclared.getAssignedType().equals("int"))
            {
                addLine("ali", "4", "", theDeclared.getLexum());
            }
            else if(theDeclared.getAssignedType().equals("float"))
            {
                addLine("alf", "4", "", theDeclared.getLexum());
            }
            else if (theDeclared.getAssignedType().contains("array"))
            {
                int size = theDeclared.length;
                if (size==0)
                {
                    reject(theDeclared.getLexum()+" declared as "+theDeclared.getAssignedType()+"[] of size zero");
                }
                if (theDeclared.getAssignedType().contains("int"))
                {
                    addLine("ali", Integer.toString((size*4)), "", theDeclared.getLexum());
                }
                else if (theDeclared.getAssignedType().contains("float"))
                {
                    addLine("alf", Integer.toString((size*4)), "", theDeclared.getLexum());
                }
            }


        }
        else if (theDeclared.getAssignedType().equals("void"))
        {
            reject("Why did "+theDeclared.getLexum()+" declared as a void? get past symantic analysis" );
            //check for semanticlly this should be redundant
        }
        else
        {
            //if a variable value is declared
            reject("Why did "+theDeclared.getLexum()+" fall into assignment without an assigned type?" );
        }
    }

// This starts the recursive decent methods
    /////////////////////////////////////////////////////////////////////////////////
    public void program() //S->EaCA
    {
       // System.out.print("DECENDING \n***********************************************");
        //System.out.println("program\n TokenCounter:" + tokenCounter + "Token: " + tokens.get(tokenCounter).toString());
       ///////////////////////////////////////////////////////////////////////////
       //Decent initilization
        scopeDepth=0;
        Token input=new Token("input");
        input.setAssignedType("int");
        input.parameterList.add("void");
        symbolTabel.get(scopeDepth).put(input.getLexum(), input);
        Token output=new Token("output");
        output.setAssignedType("void");
        output.parameterList.add("int");
        symbolTabel.get(scopeDepth).put(output.getLexum(), output);
        scopeDepth++;
        symbolTabel.add(new HashMap<String,Token>());
        String type;
        //End initalization
        /////////////////////////////////////////////////////////////////////////////
        type=type_specifier();//E
        Token theDeclared=tokens.get(tokenCounter);

        if (matchType("id")){
            symbolTabel.get(scopeDepth).put(theDeclared.getLexum(),theDeclared);
            theDeclared.setAssignedType(type);
            if (debug){ System.out.print(theDeclared.getLexum()+" added to symbol table (in program) as a "+theDeclared.getAssignedType() +"\n");}
        }//a
        else
        {
            error("Type id");
        }
        stemmed_decleration(theDeclared);//C
        /*
        symbolTabel.get(scopeDepth).put(theDeclared.getLexum(), theDeclared);
        if (debug){ System.out.print(theDeclared.getLexum()+" added to symbol table (in program) as a "+theDeclared.getAssignedType() +"\n");}
        moved to if match id
        */
        declaration_list(theDeclared);//A
        if (match("$"))
        {
            if (theDeclared.getLexum().equals("main"))
            {
                System.out.println("ACCEPT");
                System.exit(0);
            }
            else
            {
                reject("Main is not the last function declared contrary to the requirement on pg 493");
            }
        }

    }
    public void declaration_list(Token theDeclared) //A->BA | @
    {
        if (debugMethods){
            System.out.println("declaration list\n TokenCounter:"+tokenCounter+"Token: " + tokens.get(tokenCounter).toString());
        }
        //check for first of B
        if (look("int")||look("void")||look("float"))//First of B {d b n}
        {
            theDeclared=declaration();  //B
            declaration_list(theDeclared);//A
        }
        else if (match("$"))
        {
            if (theDeclared.getLexum().equals("main"))
            {
                System.out.println("ACCEPT");
                System.exit(0);
            }
            else
            {
                reject("Main is not the last function declared contrary to the requirement on pg 493");
            }
        }
        else return;
    }

    public Token declaration()  //B->EaC
    {
        String theType;
        Token checker=null;
        TypeValue modifiers = new TypeValue();
        if (debugMethods){System.out.println("declaration\n TokenCounter: " + tokenCounter + "Token: " + tokens.get(tokenCounter).toString());
        }
        theType=type_specifier();//E
        Token theDeclared=tokens.get(tokenCounter);
        theDeclared.setAssignedType(theType);
        if (matchType("id"))
            {
                checker=(Token)symbolTabel.get(scopeDepth).put(theDeclared.getLexum(), theDeclared);
                if (theDeclared.getAssignedType().equals("void"))
                {
                    reject("Illegal Void decleration of "+theDeclared.getLexum());
                }
                if (debugMethods) {System.out.print(theDeclared.getLexum()+" added to symbol table (in declaration) as a: "+ theDeclared.getAssignedType()+".\n");}


            }//a
        else
        {
            error("Type id");
        }
       modifiers=stemmed_decleration(theDeclared);//C


        if (modifiers.type!=null&&modifiers.type.equals("array"))
        {
            theDeclared.array=true;
            theDeclared.setAssignedType(theType+" array");
            if (modifiers.value!=0)
            {
                theDeclared.setLength((int) modifiers.value);
                //theType+=" array";
            }
        }

        if (checker !=null)
        {
            System.out.println("REJECT \n duplicate lexum decliration in same scope\n have :"+checker.toString()+
                    "allready declared when declaring "+theDeclared.toString());
            System.exit(-1);
        }
        return theDeclared;

    }

    public TypeValue stemmed_decleration(Token theFunction)  //C->4 |(F)G
    {

        TypeValue output=new TypeValue();
        if (debugMethods) {
            System.out.println("stemmed declaration\n TokenCounter: " + tokenCounter + "Token: " + tokens.get(tokenCounter).toString());
        }
        if (look(";")||look("["))
        {
            output=stemmed_vardecleration();//4
            if (output.type!=null&&output.type.equals("array"))
            {
                theFunction.setLength((int) output.value);
            }
            allocate(theFunction);//generates a line
        }
        else if(match("("))//(F)G
        {

            parameters(theFunction);//F
            if (match(")"))
            {
                /*
                this begins my convention of double indenting addLines
                 */
                       addLine("FUNC", theFunction.getLexum(), theFunction.getAssignedType(), Integer.toString(theFunction.argumentsList.size()));
                compound_statement(theFunction); //does not need parameters
                       addLine("END FUNC", "", "", theFunction.getLexum());
            }
            else {error(")");}
        }
        else {error("(");}

        return output;
    }
    public TypeValue stemmed_vardecleration() //4 -> ; | [c]
    {
        TypeValue output=new TypeValue();
        if (debugMethods){
            System.out.println("stemmed var declaration\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match(";"))
        {
            output.type=null;
            output.value=0;
            return output;
        }
        else if (match("[")) {

            if (matchType("int")||matchType("float")||matchType("num"))//c
            {
                output.value = Integer.parseInt(tokens.get(tokenCounter - 1).getLexum());


                if (match("]")) {
                    output.type = "array";
                    if (match(";")) {
                        return output;
                    } else {
                        error(";");
                    }
                } else {
                    error("]");
                }
            }
            else
            {
                error("integer");
            }
        }
        else
        {
            error("[");
        }
        return output;

    }

    public void declaration_prime(Token theFunction) //0 -> ,I0|@
    {
        backpatchRegister=false;
        backPatchLabel="";

        if (debugMethods){
            System.out.println("decleartion prime\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if(match(","))
        {
            parameter(theFunction);   //I
            declaration_prime(theFunction);//0
        }
        else if (look(")"))//look for follows of 0
        {return;}
        else{error(",");}
    }
    public String  type_specifier() //E-> d | b | n
    {
        if (debugMethods){
            System.out.println("type specifier\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match("int"))//d
            {
                return "int";
            }
        else if (match("void"))//b
            {
                return "void";
            }
        else if(match("float"))//n
            {
                return "float";
            }
        else
            {
                error("Type Specifier");
                return null;
            }

    }
    public void parameters(Token theFunction)  //F-> ED
    {

        if (debugMethods)
        {
            System.out.println("parameter\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        String theType;
        theType=type_specifier();
        theFunction.parameterList.add(theType);
        //done fun decleration needs to return additional parameters to include in output

        fun_declaration(theType, theFunction);

    }
    public void fun_declaration(String inputtype, Token theFunction)  //D->a50 | @
    {
        if (debugMethods)
        {
            System.out.println("function declaration\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (matchType("id"))//a
        {
            TypeValue modifiers;
            Token theParameter=tokens.get(tokenCounter-1);
            theParameter.setAssignedType(inputtype);
            theFunction.argumentsList.add(theParameter);
            //symbolTabel.get(scopeDepth).put(theParameter.getLexum(), theParameter);
            //System.out.print(theParameter.getLexum()+" added to symbol table (in fun_declaration) as a"+ theParameter.getAssignedType()+"\n");
            modifiers=parameter_list_prime(); //5
            if (modifiers.type!=null&&modifiers.type.equals("array"))
            {
                String type=theFunction.parameterList.get(theFunction.parameterList.size()-1);
                type+=" array";
                theFunction.parameterList.remove(theFunction.parameterList.size()-1);
                theFunction.parameterList.add(type);
                theParameter.setAssignedType(type);
                theParameter.setLength((int)modifiers.value);


            }
            if (debug){System.out.print("Parameter: "+theParameter.getLexum()+" of type "+theFunction.parameterList.get(theFunction.parameterList.size()-1)+" added to a function "+theFunction.getLexum()+".\n");}

            //doneTodo check modifiers for type and integrate into last parameter on parameter list.
            declaration_prime(theFunction); //0
        }
        else
        {
            //check follows of D
            if (look(")"))
            {}
            else
            {
                error (")");
            }
        }

    }
    public TypeValue compound_statement(Token theFunction) //G-> {JK}
    {
        TypeValue output=new TypeValue();
        //doneTodo may not need
        if (debugMethods){
            System.out.println("compound statement\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match("{"))
        {
            scopeDepth++;
            symbolTabel.add(new HashMap<String, Token>());
            for (Token argument: theFunction.argumentsList)
            {
                symbolTabel.get(scopeDepth).put(argument.getLexum(), argument);
            }

            local_declaration();   //J
            statement_list(theFunction);       //K

            //doneTodo  JK need to affect output?
            if (match("}"))
            {
                scopeDepth--;
                symbolTabel.remove(symbolTabel.size()-1);
                return output;
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
        return output;
    }
    public void parameter_list() //H
    {
        //System.out.println("parameter list\n this method is empty and will need to be populated\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
    }
    public TypeValue parameter_list_prime() //5->[]|@
    {
        TypeValue output=new TypeValue();
        if (debugMethods){
            System.out.println("parameter list prime\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match("["))
        {
            if (match("]"))
            {
                output.type="array";
                if (look(",")||look(")")) ///check follows of 5
                {
                    return output;
                }
                else
                {
                    error ("}");
                }

            }
        } else if (look(",")||look(")")) ///check follows of 5
            {
                return output;
            }
            else
            {
                error ("}");
            }
        return output;

    }
    public void parameter(Token theFunction) //I-> Ea5
    {
        Boolean matched=false;
        String theType= new String();
        if (debugMethods){
            System.out.println("parameter\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        theType=type_specifier();   //E
        if (matchType("id"))//a
        {
           matched=true;
            Token theParameter=tokens.get(tokenCounter-1);
            theFunction.parameterList.add(theType);
            theParameter.setAssignedType(theType);
            //storeToken(theParameter);
            TypeValue modifiers=parameter_list_prime();//5
            if (modifiers.type!=null&&modifiers.type.equals("array"))
            {
                String type=theFunction.parameterList.get(theFunction.parameterList.size()-1);
                type+=" array";
                theFunction.parameterList.remove(theFunction.parameterList.size()-1);
                theFunction.parameterList.add(type);
                theParameter.setLength((int)modifiers.value);

            }
            if (debug)
            {
                System.out.print("Parameter: "+theParameter.getLexum()+" of type "+theType+" added to function "+theFunction.getLexum()+".\n");
            }
            symbolTabel.get(symbolTabel.size()-1).put(theParameter.getLexum(), theParameter);

        }
        else
        {
            error("parameter" );
        }

    }
    public void local_declaration() //J->BJ | @
    {
        if (debugMethods){
            System.out.println("local declaration\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
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
    public void statement_list(Token theFunction) //K->LK
    {
        if (debugMethods){
            System.out.println("statment list\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        //check first of statment
        if (look("while")||look("if")||lookType("num")||lookType("int")|| lookType("float")||lookType("id")||look(";")||look("(")||look("{")||look("return"))
        {
            statement(theFunction);
            statement_list(theFunction);
        }
        else if (look("}"))//check follows of K
        {
            if (!theFunction.getAssignedType().equals("void"))
            {
                if (theFunction.returned==false)
                {
                    reject("missing return statment in "+theFunction);
                }
            }
            return;
        }
        else {error("}");}
    }
    public void statement(Token theFunction)  //L-> M | G | N | O | P
    {
        if (debugMethods){
            System.out.println("statement\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (lookType("num")||lookType("id")||look(";")||look("(")||lookType("int")|| lookType("float") )//first of M
        {
            expressions_statement();
        }
        else if (look("{"))//first of G:compound_statement
        {
            compound_statement(theFunction);
        }
        else if (look("if"))//first of N:selection_statment
        {
            selection_statement(theFunction);
        }
        else if(look("while")) //first of O:iteration_statment
        {
            iteration_statement(theFunction);
        }
        else if(look("return")) //first og P :return_statement
        {
            return_statement(theFunction);
            theFunction.returned=true;

        }
        else{error ("statment ");}

    }
    public void expressions_statement()  //M->Q; | ;
    {
        if (debugMethods){
            System.out.println("expression statement\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
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
    public void selection_statement(Token theFunction) //N-> e(Q)L6  //doneTodo fix if else
    {
        if (debugMethods){
            System.out.println("selection statement\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match("if"))//e
        {
            if (match("("))
            {
                expression();
                if (match(")"))
                {
                    statement(theFunction);
                    local_declarations_prime(theFunction);
                }
                else{error("selection )");}
            }
            else{error("(");}
        }
    }
    public void local_declarations_prime(Token theFunction)//6 -> fL | @
    {
        if (debugMethods){
            System.out.println("local declaration prime\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match("else"))
        {
            if (look("while")||look("if")||lookType("num")||lookType("int")|| lookType("float")||lookType("id")||look(";")||look("(")||look("{")||look("return"))//check first of statement
            {
                statement(theFunction);
            }
            else{error("statement");}
        }
        else if (look("while")||look("if")||lookType("num")|| lookType("int")|| lookType("float")||lookType("id")||look(";")||look("(")||look("{")||look("return")||look("}"))
        {
         return;//check follows of 6
        }
        else {error("local declaration");}
    }
    public void iteration_statement(Token theFunction) //O-> g(Q)L
    {
        if (debugMethods){
            System.out.println("iteration statement\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match("while"))
        {
            //(Q)L
            if (match("("))
            {
                expression();
                if (match(")"))
                {
                    statement(theFunction);
                }
                else {error(")");}
            }
            else
            {error("(");}
        }
        else
        {error("while ");}
    }
    public void return_statement(Token theFunction)  //P-> h7
    {
        if (debugMethods){
            System.out.println("return statment\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match("return"))//h
        {
            stemmed_return_statement(theFunction);
        }
        else
        {error("return");}
    }
    public void stemmed_return_statement(Token theFunction) //7->;|Q;
    {
        if (debugMethods){
            System.out.println("stemmed return statment\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match(";"))
        {
            if (theFunction.getAssignedType()==null||theFunction.getAssignedType().equals("void"))
            {
                return;
            }
            else
            {
                System.out.print("REJECT\n Return without Parameter in function "+theFunction.getLexum()+" of return type "+ theFunction.getAssignedType()+"\n");
                printSymbolTabel();
                System.exit(-1);

            }
        }
        else  if (lookType("num")||lookType("int")||lookType("float")||lookType("id")||look("("))//donetodo check for first of Q
        {
            Token returnValue;
            returnValue=expression();
            if (returnValue.getType().equals("id"))
            {
                checkToken(returnValue);
            }
            else
            {
                if (theFunction.getAssignedType().equals(returnValue.getType()))
                {
                    if (match(";"))
                    {return;}
                }
            }

            if (returnValue!=null&&returnValue.getAssignedType()!=null&&!returnValue.getAssignedType().equals(""))
            {
                if (returnValue.getAssignedType().equals(theFunction.getAssignedType()))
                {// this is the proper condition
                }
                else
                {
                    reject("return value "+returnValue.getLexum()+" does not match return value type for function "+theFunction.getLexum()+" which is "+ theFunction.getAssignedType()+". \n" );
                }
            }
            //doneTodo uncomment after finishing typevalue of expression

            else
            {
                System.out.print("REJECT \n return value has null type: this is probably a compiler error please email aaron.p.wagner@gmail.com with details");
                System.exit(-1);
            }


            if (match(";"))
            {return;}
            else {error("; ");}
        }
        else {error("; ");}
    }
    public Token expression() //Q-> R=Q | T
            /*Correction
           Q->  var=expression | simple expression
           R=Q|T    T->U9  U->Xu  u->WXu|@ X-> Zy
                    T->Xu9
                    T->Zyu9   Z->(Q)|R|1|c  1->a(2) R->a8 Z c-num
            Q -> (Q)yu9 | cyu9 | am
            m -> 8yu9 | (2)yu9 | 8=Q
            */

    {
        Token leftHandSide=null;
        TypeValue output=new TypeValue(); //doneTodo needs to be populated
        if (debugMethods){
            System.out.println("expression\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (matchType("id"))//a
        {
            leftHandSide=tokens.get(tokenCounter-1);
            Token storedLHS=checkToken(leftHandSide);
            leftHandSide.setAssignedType(storedLHS.getAssignedType());
            output.type=storedLHS.getAssignedType();
            TypeValue modifier=new TypeValue();
            if (debug) System.out.print("after look up "+leftHandSide.getLexum()+" is of type: "+leftHandSide.getAssignedType()+"\n");

            modifier=stemmed_expression(leftHandSide);//m
            if (modifier!=null&&modifier.type!=null&&modifier.type.contains("array"))
            {
                if (!(leftHandSide.getAssignedType().contains("array")))
                {
                    String type=leftHandSide.getAssignedType();
                    type+=" array";
                }
            }

            //if (match("="))

        }
        else if (match("("))
        {
            //Q->(Q)yu9
            leftHandSide=expression(); //Q expression
            if (match(")"))
            {}
            else {error(")");}
            term_prime(leftHandSide); //y term prime
            additive_expression_prime(leftHandSide);// u term_prime
            stemmed_other_expression(leftHandSide);//9

        }
        else if (matchType("num"))
        {
            leftHandSide=tokens.get(tokenCounter - 1);
            term_prime(leftHandSide); //y term prime
            additive_expression_prime(leftHandSide);// u term_prime
            stemmed_other_expression(leftHandSide); //9
        }
        else if (matchType("int"))
        {
            leftHandSide=tokens.get(tokenCounter - 1);
            term_prime(leftHandSide); //y term prime
            additive_expression_prime(leftHandSide);// u term_prime
            stemmed_other_expression(leftHandSide); //9
        }
        else if (matchType("float")) {
            leftHandSide=tokens.get(tokenCounter-1);
            term_prime(leftHandSide); //y term prime
            additive_expression_prime(leftHandSide);// u term_prime
            stemmed_other_expression(leftHandSide); //9
        }
        else {error ("expression");}
        return leftHandSide;

    }
    public TypeValue stemmed_expression(Token leftHandSide) //m -> 8yu9 | (2)yu9 | 8=Q
    {
        TypeValue output=new TypeValue();
        if (debugMethods){
            System.out.println("stemmed expression\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match("("))
        {
            checkToken(leftHandSide);
            args(leftHandSide); //2
            if (match(")"))
            {

                term_prime(leftHandSide);  //y
                additive_expression_prime(leftHandSide); //u
                stemmed_other_expression(leftHandSide); //9
            }
            else
            {error (")");}

        }
        else if (look("["))
        {
            output=stemmed_variable(leftHandSide);  //8
            if (match("="))
            {
                Token rightHandSide=expression();
                if ((rightHandSide.getAssignedType()==null)&&(!rightHandSide.getType().equals("int")))
                {
                    reject ("Type missmatch in assignment between "+leftHandSide+" and "+rightHandSide+".");
                }

            }
            else
            {
                //what spould be the left hand side for this?!?!?
                stemmed_variable(leftHandSide); //8
                term_prime(leftHandSide);  //y
                additive_expression_prime(leftHandSide); //u
                stemmed_other_expression(leftHandSide); //9
            }
        }
        else if (match("="))
        {
            //expression();
            Token rightHandSide=expression();
            Boolean good=compareType(leftHandSide,rightHandSide);
            if (!good)
            {
                reject ("Type missmatch in assignment between "+leftHandSide+" and "+rightHandSide+".");
            }
        }
        else
        {
            stemmed_variable(leftHandSide); //8
            term_prime(leftHandSide);  //y
            additive_expression_prime(leftHandSide); //u
            stemmed_other_expression(leftHandSide); //9
        }
        return output;
    }
    //This method is currently not called
    public void variable() //R-> a8
    {
        if (debugMethods){
            System.out.println("variable\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (matchType("id"))
        {
            Token leftHandSide=tokens.get(tokenCounter-1);
            checkToken(leftHandSide);
            leftHandSide.setAssignedType(checkToken(leftHandSide).getAssignedType());

            TypeValue modifier=stemmed_variable(leftHandSide);
            //modify(leftHandSide,modifier);
        }
        else{error("id");}
    }

    public TypeValue stemmed_variable(Token leftHandSide) //8->[Q]|@
    {
        TypeValue output=new TypeValue();
        if (debugMethods){
            System.out.println("stemmed variable\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match("["))
        {
            Token checker;
            checker=expression(); //Q
            if (checker==null)
            {
                //skip the rest of this stuff.
            }
            else if (checker.getType().equals("int")||(checker.getAssignedType()!=null&&checker.getAssignedType().equals("int")))
            {
                output.type = "array";
                if (leftHandSide.getAssignedType().contains(" array"))
                {
                    String type = leftHandSide.getAssignedType();
                    type = type.substring(0, type.length() - 5).trim();
                    leftHandSide.setAssignedType(type);
                    if (debug)
                        {
                            System.out.print("\n this " + leftHandSide.getLexum() + "is a" + leftHandSide.getAssignedType());
                        }
                }

            }
            else
            {
                reject(checker.getLexum()+" is of an invalid type to be assigned as an element to "+leftHandSide.getLexum());
            }
        if (match("]"))
        {

            return output;
        }
        else {error("]");}
        }
        else if (look("=")||look("*")||look("/")||look("+")||look("-")||look("!")||look(">")||look("<")||look("]")||look(";")||look(")")||look(",")||look("(")||look("<=")||look(">=")||look("==")|look("!="))//check follows of 8
        {
            return output;
        }
        else {error("operand expected");}
        return output;
    }
    public void simple_expression(Token leftHandSide) //T-> U9
    {
        if (debugMethods){
            System.out.println("simple expression\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        additive_expression(leftHandSide);
        stemmed_other_expression(leftHandSide);
    }
    public void stemmed_other_expression(Token leftHandSide) //9->VU |@
    {
        if (debugMethods){
            System.out.println("stemmed expression\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (look("!")||look(">")||look("=")||look("<")||look("<=")||look(">=")||look("==")||look("!="))
        {
            relop(leftHandSide);
            additive_expression(leftHandSide);
        }
        else if(look("]")||look(";")||look(")")||look(","))//follows of 9
        {return;}
        else{error("stemmed expression");}

    }

    public void additive_expression(Token leftHandSide) //U-> Xu
    {
        if (debugMethods){
            System.out.println("additive expression\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        term(leftHandSide);
        Token rightHandside=additive_expression_prime(leftHandSide);
        compareType(leftHandSide,rightHandside);
    }
    public Token additive_expression_prime(Token leftHandSide) //u->WXu |@
    {
        if (debugMethods){
            System.out.println("additive expression prime\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (look("+")||look("-"))
        {
            String operand;
            operand =addop(leftHandSide);
            Token rightHandSide=term(leftHandSide);
            compareType(leftHandSide,rightHandSide);
            Token tempToken=addTemp(leftHandSide);
            if (operand.equals("+"))
            {
                addLine("ADD", leftHandSide.getLexum(), rightHandSide.getLexum(), tempToken.getLexum());

            }
            else
            {
                addLine("SUB", leftHandSide.getLexum(), rightHandSide.getLexum(), tempToken.getLexum());
            }
            if (compareType(leftHandSide,rightHandSide))
            {
                if (leftHandSide!=null&&rightHandSide!=null)
                {
                    if (debug) System.out.print("\n Good addition or subtraction"+leftHandSide.toString()+rightHandSide.toString());
                }
            }
            else
            {
                reject("Cannot add/subtract"+rightHandSide.getLexum()+" of type "+rightHandSide.getAssignedType()+" to/from "+rightHandSide.getLexum()+" of type "+rightHandSide.getAssignedType()+"."  );
            }
            Token next=additive_expression_prime(tempToken);
            return next;

        }
        else if (look("!")||look(">")||look("=")||look("=")||look("<")||look("]")||look(";")||look(")")||look(",")||look("<=")||look(">=")||look("==")||look("!="))//follows of u
        {
            return null;
        }
        else {error("right hand of additive expression or end of expression");}
        return null;
    }
    public void relop(Token leftHandSide) //V -> <x | >x | == | !=
    {
        if (debugMethods){
            System.out.println("relop\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
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
    public String addop(Token leftHandSide) //W-> + | -
    {
        if (debugMethods){
            System.out.println("additive operation\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match("+"))
        {return "+";}
        else if (match("-"))
        {return "-";}
            else {error("additive operand");}
        return null;
    }
    public Token term(Token leftHandSide) //X-> Zy
    {
        Token rightHandSide;
        Token intermediate;
        if (debugMethods){
            System.out.println("term\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }

        rightHandSide=factor(leftHandSide);
        compareType(leftHandSide,rightHandSide);
        rightHandSide=term_prime(leftHandSide);
        compareType(leftHandSide,rightHandSide);
        //intermediate=getIntermediate(leftHandSide,rightHandSide);
        return rightHandSide;
    }
    public Token term_prime(Token leftHandSide) //y-> YZy | @
    {
        Token rightHandSide=null;
        Token intermediate=null;
        if (debugMethods) {
            System.out.println("term prime\n TokenCounter: " + tokenCounter + "Token: " + tokens.get(tokenCounter).toString());
        }
        if (look("*")||look("/"))
        {
            String operand;

            operand=mulop(leftHandSide);  //this will need to return an indcator of multiply or divide for intermediate code generation
            rightHandSide=factor(leftHandSide); //Z
                //Todo make and call gettemp();

                Token returnToken =addTemp(leftHandSide);
                String temp=returnToken.getLexum();
                if (operand.equals("*"))
                {
                    addLine("MUL", leftHandSide.getLexum(), rightHandSide.getLexum(), temp);
                }
                else//implicit operand.equals("/")
                {
                    addLine("DIV", leftHandSide.getLexum(), rightHandSide.getLexum(), temp);
                }

                //

            if (compareType(leftHandSide, rightHandSide))
            {
                System.out.print("\n Good multiplication or division. \n");
                //intermediate code generation will go here
            }
            else
            {

                reject("multiplication operation between different types have "+leftHandSide.getLexum()+" of type "+leftHandSide.getAssignedType() +" and "+rightHandSide.getLexum()+" of type "+ rightHandSide.getAssignedType()+".");
            }
            //intermediate=getIntermediate(leftHandSide,rightHandSide);
            term_prime(rightHandSide);

            return returnToken;
        }

        else if (look("+")||look("-")||look("!")||look(">")||look("=")||look("<")||look("]")||look(";")||look(")")||look(");")||look(",")||look("<=")||look(">=")||look("==")||look("!="))//follows y
        {
            return rightHandSide;
        }
        else
        {
            error ("operand, ',', or ';' ");
        }
        //for intermediate code the return value will be the product of a multop or the left hand side for a null right hand side
        return rightHandSide;
    }
    public String mulop(Token leftHandSide) //Y-> * | /
    {
        if (debugMethods){
            System.out.println("multiplication operation\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match ("*"))
        {
            return "*" ;
        }
        else if (match("/"))
        {
            return "/";
        }
        else
        {
            error ("multiplication operand ");
        }
        return null; //dead code return
    }
    public Token factor(Token leftHandSide) /*Z -> (Q) | R | 1 | c
                         //R->a8
                        //1->a(2)
                        correction Z->(Q)|a new|c
                        new -> 8 |(2)

                        */
    {
        Token rightHandSide=null;
        if (debugMethods){
            System.out.println("factor\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match("("))
        {
            rightHandSide=expression();
            if( match (")"))
            {
                return rightHandSide;
            }
            else {error(")");}
        }
        else if (matchType("id"))//first of R and 1
        {
            rightHandSide=tokens.get(tokenCounter-1);
            checkToken(rightHandSide);
            if (debug){System.out.print(rightHandSide.toString());}
            variable_call_discriminator(rightHandSide);
        }
        else if (matchType("num")) {

            rightHandSide = tokens.get(tokenCounter - 1);
            if (leftHandSide.getType().equals("id")) {
                if (!leftHandSide.getAssignedType().equals("num")) {
                    reject("type missmatch between " + leftHandSide.getLexum() + " and " + rightHandSide.getLexum());
                }
            }
            else
            {
                if (!rightHandSide.getType().equals(leftHandSide.getType()))
                {
                    reject("type missmatch between " + leftHandSide.getLexum() + " and " + rightHandSide.getLexum());
                }
            }
            return rightHandSide;
        }
        else if (matchType("int"))
        {
            rightHandSide=tokens.get(tokenCounter-1);
            if (leftHandSide.getType().equals("id")) {
                if (!leftHandSide.getAssignedType().equals("int")) {
                    reject("type missmatch between " + leftHandSide.getLexum() + " and " + rightHandSide.getLexum());
                }
            }
            else
            {
                if (!rightHandSide.getType().equals(leftHandSide.getType()))
                {
                    reject("type missmatch between " + leftHandSide.getLexum() + " and " + rightHandSide.getLexum());
                }
            }
            return rightHandSide;
        }
        else if (matchType("float"))
        {
            rightHandSide=tokens.get(tokenCounter-1);
            if (leftHandSide.getType().equals("id")) {
                if (!leftHandSide.getAssignedType().equals("float")) {
                    reject("type missmatch between " + leftHandSide.getLexum() + " and " + rightHandSide.getLexum());
                }
            }
            else
            {
                if (!rightHandSide.getType().equals(leftHandSide.getType()))
                {
                    reject("type missmatch between " + leftHandSide.getLexum() + " and " + rightHandSide.getLexum());
                }
            }
            return rightHandSide;
        }
        else {error (" '(expression)', variable, call or number type ");}

        return rightHandSide;
    }

    //will work from call register

    public void variable_call_discriminator(Token leftHandSide) //new -> 8 |(2) | =Q
    {
        //donetodo look at this shouldn't match "=" call expression
        if (debugMethods){
            System.out.println("variable or call discriminator\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match("(")) {
            args(leftHandSide);
            if (match(")"))
            {
                System.out.print("\n out of args \n");
                return;
            }
            else{error(")");}
        }
        //first of 8 and follows of 8
        else if (look("[")||look("=")||look("*")||look("/")||look("+")||look("-")||look("!")||look(">")||look("<")||look("]")||look(";")||look(")")||look(",")||look("==")||look("!=")||look(">=")||look("<="))
        {
            stemmed_variable(leftHandSide);//8
        }

    }
    //this method is not called
    public void call() //1-> a(2)
    {
        if (debugMethods){
            System.out.println("call\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        Token callingFunction=tokens.get(tokenCounter);
        Token calledFunction;
        if (matchType("id "))
        {
            Token leftHandSide=tokens.get(tokenCounter-1);
            if ((calledFunction=findToken(callingFunction.getLexum()))!=null)
            {
                if(debug){System.out.print(callingFunction.getLexum()+" found in Symbol Table\n");}
            }
            else
            {
                reject("Function: "+callingFunction.getLexum()+" not defined in symbol table.");

            }
            if (match("("))
            {
                args(leftHandSide);
                if (match(")"))
                {
                    if (debug)
                    {System.out.print("recognized end of args");}
                    return;}
                else {error(")");}
            }
            else {error("( ");}
        }
        else {error("id ");}
    }
    public void call_prime(Token leftHandSide) //1'
    {
        System.out.println("call prime\n This method is empty and will require population\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
    }

    //will work from call register
    public void args(Token leftHandSide) //2 -> 3 | @
    {
        ArrayList parameterTypes=checkToken(leftHandSide).getParameterList();
        if (debugMethods){
            System.out.println("args\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
      if (lookType("num")||lookType("int")||lookType("float")||lookType("id")||look("("))
      {
          args_list(leftHandSide);
      }
      if (look(")"))//follows of 2
      {
          if (debug) System.out.print("\n Comaring parameters and args\n");

          if ((parameterTypes.size()==1)&&parameterTypes.get(0).equals("void"))
          {
              System.out.print("\nVoid parameters\n");
              return;
          }
          if (!(parameterTypes.size()==leftHandSide.getParameterList().size()))
          {

              reject("Calling function "+leftHandSide.getLexum()+" requires "+parameterTypes.size()+" arguments. Have "+leftHandSide.getParameterList().size()+" arguments");
          }
          for (int i=0; i<parameterTypes.size(); i++)
          {
              if (debugMethods)System.out.print("Comparing parameters and arguments\n");
              if (!parameterTypes.get(i).equals(leftHandSide.getParameterList().get(i)))
              {
                  int label=i+1;
                  reject("Type mismatch on parameter "+label+" when calling function "+leftHandSide.getLexum()+" have "+leftHandSide.getParameterList().get(i)+
                          " require "+ parameterTypes.get(i)+".");
              }


          }

              if (debugMethods)
              {
                  System.out.print("Parameters: ");
                  for (int i=0; i<parameterTypes.size(); i++)
                  {
                      System.out.print (parameterTypes.get(i)+", ");
                  }
                  System.out.print("Arguments: ");
                  for (int i=0; i<leftHandSide.getParameterList().size(); i++)
                  {
                      System.out.print (leftHandSide.getParameterList().get(i)+", ");
                  }
              }
          System.out.print("\n exiting args");
      }
        else {error(")");}
    }
    public void args_list(Token leftHandSide) //3-> Qv
    {
        String type;
        Token argument;
        if (debugMethods){
            System.out.println("arguments list\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        argument=checkToken(expression());//Q

        if (debug)System.out.print("Expression returned the argument "+argument.toString());

        type=argument.getAssignedType();
        if (argument.getAssignedType()==null)
        {
            type=argument.getType();

        }
        leftHandSide.parameterList.add(type);
        if (debug) System.out.println("\nin Args  argument type is " + type + "\n");
        if (look(")"))
        {
            if (debug)System.out.print("in args list returning to args");
            return;
        }
        else {args_list_prime(leftHandSide);}
        if (look(")"))
        {
            if (debug)System.out.print("in args list returning to args after prime\n");
            return;
        }
        else {
            error (")");
        }
    }
    public void args_list_prime(Token leftHandSide) //v-> ,Qv | @
    {
        String type;
        if (debugMethods){
            System.out.println("arguments list prime\n TokenCounter: "+tokenCounter+"Token: "+tokens.get(tokenCounter).toString());
        }
        if (match(","))
        {
            Token argument;
          argument=expression();
            type=argument.getAssignedType();
            if (type==null)
            {
                type=argument.getType();
            }
            leftHandSide.parameterList.add(type);
            if (debug) System.out.println("\nin Args prime  argument type is " + type + "\n");
         args_list_prime(leftHandSide);
        }
        else if (look(")"))//follows of v
        {return;}

        else {error(",");}

    }
}
//