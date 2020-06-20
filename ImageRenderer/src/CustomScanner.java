import java.util.Scanner;

/**
 * Author: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 *
 * Custom scanner class to ignore comments inside the PBM files.
 * I would have used inheritance but the Java Scanner class is marked as final so it cannot be extended :(
 */
public class CustomScanner  {

    public Scanner InnerScanner;

    public CustomScanner(Scanner InnerScanner)
    {
        this.InnerScanner = InnerScanner;
    }

    /**
     * Grabs next token from scanner ignoring single line comments
     * @return Token string
     */
    public String next()
    {
        String token;
        while(true)
        {
            token = InnerScanner.next();
            if(token.contains("#")) //Ignore token if it contains a comment
                InnerScanner.nextLine(); //Skip the line because the rest of it will be a comment
            else
                break; //Exit the loop to return the value
        }
        return token;
    }

    /**
     * Grabs next integer from scanner ignoring single line comments
     * @return Token string
     */
    public int nextInt()
    {
        String token;
        int num;
        while(true)
        {
            token = InnerScanner.next();
            if(token.contains("#")) //If token contains the comment delimiter, skip the comment by skipping the rest of the current line
                InnerScanner.nextLine();
            else {
                try {
                    num = Integer.parseInt(token); //Attempt to parse the token as an integer
                    break;
                } catch(Exception ex){} //If error, ignore it and continue the loop;
            }
        }
        return num;
    }

    /**
     * Pass through method to skip/get the rest of a line. Only used to skip the comments at this point
     * @return
     */
    public String nextLine()
    {
        return InnerScanner.nextLine(); //Return the whole line
    }

    /**
     * Pass through method to check if the inner scanner has more data. This method does not ignore comments and will still count
     * @return
     */
    public boolean hasNext()
    {
        return InnerScanner.hasNext();
    }
}
