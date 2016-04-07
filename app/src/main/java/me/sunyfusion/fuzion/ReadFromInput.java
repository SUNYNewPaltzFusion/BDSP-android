package me.sunyfusion.fuzion;

import java.io.*;
import java.io.File;
import java.util.Arrays;
import java.util.Scanner;


/**
 * Created by Robert Wieland on 3/28/16.
 * This class will read from the buildApp.txt and will return info
 * from that text file.
 */

public class ReadFromInput
{
    private String Type = null;    // To hold the type of category from the buildApp.txt when read.
    private String Name = null;    // To hold the unique category name from the buildApp.txt when read.
    private Scanner infile;
    private Scanner line;
    private String Line;
    private int answer = 0;        // To hold the answer to categories such as camera. 1 = yes, 0 = no

    public ReadFromInput(Scanner sq)
    {
        infile = sq; // passes the scanned File
    }

    public void getNextLine()
    {
        Line = infile.nextLine(); // gets Line
        if(Line.equals("") || Line.startsWith("//")) // ignores blank lines and comments
        {
            while(Line.equals("") || Line.startsWith("//"))
            {
                Line = infile.nextLine();
            }
        }
        line = new Scanner(Line); // scans line
    }

    public void ReadLineCollectInfo() throws FileNotFoundException
    {
        // Resets variables to read the next line fresh.
        Type = null;
        Name = null;
        answer = 0;

        if(line.hasNext())
        {
            Type = line.next(); // Gets the category and assigns to Type

            if(line.hasNextInt()) // Checks for an answer, 1 = include, 0 = do not
            {
                answer = line.nextInt();
            }

            else if (line.hasNext())
            {
                Name = line.next(); // Gets unique name of category
            }
        }
    }

    public String getType()
    {
        return Type;
    }

    public int getAnswer()
    {
        return answer;
    }


    /*
        Returns an array of all parameters of a
     */
    public String[] getArgs() {
        String[] args = null;
        String[] ls = Line.split(" ");
        if(ls.length > 2) {
            Arrays.copyOfRange(Line.split(" "), 2, Line.split(" ").length - 1);
        }
        return args;
    }

    public String getUnigueName()
    {
        return Name;
    }

}
